package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.dao.analysis.AnalysisSettingDao;
import org.silverduck.jace.dao.vcs.DiffDao;
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
import org.silverduck.jace.domain.vcs.ModificationType;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
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
    public static final int BATCH_SIZE = 10;

    @EJB
    private AnalysisDao analysisDao;

    @EJB
    private AnalysisService analysisService;

    @EJB
    private AnalysisSettingDao analysisSettingDao;

    @EJB
    private ProjectService projectService;

    @EJB
    private DiffDao diffDao;

    @Resource
    private EJBContext context;

    @Override
    @Asynchronous
    public Future<Boolean> addAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.add(setting);
        analysisService.initialAnalysis(setting.getId());
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void analyseDependencies(Analysis analysis) {
        LOG.info("Clearing old dependencies in project {} in analysis {}", analysis.getProject().getName(), analysis.getId());
        analysisDao.clearDependencies(analysis.getProject().getId());

        final int pageSize = 20;
        int offset = pageSize;

        List<SLO> slosToAnalyse = analysisDao.listSLOsForDependencyAnalysis(analysis.getId(), 0, pageSize);
        int batch = 1;
        do {
            if (LOG.isDebugEnabled()) {
                List<Long> idList = new ArrayList<>();
                for (SLO slo : slosToAnalyse) {
                    idList.add(slo.getId());
                }
                LOG.debug("List to analyse in batch {}: {}", batch, idList);

            }

            analyseDependenciesBatch(analysis, slosToAnalyse);
            offset += pageSize;
            slosToAnalyse = analysisDao.listSLOsForDependencyAnalysis(analysis.getId(), offset, pageSize);
            batch++;
        } while (!slosToAnalyse.isEmpty());

        LOG.info("Analysing of dependencies is complete for analysis {}", analysis.getId());
    }


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void analyseDependenciesBatch(Analysis analysis, List<SLO> slosToAnalyse) {
        for (SLO slo : slosToAnalyse) {

            LOG.debug("Analysing dependencies of SLO ({}, {}). Qual.cl.name={}, commit={}", slo.getPath(), slo.getSloStatus(), slo.getQualifiedClassName(), slo.getCommit());
            for (SLOImport sloImport : slo.getSloImports()) {
                LOG.debug("Analysing import={}", sloImport.getQualifiedClassName());
                SLO dependency = analysisDao.findSLOByQualifiedClassName(sloImport.getQualifiedClassName(), analysis.getProject().getId(), slo.getCommit().getAuthorDateOfChange());

                if (dependency != null) {
                    slo.addDependency(dependency);

                    LOG.debug("Found a dependency. Adding dependency for SLO {} ({}, {}) to -> {} ({}, {})", slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId(),
                            dependency.getQualifiedClassName(), dependency.getSloStatus(), dependency.getAnalysis().getId());
                } else {
                    LOG.debug("Couldn't find a dependency for import {}",sloImport.getQualifiedClassName());
                }
            }

            LOG.debug("Clearing SLO imports for SLO {} ({}, {})", slo.getQualifiedClassName(), slo.getSloStatus(), slo.getAnalysis().getId());
            slo.removeSLOImports();
            LOG.debug("Updating SLO to db {}", slo.getPath());
            analysisDao.updateSlo(slo);
        }
    }

    /**
     * Analyses a project. Pulls the changes from repository and determinse the changed features from Diffs. Then analyses the SLOs and their dependencies.
     * TODO: Re-write. The batching changes were necessary to get performance acceptable but now there's only the minor task of refactoring this fine piece of code...
     * @param analysisSettingId The analysisSettingId that defines the analysis to be performed
     * @return
     */
    @Override
    @Asynchronous
    public Future<Boolean> analyseProject(Long analysisSettingId) {
        LOG.debug("analyseProject(): Finding setting...");
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        if (setting == null) {
            throw new JaceRuntimeException("The analysis setting was not found when attempting to perform analysis.");
        }

        LOG.debug("analyseProject(): Creating new analysis...");
        Analysis analysis = createNewAnalysis(setting);

        LOG.debug("analyseProject(): Pulling changes...");
        projectService.pullProject(setting.getProject(), analysis);

        final int pageSize = 20;
        int offset = pageSize;
        List<Diff> diffs = diffDao.listDiffs(analysis.getId(), 0, pageSize);

        LOG.info("Got {} diffs from projectService", diffs.size());
        if (diffs.size() > 0) {
            Set<SLO> newSLOs = new HashSet<>(diffs.size());
            List<Long> deletedSloIDs = new ArrayList<>(diffs.size());
            List<Long> oldSloIDs = new ArrayList<>(diffs.size());

            do {
                analyseDiffsBatch(analysis, diffs, newSLOs, deletedSloIDs, oldSloIDs);
                diffs = diffDao.listDiffs(analysis.getId(), offset, pageSize);
                offset += pageSize;
            } while (!diffs.isEmpty());

            LOG.debug("Marking SLOSs with IDs as old: {}", oldSloIDs);
            // Mark old SLOs appropriately
            markSLOsAsOld(oldSloIDs);
            LOG.debug("Marking SLOs with IDs as deleted: {}", deletedSloIDs);
            markSLOsAsDeleted(deletedSloIDs);

            analysis = (Analysis) analysisDao.find(Analysis.class, analysis.getId());
            LOG.debug("Starting to analyse the modified SLOs. Project='{}', Branch='{}', Files={}", setting.getProject().getName(), setting.getBranch(), newSLOs);

            analyseSLOs(analysis, newSLOs);

            LOG.info("Starting to analyse dependencies.");
            analyseDependencies(analysis);

            analysis = (Analysis) analysisDao.find(Analysis.class, analysis.getId());

            LOG.debug("Analysis complete! Persisting...");
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
            analysisDao.update(analysis);
            LOG.debug("Persisted.");
        }
        LOG.debug("Returning AsyncResult.");
        return new AsyncResult<Boolean>(true);
    }


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addNewFeature(Analysis analysis, SLO newSlo, Diff diff) {
        if (newSlo != null) {
            LOG.debug("The feature '{}' has changed by adding a new SLO with path '{}'", (newSlo.getFeature() == null ? "Unknown" : newSlo.getFeature().getName()), newSlo.getPath());
            analysisDao.addChangedFeature(new ChangedFeature(analysis, newSlo.getFeature(), newSlo, diff));
        }
    }


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void analyseDiffsBatch(Analysis analysis, List<Diff> diffs, Set<SLO> newSLOs, List<Long> deletedSloIDs, List<Long> oldSloIDs) {
        String commitIdPattern = analysis.getAnalysisSetting().getProject().getPluginConfiguration().getCommitIdPattern();
        Pattern pattern = Pattern.compile(commitIdPattern);

        SLO oldSlo;
        for (Diff diff : diffs) {
            LOG.debug("Checking diff. Old oldPath='{}', newPath='{}' Mod. Type='{}', ", diff.getOldPath(), diff.getNewPath(), diff.getModificationType());
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

            Diff updatedDiff = diffDao.update(diff);
            SLO newSlo = null;
            if (updatedDiff.getModificationType() != ModificationType.DELETE) {
                newSlo = createNewSlo(analysis, diff, newPath);
                newSLOs.add(newSlo);
            }

            switch (updatedDiff.getModificationType()) {
                case ADD:
                    LOG.debug("Adding new file with oldPath {} to list of added and modified files", updatedDiff.getNewPath());
                    addNewFeature(analysis, newSlo, diff);
                    break;
                case MODIFY:
                    oldSlo = findSloByPath(oldPath, analysis.getProject().getId());
                    if (oldSlo != null) {
                        if (analysis.getAnalysisSetting().getGranularity() == Granularity.METHOD) {
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
                            addChangedFeature(analysis, oldSlo, updatedDiff);
                        }
                    }
                    break;
                case DELETE:
                    oldSlo = findSloByPath(oldPath, analysis.getProject().getId());
                    if (oldSlo != null) {
                        LOG.debug("The feature '{}' has changed by removal of SLO with oldPath '{}'", (oldSlo.getFeature() == null ? "Unknown" : oldSlo.getFeature().getName()), oldSlo.getPath());
                        deletedSloIDs.add(oldSlo.getId());
                        addChangedFeature(analysis, oldSlo, updatedDiff);
                    }
                    break;
                case RENAME:
                    oldSlo = findSloByPath(oldPath, analysis.getProject().getId());
                    if (oldSlo != null) {
                        LOG.debug("The feature '{}' has changed by renaming of SLO with oldPath '{}'", (oldSlo.getFeature() == null ? "Unknown" : oldSlo.getFeature().getName()), oldSlo.getPath());
                        oldSloIDs.add(oldSlo.getId());
                        addChangedFeature(analysis, oldSlo, updatedDiff);
                    }
                    break;
                case COPY:
                    // no op
                    break;
            }
        }
    }

    private SLO createNewSlo(Analysis analysis, Diff diff, String newPath) {
        SLO newSlo = new SLO(newPath, null);
        newSlo.setAnalysis(analysis);
        if (diff != null) {
            newSlo.setCommit(diff.getCommit());
        }
        analysisDao.updateSlo(newSlo);
        return newSlo;
    }

    private void addChangedFeature(Analysis analysis, SLO slo, Diff diff) {
        analysisDao.addChangedFeature(new ChangedFeature(analysis, slo.getFeature(), slo, diff));
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


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void analyseSLOs(Analysis analysis, Set<SLO> slos) {
        // Walk all files in the tree and analyse them
        SLO releaseSlo = createNewSlo(analysis, null, analysis.getAnalysisSetting().getProject().getReleaseInfo().getPathToVersionFile());
        slos.add(releaseSlo); // always read rel file
        try {
            Files.walkFileTree(Paths.get(analysis.getProject().getPluginConfiguration().getLocalDirectory()),
                new AnalysisFileVisitor(analysis, slos));
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform analysis.", e);
        }

        for (SLO slo : slos) {
            analysisDao.updateSlo(slo);
        }
        analysisDao.update(analysis);
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
                setting.getBranch(), setting.getProject().getPluginConfiguration().getStartPoint());

            // Walk all files in the tree and analyse them
            Files.walkFileTree(Paths.get(localDirectory), new InitialAnalysisFileVisitor(analysis));
            analysisDao.update(analysis);

            analyseDependencies(analysis);

            analysis = (Analysis) analysisDao.find(Analysis.class, analysis.getId());
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
            LOG.debug("Found scored commit with id '{}' and score '{}'", commitId, score);
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
                        LOG.debug("Adding dependencies score of amount '{}'" +
                                " for commitId '{}' with existing score of '{}'", score, scoredCommit.getCommitId(), scoredCommit.getScore());
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

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markSLOsAsDeleted(List<Long> deletedSloIDs) {
        if (deletedSloIDs.size() > 0) {
            analysisDao.updateSlosAsDeleted(deletedSloIDs);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markSLOsAsOld(List<Long> oldSloIDs) {
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

    protected static class DependencyCalculator {
        private Set<SLO> processed = new HashSet<SLO>();


        /**
         * Calculates the total amount of dependencies per level
         * @param slo SLO to calculate dependencies for
         * @return An list containing the amount of dependencies per level.
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
                    if (dependency.getSloStatus() == SLOStatus.CURRENT && !dependency.getDependantOf().isEmpty()) {
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

            if (LOG.isDebugEnabled()) {
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
                    if (dependency.getSloStatus() == SLOStatus.CURRENT && !dependency.getDependantOf().isEmpty()) {
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

            LOG.debug("The SLO '" + slo.getPath() + "' is dependant of following classes: " + StringUtils.join(paths, ", "));
        }
    }
}
