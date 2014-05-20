package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.dao.analysis.AnalysisSettingDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Future;

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

    @Override
    public Future<Boolean> addAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.add(setting);
        analysisService.initialAnalysis(setting);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

    @Override
    public void analyseProject(@Observes PullingCompleteEvent event) {
        throw new RuntimeException("Analysis not yet implemented. Not analysing project "
            + event.getProject().getName());
    }

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
        return analysisSettingDao.findAllAnalysisSettings();
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long id) {
        return analysisSettingDao.findAnalysisSettingById(id);
    }

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param setting
     */
    @Asynchronous
    public void initialAnalysis(AnalysisSetting setting) {
        String localDirectory = setting.getProject().getPluginConfiguration().getLocalDirectory();
        LOG.fatal("localdir: " + localDirectory);
        try {
            Analysis analysis = new Analysis();
            analysis.setInitialAnalysis(true);
            Files.walkFileTree(Paths.get(localDirectory), new InitialAnalysisFileVisitor(setting, analysis));
            analysisDao.add(analysis);
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }

    }

    @Override
    public void initialAnalysis(Long analysisSettingId) {
        analysisService.initialAnalysis(analysisSettingDao.findAnalysisSettingById(analysisSettingId));
    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisSettingDao.findAnalysisSettingById(id);
        analysisSettingDao.remove(setting);
    }

    @Override
    public void triggerAnalysis(Long analysisSettingId) {

    }

    @Override
    public Future<Boolean> updateAnalysisSetting(AnalysisSetting setting) {
        analysisSettingDao.update(setting);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }
}
