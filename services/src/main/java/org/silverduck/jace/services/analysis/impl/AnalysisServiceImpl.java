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
import org.silverduck.jace.domain.slo.JavaSourceSLO;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
    public void analyseProject(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        if (setting == null) {
            throw new JaceRuntimeException("The analysis setting was not found when attempting to perform analysis.");
        }

        List<Diff> diffs = projectService.pullProject(setting.getProject());
        if (diffs.size() > 0) {
            Analysis analysis = new Analysis();
            analysis.setProject(setting.getProject());
            analysis.setAnalysisStatus(AnalysisStatus.ANALYSING);

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
                switch (diff.getModificationType()) {
                case ADD:
                    break;
                case MODIFY:
                    JavaSourceSLO oldSLO = findJavaSourceSLO(diff.getOldPath());

                    break;
                case DELETE:
                    break;
                case RENAME:
                    break;
                case COPY:
                    break;
                }

                diff.getNewPath();
            }
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETE);
        }

    }

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
        return analysisSettingDao.findAllAnalysisSettings();
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long id) {
        return analysisSettingDao.findAnalysisSettingById(id);
    }

    private JavaSourceSLO findJavaSourceSLO(String path) {
        return analysisDao.findJavaSourceSLO(path);
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

        try {
            Analysis analysis = new Analysis();

            analysis.setProject(setting.getProject());
            // This is a "root"-analysis
            analysis.setInitialAnalysis(true);
            // Change branch to the one defined in the setting
            projectService.changeBranch(setting.getProject().getPluginConfiguration().getLocalDirectory(),
                setting.getBranch());

            // Walk all files in the tree and analyse them
            Files.walkFileTree(Paths.get(localDirectory), new InitialAnalysisFileVisitor(setting, analysis));

            // If all OK, add the analysis to DB
            analysisDao.add(analysis);
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

    @Override
    public List<Analysis> listAllAnalyses() {
        return analysisDao.listAll();
    }

    @Override
    public void performAnalysis(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        // do the tango
    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(id);
        analysisSettingDao.remove(setting);
    }

    @Override
    public void triggerAnalysis(Long analysisSettingId) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(analysisSettingId);
        projectService.pullProject(setting.getProject());
        performAnalysis(analysisSettingId);
    }

    @Override
    @Asynchronous
    public Future<Boolean> updateAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.update(setting);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }
}
