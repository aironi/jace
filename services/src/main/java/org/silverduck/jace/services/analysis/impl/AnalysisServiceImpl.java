package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.domain.vcs.Hunk;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    private static final Log LOG = LogFactory.getLog(AnalysisServiceImpl.class);

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

        List<SLO> slos = analysisDao.listSLOs(analysis.getProject().getId());
        Map<String, SLO> qualifiedSlos = new HashMap<String, SLO>(slos.size());
        for (SLO slo : slos) {
            qualifiedSlos.put(slo.getQualifiedClassName(), slo);
        }
        for (SLO slo : analysis.getSlos()) {
            for (SLOImport sloImport : slo.getSloImports()) {
                SLO dependency = qualifiedSlos.get(sloImport.getQualifiedClassName());
                if (dependency != null) {
                    slo.addDependency(dependency);
                    dependency.setAnalysis(analysis);
                    LOG.fatal("Adding dependency for SLO " + slo.getQualifiedClassName() + " to -> "
                        + dependency.getQualifiedClassName());
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
            Analysis analysis = new Analysis();
            analysis.setProject(setting.getProject());
            analysis.setAnalysisStatus(AnalysisStatus.ANALYSING);
            analysis.setAnalysisSetting(setting);
            analysisDao.add(analysis);

            Map<String, Diff> addedFiles = new HashMap<String, Diff>();
            List<String> modifiedFiles = new ArrayList<String>();
            List<Long> deletedSloIDs = new ArrayList<Long>();
            List<Long> oldSloIDs = new ArrayList<Long>();

            for (Diff diff : diffs) {
                diff.setProject(setting.getProject());
                String commitPattern = setting.getProject().getPluginConfiguration().getCommitIdPattern();
                if (!StringUtils.isEmpty(commitPattern)) {
                    Pattern pattern = Pattern.compile(commitPattern);
                    Matcher matcher = pattern.matcher(diff.getCommit().getMessage());
                    if (matcher.find()) {
                        diff.getCommit().setCommitId(matcher.group());
                    } else {
                        // TODO: Consider making this configurable, not everyone likes to see such things
                        diff.getCommit().setCommitId(diff.getCommit().getMessage());
                    }

                }

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

    protected Long calculateFileDependenciesScore(SLO slo, Set<SLO> processed, int depth) {
        Long score = 0L;

        List<SLO> dependsOn = slo.getDependsOn();
        score += (dependsOn.size() / depth); // direct dependencies multiplier = 1, for each level divide by depth

        if (!processed.contains(slo)) {
            processed.add(slo);
            for (SLO dependency : dependsOn) {
                score += calculateFileDependenciesScore(dependency, processed, depth + 1);
            }
        }

        return score;
    }

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
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
            // analysisDao.update(analysis);
        } catch (IOException e) {
            analysis.setAnalysisStatus(AnalysisStatus.ERROR);
            analysisDao.update(analysis);
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }

    }

    @Override
    public List<ScoredCommit> listScoredCommitsByRelease(Long projectId, String releaseVersion) {
        // Get the directly changed feature from db and the initial score
        Map<String, Long> commitScoreMap = new HashMap<String, Long>();
        List<Object[]> commits = analysisDao.listScoredCommitsByRelease(projectId, releaseVersion);
        for (Object[] commit : commits) {
            Long score = (Long) commit[0];
            String commitId = (String) commit[1];
            commitScoreMap.put(commitId, score);
        }

        // Analyse all changed feature dependencies
        List<ChangedFeature> changedFeatures = analysisDao.listChangedFeaturesByRelease(projectId, releaseVersion);
        for (ChangedFeature cf : changedFeatures) {
            Long score = commitScoreMap.get(cf.getDiff().getCommit().getCommitId());

            if (score != null) {
                if (cf.getAnalysis().getAnalysisSetting().getGranularity() == Granularity.FILE) {
                    Set<SLO> processed = new HashSet<SLO>();
                    processed.add(cf.getSlo());
                    score += calculateFileDependenciesScore(cf.getSlo(), processed, 1);
                } else {
                    // TODO: Implement method level granularity score calculation
                }
                commitScoreMap.put(cf.getDiff().getCommit().getCommitId(), score);
            }
        }

        // Create a ordered ScoredCommit list...
        List<ScoredCommit> list = new ArrayList<ScoredCommit>();
        Iterator<Map.Entry<String, Long>> iterator = commitScoreMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> item = iterator.next();
            list.add(new ScoredCommit(item.getKey(), item.getValue()));
        }

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
}
