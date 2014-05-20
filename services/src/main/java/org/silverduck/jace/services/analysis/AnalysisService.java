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

    java.util.concurrent.Future<Boolean> addAnalysisSetting(AnalysisSetting setting);

    void analyseProject(@Observes PullingCompleteEvent event);

    List<AnalysisSetting> findAllAnalysisSettings();

    AnalysisSetting findAnalysisSettingById(Long id);

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param setting
     */
    @Asynchronous
    void initialAnalysis(AnalysisSetting setting);

    /**
     * Triggers initial analysis for a given analysis ID (DEVELOPMENT FEATURE)
     * 
     * @param analysisSettingId
     */
    void initialAnalysis(Long analysisSettingId);

    void removeAnalysisSettingById(Long id);

    /**
     * Triggers an analysis for a given analysis ID (DEVELOPMENT FEATURE)
     * 
     * @param analysisSettingId
     */
    void triggerAnalysis(Long analysisSettingId);

    java.util.concurrent.Future<Boolean> updateAnalysisSetting(AnalysisSetting setting);
}
