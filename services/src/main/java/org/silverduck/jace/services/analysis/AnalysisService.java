package org.silverduck.jace.services.analysis;

import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface AnalysisService {

    void addAnalysisSetting(AnalysisSetting setting);

    void analyseProject(@Observes PullingCompleteEvent event);

    void removeAnalysisSettingById(Long id);

    void updateAnalysisSetting(AnalysisSetting setting);

    List<AnalysisSetting> findAllAnalysisSettings();

    AnalysisSetting findAnalysisSettingById(Long id);

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     *
     * @param setting
     */
    @Asynchronous
    void initialAnalysis(AnalysisSetting setting);
}
