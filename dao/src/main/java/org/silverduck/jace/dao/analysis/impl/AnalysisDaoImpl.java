package org.silverduck.jace.dao.analysis.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOStatus;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

/**
 * Analysis related database operations
 *
 * Created by Iiro Hietala on 18.5.2014.
 */
@Stateless(name = "AnalysisDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AnalysisDaoImpl extends AbstractDaoImpl<Analysis> implements AnalysisDao {

    @Override
    public JavaMethod findMethodByLineNumber(SLO slo, Integer lineNumber) {
        try {
            Query query = getEntityManager().createNamedQuery("findMethodByLineNumber", JavaMethod.class);
            query.setParameter("SLORID", slo.getId());
            query.setParameter("LineNumber", lineNumber);
            return (JavaMethod) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public SLO findSLO(String path, Long projectId) {
        // In this particular case it is possible that entity might not be found.
        try {
            Query query = getEntityManager().createNamedQuery("findByPath", SLO.class);
            query.setParameter("path", path);
            query.setParameter("projectRID", projectId);
            query.setFirstResult(0);
            query.setMaxResults(1);
            return (SLO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public SLO findSLOByQualifiedClassName(String qualifiedClassName, Long projectId) {
        try {
            Query query = getEntityManager().createNamedQuery("findByQualifiedClassName", SLO.class);
            query.setParameter("qualifiedClassName", qualifiedClassName);
            query.setParameter("projectRID", projectId);
            query.setFirstResult(0);
            query.setMaxResults(1);
            return (SLO) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Analysis> listAllAnalyses() {
        Query query = getEntityManager().createNamedQuery("findAllAnalyses", Analysis.class);
        return query.getResultList();
    }

    @Override
    public List<String> listAllCommits(Long projectId) {
        Query query = getEntityManager().createNamedQuery("findAllCommitIds", ChangedFeature.class);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public List<String> listAllReleases(Long projectId) {
        Query query = getEntityManager().createNamedQuery("findAllReleases", Analysis.class);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public List<Analysis> listAnalysesBySetting(Long analysisSettingId) {
        Query query = getEntityManager().createNamedQuery("listAnalysesBySettingId");
        query.setParameter("analysisSettingRID", analysisSettingId);
        return query.getResultList();
    }

    @Override
    public List<ChangedFeature> listChangedFeaturesByProject(Long projectId) {
        Query query = getEntityManager().createNamedQuery("findChangedFeaturesByProject", ChangedFeature.class);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public List<ChangedFeature> listChangedFeaturesByProjectAndRelease(Long projectId, String release) {
        Query query = getEntityManager().createNamedQuery("findChangedFeaturesByRelease", ChangedFeature.class);
        query.setParameter("releaseVersion", release);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public List<String> listChangedFeaturesNamesByRelease(Long projectId, String release) {
        Query query = getEntityManager().createNamedQuery("findFeatureNamesByProjectAndRelease", ChangedFeature.class);
        query.setParameter("releaseVersion", release);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public List<Object[]> listScoredCommitsByProjectAndRelease(Long projectId, String releaseVersion) {
        Query query = getEntityManager().createNamedQuery("findScoredCommitsByProjectAndRelease", ChangedFeature.class);
        query.setParameter("projectRID", projectId);
        query.setParameter("releaseVersion", releaseVersion);
        return query.getResultList();
    }

    @Override
    public List<SLO> listSLOs(Long projectId) {
        Query query = getEntityManager().createNamedQuery("listSLOs", SLO.class);
        query.setParameter("projectRID", projectId);
        return query.getResultList();
    }

    @Override
    public void updateSlosAsDeleted(List<Long> deletedSloIDs) {
        updateSlos(deletedSloIDs, SLOStatus.DELETED);
    }

    @Override
    public void updateSlosAsOld(List<Long> oldSloIDs) {
        updateSlos(oldSloIDs, SLOStatus.OLD);
    }

    private void updateSlos(List<Long> sloIDs, SLOStatus status) {
        Query query = getEntityManager().createNamedQuery("updateStatus", SLO.class);
        query.setParameter("ids", sloIDs);
        query.setParameter("status", status);
        query.executeUpdate();
    }

    @Override
    public void clearDependencies(Long id) {
        List<SLO> slos = listSLOs(id);
        for (SLO slo : slos) {
            slo.clearDependsOnList();
            slo.clearDependantOfList();
            getEntityManager().merge(slo);
        }
    }

}
