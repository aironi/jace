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

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
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

        analysisDao.clearDependencies(analysis.getProject().getId());
        List<SLO> slos = analysisDao.listSLOs(analysis.getProject().getId());
        Map<String, SLO> qualifiedSlos = new HashMap<String, SLO>(slos.size());
        for (SLO slo : slos) {
            qualifiedSlos.put(slo.getQualifiedClassName(), slo);
        }
        for (SLO slo : analysis.getSlos()) {
            if (slo.getSloStatus() == SLOStatus.CURRENT) {
                for (SLOImport sloImport : slo.getSloImports()) {
                    SLO dependency = qualifiedSlos.get(sloImport.getQualifiedClassName());
                    if (dependency != null) {
                        slo.addDependency(dependency);
                        dependency.setAnalysis(analysis);
                        LOG.debug("Adding dependency for SLO {} ({}, {}) to -> {} ({}, {})",  slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId(),
                                dependency.getQualifiedClassName(), dependency.getSloStatus(), dependency.getAnalysis().getId());
                    }
                }
            }
            // Clear first-phase stuff
            slo.removeSLOImports();
        }
    }

    @Override
    @Asynchronous
    public Future<Boolean> analyseProject(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        if (setting == null) {
            throw new JaceRuntimeException("The analysis setting was not found when attempting to perform analysis.");
        }

        List<Diff> diffs = projectService.pullProject(setting.getProject());

        if (diffs.size() > 0) {
            Analysis analysis = createNewAnalysis(setting);

            Map<String, Diff> addedFiles = new HashMap<String, Diff>();
            List<String> modifiedFiles = new ArrayList<String>();
            List<Long> deletedSloIDs = new ArrayList<Long>();
            List<Long> oldSloIDs = new ArrayList<Long>();

            for (Diff diff : diffs) {
                diff.setProject(setting.getProject());
                String commitIdPattern = setting.getProject().getPluginConfiguration().getCommitIdPattern();
                String commitMessage = diff.getCommit().getMessage();
                String commitId = parseCommitId(commitIdPattern, commitMessage);
                diff.getCommit().setCommitId(commitId);

                String path = diff.getOldPath();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                SLO oldSlo = findSloByPath(path, analysis.getProject().getId());

                switch (diff.getModificationType()) {
                case ADD:
                    addedFiles.put("/" + diff.getNewPath(), diff);
                    modifiedFiles.add("/" + diff.getNewPath());
                    break;
                case MODIFY:
                    modifiedFiles.add("/" + diff.getNewPath());
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
                            oldSloIDs.add(oldSlo.getId()); // This will be updated as 'OLD'
                            analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                        }
                    }

                    break;
                case DELETE:
                    if (oldSlo != null) {
                        deletedSloIDs.add(oldSlo.getId());
                        analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                    }
                    break;
                case RENAME:
                    if (oldSlo != null) {
                        oldSloIDs.add(oldSlo.getId());
                        analysis.addChangedFeature(new ChangedFeature(oldSlo.getFeature(), oldSlo, diff));
                    }
                    modifiedFiles.add("/" + diff.getNewPath());
                    break;
                case COPY:
                    modifiedFiles.add("/" + diff.getNewPath());
                    break;
                }
            }

            // Mark old SLOs appropriately
            markSLOsAsOld(oldSloIDs);
            markSLOsAsDeleted(deletedSloIDs);

            // Analyse all modified/added files and generate new SLOs
            analyseSLOs(setting, analysis, modifiedFiles);

            analyseDependencies(analysis);

            // Iterate the added files set
            Iterator<Map.Entry<String, Diff>> iterator = addedFiles.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Diff> item = iterator.next();
                String path = item.getKey();
                Diff diff = item.getValue();

                SLO newSlo = findSloByPath(path, analysis.getProject().getId());
                if (newSlo != null) {
                    analysis.addChangedFeature(new ChangedFeature(newSlo.getFeature(), newSlo, diff));
                }
            }

            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
            analysisDao.update(analysis);
        }
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
            Long score = (Long)commit[0];
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
                Long score = calculator.calculateScore(cf.getSlo());
                List<Integer> deps = calculator.calculateDependencies(cf.getSlo());
                ScoredCommit scoredCommit = commitIdScoredCommitMap.get(cf.getDiff().getCommit().getCommitId());
                if (scoredCommit != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding dependencies score of amount '" + score +
                                "' for commitId '" + scoredCommit.getCommitId() +
                                "' with existing score of '" + scoredCommit.getScore() + "'");
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
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
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

        public Long calculateScore(SLO slo) {
            processed.clear();
            return calculateFileDependenciesScore(slo, 1);
        }

        protected Long calculateFileDependenciesScore(SLO slo, int depth) {
            Long score = 0L;

            List<SLO> dependantOf = slo.getDependantOf();

            if (LOG.isTraceEnabled()) {
                logDependencyPaths(slo, dependantOf);
            }

            if (!processed.contains(slo)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Adding SLO '" + slo.getPath() + "' to set of processed dependencies");
                }
                processed.add(slo);

                int deps = calculateDeps(dependantOf);
                score += (deps / depth); // direct dependencies multiplier = 1, for each level divide by depth

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
