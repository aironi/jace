package org.silverduck.jace.dao.vcs.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.vcs.DiffDao;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.domain.vcs.Diff;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by Iiro on 16.11.2014.
 */
@Stateless(name = "DiffDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class DiffDaoImpl extends AbstractDaoImpl<Diff> implements DiffDao {

    @Override
    public List<Diff> listDiffs(Long analysisId, int firstResult, int maxResults) {
        Query query = getEntityManager().createNamedQuery("listDiffs", Diff.class);
        query.setParameter("analysisId", analysisId);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.getResultList();

    }

    @Override
    public void addCommit(Commit commit) {
        getEntityManager().persist(commit);
        getEntityManager().flush();
        getEntityManager().clear();
    }
}
