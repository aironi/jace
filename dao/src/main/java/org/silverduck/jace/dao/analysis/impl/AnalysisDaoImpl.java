package org.silverduck.jace.dao.analysis.impl;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.Analysis;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by ihietala on 18.5.2014.
 */
@Stateless(name = "AnalysisDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AnalysisDaoImpl extends AbstractDaoImpl<Analysis> implements AnalysisDao {

    @Override
    public List<Analysis> listAll() {
        Query query = getEntityManager().createNamedQuery("findAllAnalyses");
        return query.getResultList();
    }
}
