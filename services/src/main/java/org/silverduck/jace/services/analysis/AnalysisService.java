package org.silverduck.jace.services.analysis;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.services.analysis.impl.ScoredCommit;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * Perform analysis with the given ID. The analysis must be first created with {@link #createNewAnalysis}
     * @param analysisId The id of the analysis
     *
     */
    void performAnalysis(Long analysisId);

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

    /**
     * Analyse the dependencies of SLOs for an analysis
     * @param analysis
     */
    void analyseDependencies(Analysis analysis);

    /**
     * Analyse a batch of dependencies
     * @param analysis Analysis
     * @param slosToAnalyse The SLOs to analyse dependencies for
     */
    void analyseDependenciesBatch(Analysis analysis, List<SLO> slosToAnalyse);

    /**
     * Add a new Feature (ChangedFeature)
     * @param analysis Analysis
     * @param newSLO The new SLO
     * @param diff The diff where the new feature was found
     */
    void addNewFeature(Analysis analysis, SLO newSLO, Diff diff);

    /**
     * Analyses a batch of diffs.
     * @param analysis Analysis
     * @param diffs Diffs to analyse
     * @param newSLOs A set of SLOs where the new initial SLOs for each change are to be added for later analysis
     * @param deletedSloIDs List of deleted SLO ids
     * @param oldSloIDs List of modified (old) SLO ids
     */
    void analyseDiffsBatch(Analysis analysis, List<Diff> diffs, Set<SLO> newSLOs, List<Long> deletedSloIDs, List<Long> oldSloIDs);

    /**
     * Analyse the file contents of given SLOs
     * @param analysis Analysis
     * @param slos The SLOs to analyse
     */
    void analyseFileContents(Analysis analysis, Set<SLO> slos);

    /**
     * Mark SLO.SLOType.DELETED for the given IDs
     * @param deletedSloIDs IDs to mark
     */
    void markSLOsAsDeleted(List<Long> deletedSloIDs);

    /**
     * Mark SLO.SLOType.OLD for the given IDs
     * @param oldSloIDs IDs to mark
     */
    void markSLOsAsOld(List<Long> oldSloIDs);

    Analysis createNewAnalysis(AnalysisSetting setting);
}
