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
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;
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
    public SLO findSLOByQualifiedClassName(String qualifiedClassName, Long projectId, Date committedBefore) {
        try {
            Query query = getEntityManager().createNamedQuery("findByQualifiedClassName", SLO.class);
            query.setParameter("qualifiedClassName", qualifiedClassName);
            query.setParameter("projectRID", projectId);
            query.setParameter("committedBefore", committedBefore);
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
        getEntityManager().flush();
        getEntityManager().clear();
    }

    @Override
    public void addChangedFeature(ChangedFeature changedFeature) {
        EntityManager em = super.getEntityManager();
        if (em == null) {
            throw new RuntimeException("The EntityManager was null!");
        }
        if (changedFeature.getId() == null) {
            em.persist(changedFeature);
        } else {
            em.merge(changedFeature);
        }
        em.flush();
        em.clear();
    }

    @Override
    public SLO updateSlo(SLO slo) {
        SLO s;
        if (slo.getId() == null) {
            getEntityManager().persist(slo);
            s = slo;
        } else {
            s = getEntityManager().merge(slo);
        }
        getEntityManager().flush();
        getEntityManager().clear();
        return s;
    }

    @Override
    public List<SLO> listSLOsForDependencyAnalysis(Long analysisId, int offset, int pageSize) {
        Query query = getEntityManager().createNamedQuery("listSLOsForDependencyAnalysis", SLO.class);
        query.setParameter("analysisID", analysisId);
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }


}
