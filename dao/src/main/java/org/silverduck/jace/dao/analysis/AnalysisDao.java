package org.silverduck.jace.dao.analysis;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.analysis.Analysis;

import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.vcs.Commit;

import java.util.List;

/**
 * Created by Iiro Hietala on 18.5.2014.
 */
public interface AnalysisDao extends AbstractDao<Analysis> {

    /**
     * Finds a method by given line number. If an entity is not found returns null.
     * 
     * @param slo
     *            SLO A Software Life-Cycle Object to search for the method
     * @param lineNumber
     *            A line-number in file where to look what method resides at that point
     * @return JavaMethod containing the change, or null if no method may be found.
     */
    JavaMethod findMethodByLineNumber(SLO sLO, Integer lineNumber);

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
     * Lists Changed Features by Project
     * 
     * @return
     */
    List<ChangedFeature> listChangedFeaturesByProject(Long projectId);

    /**
     * Lists all changed versions by given release identifier
     * 
     * @param release
     *            Release identifier
     * @return
     */
    List<ChangedFeature> listChangedFeaturesByRelease(String release);

    /**
     * Lists unique feature names for changed features in a release
     * 
     * @param release
     *            Release identifier
     * @return
     */
    List<String> listChangedFeaturesNamesByRelease(String release);

    /**
     * Returns a scored list of commits in a list of two-dimension Object where index 0 contains the score and index 1
     * the commit Id
     * 
     * @param projectId
     * @param releaseVersion
     * @return
     */
    List<Object[]> listScoredCommitsByRelease(Long projectId, String releaseVersion);

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


}
