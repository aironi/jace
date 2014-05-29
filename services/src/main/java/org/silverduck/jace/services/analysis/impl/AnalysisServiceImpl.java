package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.dao.analysis.AnalysisSettingDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.analysis.AnalysisStatus;
import org.silverduck.jace.domain.analysis.Granularity;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
                    }
                }

                String path = diff.getOldPath();
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }

                SLO oldSlo = findSloByPath(path);

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

                SLO newSlo = findSloByPath(path);
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

    private SLO findSloByPath(String path) {
        return analysisDao.findSLO(path);
    }

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param analysisSettingId
     */
    @Asynchronous
    public Future<Boolean> initialAnalysis(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        String localDirectory = setting.getProject().getPluginConfiguration().getLocalDirectory();

        Analysis analysis = new Analysis();
        analysis.setProject(setting.getProject());
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

            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
            analysisDao.update(analysis);
        } catch (IOException e) {
            analysis.setAnalysisStatus(AnalysisStatus.ERROR);
            analysisDao.update(analysis);
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }
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

    @Override
    public List<ChangedFeature> listChangedFeaturesByRelease(String release) {
        return analysisDao.listChangedFeaturesByRelease(release);
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
        analysisSettingDao.remove(setting);
    }

    @Override
    public void triggerAnalysis(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        analyseProject(analysisSettingId);
    }

    @Override
    @Asynchronous
    public Future<Boolean> updateAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.update(setting);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }
}
