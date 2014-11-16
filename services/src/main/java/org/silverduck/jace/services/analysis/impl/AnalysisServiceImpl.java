package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.dao.analysis.AnalysisSettingDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.analysis.AnalysisStatus;
import org.silverduck.jace.domain.analysis.Granularity;
import org.silverduck.jace.domain.analysis.slo.SLOImport;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOStatus;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.domain.vcs.Hunk;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisServiceImpl.class);

    @EJB
    private AnalysisDao analysisDao;

    @EJB
    private AnalysisService analysisService;

    @EJB
    private AnalysisSettingDao analysisSettingDao;

    @EJB
    private ProjectService projectService;

    @Override
    @Asynchronous
    public Future<Boolean> addAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.add(setting);
        analysisService.initialAnalysis(setting.getId());
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

    private void analyseDependencies(Analysis analysis) {

        LOG.info("Clearing old dependencies in project {} in analysis {}", analysis.getProject().getName(), analysis.getId());
        analysisDao.clearDependencies(analysis.getProject().getId());
        List<SLO> slos = analysisDao.listSLOs(analysis.getProject().getId());
        LOG.debug("Building qualified class name to SLO map...");
        Map<String, SLO> qualifiedSlos = new HashMap<String, SLO>(slos.size());
        for (SLO slo : slos) {
            qualifiedSlos.put(slo.getQualifiedClassName(), slo);
        }
        LOG.debug("Starting to iterate SLOs of the analysis.");
        for (SLO slo : analysis.getSlos()) {
            LOG.debug("Analysing dependencies of SLO ({}, {}). Starting to iterate imports..", slo.getPath(), slo.getSloStatus());
            if (slo.getSloStatus() == SLOStatus.CURRENT) {
                for (SLOImport sloImport : slo.getSloImports()) {
                    LOG.debug("Analysing dependencies of SLO ({}, {}), import={}", slo.getPath(), slo.getSloStatus(), sloImport.getQualifiedClassName());
                    SLO dependency = qualifiedSlos.get(sloImport.getQualifiedClassName());
                    if (dependency != null) {
                        slo.addDependency(dependency);
                        dependency.setAnalysis(analysis);
                        LOG.debug("Found a dependency. Adding dependency for SLO {} ({}, {}) to -> {} ({}, {})",  slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId(),
                                dependency.getQualifiedClassName(), dependency.getSloStatus(), dependency.getAnalysis().getId());
                    } else {
                        LOG.debug("Couldn't find a dependency for SLO {} ({}, {})",  slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId());
                    }
                }
            }
            LOG.debug("Clearing SLO imports for SLO {} ({}, {})",  slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId());
            // Clear first-phase stuff
            slo.removeSLOImports();
        }

        LOG.info("Analysing of dependencies done for analysis {}", analysis.getId());
    }

    @Override
    @Asynchronous
    public Future<Boolean> analyseProject(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        if (setting == null) {
            throw new JaceRuntimeException("The analysis setting was not found when attempting to perform analysis.");
        }

        List<Diff> diffs = projectService.pullProject(setting.getProject());
        LOG.info("Got {} diffs from projectService", diffs.size());
        if (diffs.size() > 0) {
            Analysis analysis = createNewAnalysis(setting);

            Map<String, Diff> addedFiles = new HashMap<String, Diff>();
            List<String> modifiedFiles = new ArrayList<String>();
            List<Long> deletedSloIDs = new ArrayList<Long>();
            List<Long> oldSloIDs = new ArrayList<Long>();

            int i = 1;
            String commitIdPattern = setting.getProject().getPluginConfiguration().getCommitIdPattern();
            Pattern pattern = Pattern.compile(commitIdPattern);
            for (Diff diff : diffs) {
                LOG.debug("Checking diff {}/{}. Old oldPath='{}', newPath='{}' Mod. Type='{}', ", i, diffs.size()+1, diff.getOldPath(), diff.getNewPath(), diff.getModificationType());
                i++;
                diff.setProject(setting.getProject());
                String commitMessage = diff.getCommit().getMessage();
                String commitId;
                Matcher matcher = pattern.matcher(commitMessage);
                if (matcher.find()) {
                    commitId = StringUtils.left(matcher.group(), 4090);
                } else {
                    // TODO: Consider making this configurable, not everyone likes to see such things
                    commitId = StringUtils.left(commitMessage, 4090);
                }

                diff.getCommit().setCommitId(commitId);

                String oldPath = diff.getOldPath();
                if (!oldPath.startsWith("/")) {
                    oldPath = "/" + oldPath;
                }
                String newPath = "/" + diff.getNewPath();

                SLO oldSlo = findSloByPath(oldPath, analysis.getProject().getId());


                switch (diff.getModificationType()) {
                case ADD:
                    LOG.debug("Adding new file with oldPath {} to list of added and modified files", diff.getNewPath());
                    addedFiles.put(newPath, diff);
                    modifiedFiles.add(newPath);
                    break;
                case MODIFY:
                    modifiedFiles.add(newPath);
                    if (oldSlo != null) {
                        if (setting.getGranularity() == Granularity.METHOD) {
                            for (Hunk hunk : diff.getParsedDiff().getHunks()) {
                                JavaMethod method = analysisDao.findMethodByLineNumber(oldSlo, hunk.getOldStartLine());
                                if (method != null) {
                                    // found a previous method
                                    // TODO: Add Feature map to a method. It should look up all used Types and determine
                                    // the features based on that (or, just map to SLOs)
                                }
                            }
                        } else {
                            LOG.debug("The feature '{}' has changed by modification to SLO with oldPath '{}'", (oldSlo.getFeature() == null ? "Unknown" : oldSlo.getFeature().getName()), oldSlo.getPath());
                            oldSloIDs.add(oldSlo.getId()); // This will be updated as 'OLD'
                            analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                        }
                    }

                    break;
                case DELETE:
                    if (oldSlo != null) {
                        LOG.debug("The feature '{}' has changed by removal of SLO with oldPath '{}'", (oldSlo.getFeature() == null ? "Unknown" : oldSlo.getFeature().getName()), oldSlo.getPath());
                        deletedSloIDs.add(oldSlo.getId());
                        analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                    }
                    break;
                case RENAME:
                    if (oldSlo != null) {
                        LOG.debug("The feature '{}' has changed by renaming of SLO with oldPath '{}'", (oldSlo.getFeature() == null ? "Unknown" : oldSlo.getFeature().getName()), oldSlo.getPath());
                        oldSloIDs.add(oldSlo.getId());
                        analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                    }
                    modifiedFiles.add(newPath);
                    break;
                case COPY:
                    modifiedFiles.add(newPath);
                    break;
                }
            }

            LOG.debug("Marking SLOs with IDs as old: {}", oldSloIDs);
            // Mark old SLOs appropriately
            markSLOsAsOld(oldSloIDs);
            LOG.debug("Marking SLOs with IDs as deleted: {}", deletedSloIDs);
            markSLOsAsDeleted(deletedSloIDs);

            LOG.debug("Starting to analyse the modified SLOs. Project='{}', Branch='{}', Files={}", setting.getProject().getName(), setting.getBranch(), modifiedFiles);
            // Analyse all modified/added files and generate new SLOs
            analyseSLOs(setting, analysis, modifiedFiles);
            LOG.info("Starting to analyse dependencies.");
            analyseDependencies(analysis);

            LOG.info("Starting to iterate the added file set");
            // Iterate the added files set
            Iterator<Map.Entry<String, Diff>> iterator = addedFiles.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Diff> item = iterator.next();
                String path = item.getKey();
                Diff diff = item.getValue();

                SLO newSlo = findSloByPath(path, analysis.getProject().getId());
                if (newSlo != null) {
                    LOG.debug("The feature '{}' has changed by adding a new SLO with path '{}'", (newSlo.getFeature() == null ? "Unknown" : newSlo.getFeature().getName()), newSlo.getPath());
                    analysis.addChangedFeature(new ChangedFeature(newSlo.getFeature(), newSlo, diff));
                }
            }
            LOG.debug("Analysis complete! Persisting...");
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
            analysisDao.update(analysis);
            LOG.debug("Persisted.");
        }
        LOG.debug("Returning AsyncResult.");
        return new AsyncResult<Boolean>(true);
    }

    private String parseCommitId(String commitPattern, String message) {
        if (!StringUtils.isEmpty(commitPattern)) {
            Pattern pattern = Pattern.compile(commitPattern);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return StringUtils.left(matcher.group(), 4090);
            } else {
                // TODO: Consider making this configurable, not everyone likes to see such things
                return StringUtils.left(message, 4090);
            }

        }

        return "";
    }

    private Analysis createNewAnalysis(AnalysisSetting setting) {
        Analysis analysis = new Analysis();
        analysis.setProject(setting.getProject());
        analysis.setAnalysisStatus(AnalysisStatus.ANALYSING);
        analysis.setAnalysisSetting(setting);
        analysisDao.add(analysis);
        return analysis;
    }

    private void analyseSLOs(AnalysisSetting setting, Analysis analysis, List<String> modifiedFiles) {
        // Walk all files in the tree and analyse them

        modifiedFiles.add(setting.getProject().getReleaseInfo().getPathToVersionFile()); // always read rel file
        try {
            Files.walkFileTree(Paths.get(analysis.getProject().getPluginConfiguration().getLocalDirectory()),
                new AnalysisFileVisitor(setting, analysis, modifiedFiles));
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform analysis.", e);
        }
    }



    @Override
    public List<AnalysisSetting> listAllAnalysisSettings() {
        return analysisSettingDao.findAllAnalysisSettings();
    }

    @Override
    public Analysis findAnalysisById(Long id) {
        return (Analysis) analysisDao.find(Analysis.class, id);
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long id) {
        return analysisSettingDao.findAnalysisSettingById(id);
    }

    private SLO findSloByPath(String path, Long projectId) {
        return analysisDao.findSLO(path, projectId);
    }

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param analysisSettingId
     */
    @Override
    public void initialAnalysis(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        String localDirectory = setting.getProject().getPluginConfiguration().getLocalDirectory();

        Analysis analysis = new Analysis();
        analysis.setProject(setting.getProject());
        analysis.setAnalysisSetting(setting);
        // This is a "root"-analysis
        analysis.setInitialAnalysis(true);
        analysis.setAnalysisStatus(AnalysisStatus.INITIAL_ANALYSIS);
        analysisDao.add(analysis);
        try {
            // Change branch to the one defined in the setting
            projectService.changeBranch(setting.getProject().getPluginConfiguration().getLocalDirectory(),
                setting.getBranch());

            // Walk all files in the tree and analyse them
            Files.walkFileTree(Paths.get(localDirectory), new InitialAnalysisFileVisitor(setting, analysis));

            analyseDependencies(analysis);

            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
        } catch (IOException e) {
            analysis.setAnalysisStatus(AnalysisStatus.ERROR);
            analysisDao.update(analysis);
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }

    }

    @Override
    public List<ScoredCommit> listScoredCommitsByRelease(Long projectId, String releaseVersion) {
        // Get the directly changed features from db and the initial score with a query
        List<ScoredCommit> scoredCommitList = new ArrayList<ScoredCommit>();
        Map<String, ScoredCommit> commitIdScoredCommitMap = new HashMap<String, ScoredCommit>();
        List<Object[]> commits = analysisDao.listScoredCommitsByProjectAndRelease(projectId, releaseVersion);
        for (Object[] commit : commits) {
            Double score = new Double((Long)commit[0]);
            String commitId = (String) commit[1];
            ScoredCommit scoredCommit = new ScoredCommit(releaseVersion, commitId, score);
            scoredCommitList.add(scoredCommit);
            commitIdScoredCommitMap.put(commitId, scoredCommit);
            LOG.debug("Found scored commit with id '" + commitId + "' and score '" + score);
        }

        // Analyse all dependencies of the changed features
        DependencyCalculator calculator = new DependencyCalculator();
        List<ChangedFeature> changedFeatures = analysisDao.listChangedFeaturesByProjectAndRelease(projectId, releaseVersion);
        for (ChangedFeature cf : changedFeatures) {
            if (cf.getAnalysis().getAnalysisSetting().getGranularity() == Granularity.FILE) {
                Double score = calculator.calculateScore(cf.getSlo());
                List<Integer> deps = calculator.calculateDependencies(cf.getSlo());
                ScoredCommit scoredCommit = commitIdScoredCommitMap.get(cf.getDiff().getCommit().getCommitId());
                if (scoredCommit != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding dependencies score of amount '{}" +
                                "' for commitId '{} with existing score of '{}'", score, scoredCommit.getCommitId()
                                + scoredCommit.getScore());
                    }
                    scoredCommit.setScore(scoredCommit.getScore() + score);
                    scoredCommit.setDirectChanges(scoredCommit.getDirectChanges() + 1);
                    addDependendenciesToScoredCommit(scoredCommit, deps);
                }
            } else {
                // TODO: Implement method level granularity score calculation
            }
        }

        return sortScoredCommitList(scoredCommitList);
    }

    private void addDependendenciesToScoredCommit(ScoredCommit scoredCommit, List<Integer> deps) {
        List<Integer> dependenciesPerLevel = scoredCommit.getDependenciesPerLevel();
        if (dependenciesPerLevel != null) {
            for (int i = 0; i < dependenciesPerLevel.size(); i++) {
                if (i < deps.size()) {
                    Integer currentAmount = dependenciesPerLevel.get(i);
                    dependenciesPerLevel.set(i, currentAmount + deps.get(i));
                }
            }

            if (dependenciesPerLevel.size() < deps.size()) {
                for (int i = dependenciesPerLevel.size(); i < deps.size(); i++) {
                    dependenciesPerLevel.add(deps.get(i));
                }
            }
        }
    }

    private List<ScoredCommit> sortScoredCommitList(List<ScoredCommit> list) {
        Collections.sort(list, new Comparator<ScoredCommit>() {
            @Override
            public int compare(ScoredCommit o1, ScoredCommit o2) {
                CompareToBuilder ctb = new CompareToBuilder();
                ctb.append(o2.getScore(), o1.getScore()); // inverse
                return ctb.toComparison();
            }
        });
        return list;
    }

    private void markSLOsAsDeleted(List<Long> deletedSloIDs) {
        if (deletedSloIDs.size() > 0) {
            analysisDao.updateSlosAsDeleted(deletedSloIDs);
        }
    }

    private void markSLOsAsOld(List<Long> oldSloIDs) {
        if (oldSloIDs.size() > 0) {
            analysisDao.updateSlosAsOld(oldSloIDs);
        }
    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(id);
        List<Analysis> analyses = analysisDao.listAnalysesBySetting(id);
        for (Analysis analysis : analyses) {
            analysisDao.remove(analysis);
        }
        analysisSettingDao.remove(setting);
    }

    @Override
    public void triggerAnalysis(Long analysisSettingId) {
        analysisService.analyseProject(analysisSettingId);
    }

    @Override
    @Asynchronous
    public Future<Boolean> updateAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.update(setting);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

    @Override
    public List<Analysis> listAllAnalyses() {
        return analysisDao.listAllAnalyses();
    }

    @Override
    public List<String> listAllReleases(Long projectId) {
        return analysisDao.listAllReleases(projectId);
    }

    private class DependencyCalculator {
        private Set<SLO> processed = new HashSet<SLO>();


        /**
         * Calculates the total amount of dependencies per level
         * @param slo SLO to calculate dependencies for
         * @return An list cotainind the amount of dependencies per level.
         * The size of the list indicates the amount of levels.
         */
        public List<Integer> calculateDependencies(SLO slo) {
            processed.clear();
            Map<Integer, Integer> depthDependencies = new HashMap<Integer, Integer>();
            calculateFileDependenciesAmount(slo, depthDependencies, 1);
            List<Integer> result = new ArrayList<Integer>();
            for (int i = 1; i < depthDependencies.size() + 1; i++) {
                result.add(depthDependencies.get(i));
            }
            return result;
        }

        protected void calculateFileDependenciesAmount(SLO slo, Map<Integer, Integer> depthDependencies, int depth) {

            if (!processed.contains(slo)) {
                processed.add(slo);
                Integer dependencies = depthDependencies.get(depth);
                if (dependencies == null) {
                    dependencies = new Integer(0);
                }

                List<SLO> dependantOf = slo.getDependantOf();
                int deps = calculateDeps(dependantOf);
                dependencies += deps;
                depthDependencies.put(depth, dependencies);

                for (SLO dependency : dependantOf) {
                    if (dependency.getSloStatus() == SLOStatus.CURRENT) {
                        calculateFileDependenciesAmount(dependency, depthDependencies, depth + 1);
                    }
                }
            }
        }

        public Double calculateScore(SLO slo) {
            processed.clear();
            return calculateFileDependenciesScore(slo, 1);
        }

        protected Double calculateFileDependenciesScore(SLO slo, int depth) {
            Double score = 0D;

            List<SLO> dependantOf = slo.getDependantOf();

            if (LOG.isTraceEnabled()) {
                logDependencyPaths(slo, dependantOf);
            }

            if (!processed.contains(slo)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Adding SLO '" + slo.getPath() + "' to set of processed dependencies");
                }
                processed.add(slo);

                double deps = calculateDeps(dependantOf);
                score += (deps / (double)depth); // direct dependencies multiplier = 1, for each level divide by depth

                LOG.trace("Calculated score of '" + score + "' for dependency '" + slo.getPath() + "' at depth " + depth);
                for (SLO dependency : dependantOf) {
                    if (dependency.getSloStatus() == SLOStatus.CURRENT) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Calculating dependencies score for dependency " + dependency.getPath());
                        }
                        score += calculateFileDependenciesScore(dependency, depth + 1);
                    }
                }
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Returning score '" + score + "' for SLO '" + slo.getPath() + "' at depth " + depth);
            }
            return score;
        }

        private int calculateDeps(List<SLO> dependantOf) {
            int deps = 0;
            for (SLO dependency : dependantOf) {
                if (dependency.getSloStatus() == SLOStatus.CURRENT) {
                    deps++;
                }
            }
            return deps;
        }

        private void logDependencyPaths(SLO slo, List<SLO> dependantOf) {
            List<String> paths = new ArrayList<String>();
            for (SLO dependency : dependantOf) {
                paths.add(dependency.getPath());
            }

            LOG.trace("The SLO '" + slo.getPath() + "' is dependant of following classes: " + StringUtils.join(paths, ", "));
        }
    }
}
