package org.silverduck.jace.services.analysis;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.Asynchronous;
import javax.enterprise.event.Observes;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface AnalysisService {

    java.util.concurrent.Future<Boolean> addAnalysisSetting(AnalysisSetting setting);

    java.util.concurrent.Future<Boolean> analyseProject(Long analysisSettingId);

    List<AnalysisSetting> findAllAnalysisSettings();

    Analysis findAnalysisById(Long id);

    AnalysisSetting findAnalysisSettingById(Long id);

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param setting
     */
    void initialAnalysis(Long analysisSettingId);

    /**
     * Lists all analyses performed
     * 
     * @return
     */
    List<Analysis> listAllAnalyses();

    List<String> listAllCommitIds(Long projectId);

    /**
     * Lists all releases that have been analysed
     * 
     * @param projectId
     *            ProjectD to look for
     * @return
     */
    List<String> listAllReleases(Long projectId);

    List<ChangedFeature> listChangedFeaturesByRelease(String release);

    void removeAnalysisSettingById(Long id);

    /**
     * Triggers an analysis for a given analysis ID (DEVELOPMENT FEATURE)
     * 
     * @param analysisSettingId
     */
    void triggerAnalysis(Long analysisSettingId);

    java.util.concurrent.Future<Boolean> updateAnalysisSetting(AnalysisSetting setting);
}
