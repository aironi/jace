package org.silverduck.jace.services.analysis.impl;

import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    @EJB
    private AnalysisDao analysisDao;

    @Override
    public void addAnalysisSetting(AnalysisSetting setting) {
        analysisDao.add(setting);
    }

    @Override
    public void analyseProject(@Observes PullingCompleteEvent event) {
        throw new RuntimeException("Analysis not yet implemented. Not analysing project "
            + event.getProject().getName());
    }

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
        return analysisDao.findAllAnalysisSettings();
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long id) {
        return analysisDao.findAnalysisSettingById(id);
    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisDao.findAnalysisSettingById(id);
        analysisDao.remove(setting);
    }

    @Override
    public void updateAnalysisSetting(AnalysisSetting setting) {
        analysisDao.update(setting);
    }
}
