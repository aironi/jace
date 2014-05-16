package org.silverduck.jace.dao.analysis.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Iiro Hietala 17.5.2014.
 */
@Stateless(name = "AnalysisDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AnalysisDaoImpl extends AbstractDaoImpl<AnalysisSetting> implements AnalysisDao {

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
