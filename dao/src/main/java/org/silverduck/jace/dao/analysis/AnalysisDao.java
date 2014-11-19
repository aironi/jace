package org.silverduck.jace.dao.analysis;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.analysis.Analysis;

import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;

import java.util.List;

/**
 * Created by Iiro Hietala on 18.5.2014.
 */
public interface AnalysisDao extends AbstractDao<Analysis> {

    /**
     * Finds a method by given line number. If an entity is not found returns null.
     * 
     * @param slo
     *            A Software Life-Cycle Object to search for the method
     * @param lineNumber
     *            A line-number in file where to look what method resides at that point
     * @return JavaMethod containing the change, or null if no method may be found.
     */
    JavaMethod findMethodByLineNumber(SLO slo, Integer lineNumber);

    /**
     * Attempts to find a SLO by given path. If an entity is not found returns null.
     * 
     * @param path
     *            Path to use for search
     * @param projectRID
     * @return SLO or null
     */
    SLO findSLO(String path, Long projectRID);

    /**
     * Attempts to find a SLO with a qualified class name
     * 
     * @param qualifiedClassName
     *            Qualified Class name
     * @param projectId
     *            Project Identifier
     * @return If found the SLO, otherwise null
     */
    SLO findSLOByQualifiedClassName(String qualifiedClassName, Long projectId);

    /**
     * Lists all analyses.
     * 
     * @return
     */
    List<Analysis> listAllAnalyses();

    /**
     * List all unique Commit IDs in a project
     * 
     * @param projectId
     * @return
     */
    List<String> listAllCommits(Long projectId);

    /**
     * Lists all analysed releases
     * 
     * @return
     */
    List<String> listAllReleases(Long projectId);

    /**
     * List analyses with a specific analysisSettingId
     * @param analysisSettingId The analysisSettingId to fetch analyses for
     * @return
     */
    List<Analysis> listAnalysesBySetting(Long analysisSettingId);

    /**
     * Lists all changed versions by given release identifier
     * 
     * @param release
     *            Release identifier
     * @return
     */
    List<ChangedFeature> listChangedFeaturesByProjectAndRelease(Long projectID, String release);

    /**
     * Lists unique feature names for changed features in a release
     *
     * @param projectId    Project ID
     *
     * @param release
     *            Release identifier
     * @return
     */
    List<String> listChangedFeaturesNamesByRelease(Long projectId, String release);

    /**
     * Returns a scored list of commits in a list of two-dimension Object where index 0 contains the score and index 1
     * the commit Id
     * 
     * @param projectId
     * @param releaseVersion
     * @return
     */
    List<Object[]> listScoredCommitsByProjectAndRelease(Long projectId, String releaseVersion);

    List<SLO> listSLOs(Long projectId);

    /**
     * Update SLO.sloStatus as SLOStatus.DELETED for given SLO ids
     * 
     * @param deletedSloIDs
     *            SLO IDs to update
     */
    void updateSlosAsDeleted(List<Long> deletedSloIDs);

    /**
     * Update SLO.sloStatus as SLOStatus.OLD for given SLO ids
     * 
     * @param oldSloIDs
     *            SLO IDs to update
     */
    void updateSlosAsOld(List<Long> oldSloIDs);

    void clearDependencies(Long id);

    void addChangedFeature(ChangedFeature changedFeature);


    SLO updateSlo(SLO slo);

    List<SLO> listSLOsForDependencyAnalysis(Long analysisId, int offset, int pageSize);
}
