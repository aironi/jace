package org.silverduck.jace.services.analysis;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.services.analysis.impl.ScoredCommit;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface AnalysisService {

    /**
     * Add a new analysis setting
     * @param setting Setting to add
     * @return A Future Boolean that completes once the setting has been added.
     */
    java.util.concurrent.Future<Boolean> addAnalysisSetting(AnalysisSetting setting);

    /**
     * Analyse a project for a given analysis setting id
     * @param analysisSettingId The analysisSettingId that defines the analysis to be performed
     * @return A Future Boolean that completes once the analysis has been performed
     */
    java.util.concurrent.Future<Boolean> analyseProject(Long analysisSettingId);

    /**
     * Returns all analysis settings
     * @return
     */
    List<AnalysisSetting> listAllAnalysisSettings();

    /**
     * Finds an analysis with given id.
     * @param id The identifier of the analysis to be searched
     * @return Returns the analysis, or null if analysis may not be found
     */
    Analysis findAnalysisById(Long id);

    /**
     * Finds an analysis setting with given id.
     * @param id
     * @return Returns the analysis setting, or null if the analysis setting may not be found.
     */
    AnalysisSetting findAnalysisSettingById(Long id);

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param setting
     */
    void initialAnalysis(Long analysisSettingId);

    List<ScoredCommit> listScoredCommitsByRelease(Long projectId, String releaseVersion);

    /**
     * Removes an analysis setting and all related information.
     * @param id ID of the analysis setting to remove
     */
    void removeAnalysisSettingById(Long id);

    /**
     * Triggers an analysis for a given analysis ID (DEVELOPMENT FEATURE)
     * 
     * @param analysisSettingId
     */
    void triggerAnalysis(Long analysisSettingId);

    /**
     * Update an analysis setting
     * @param setting
     * @return
     */
    java.util.concurrent.Future<Boolean> updateAnalysisSetting(AnalysisSetting setting);

    /**
     * Lists all analyses.
     *
     * @return
     */
    List<Analysis> listAllAnalyses();

    /**
     * Lists all analysed releases
     *
     * @return
     */
    List<String> listAllReleases(Long projectId);

}
