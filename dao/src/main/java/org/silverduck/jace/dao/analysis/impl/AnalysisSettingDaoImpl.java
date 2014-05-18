package org.silverduck.jace.dao.analysis.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.analysis.AnalysisSettingDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Iiro Hietala 17.5.2014.
 */
@Stateless(name = "AnalysisSettingDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AnalysisSettingDaoImpl extends AbstractDaoImpl<AnalysisSetting> implements AnalysisSettingDao {

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
        Query query = getEntityManager().createNamedQuery("findAllAnalysisSettings");
        return query.getResultList();
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long analysisSettingId) {
        Query query = getEntityManager().createNamedQuery("findAnalysisSettingById");
        query.setParameter("id", analysisSettingId);
        return (AnalysisSetting) query.getSingleResult();
    }
}
