package org.silverduck.jace.dao.project.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.project.ProjectDao;
import org.silverduck.jace.domain.project.Project;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "ProjectDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ProjectDaoImpl extends AbstractDaoImpl<Project> implements ProjectDao {

    @Override
    public List<Project> findAllProjects() {
        Query query = getEntityManager().createNamedQuery("findAllProjects");
        return query.getResultList();
    }

    @Override
    public Project findProjectById(Long projectId) {
        Query query = getEntityManager().createNamedQuery("findProjectById");
        query.setParameter("id", projectId);
        return (Project) query.getSingleResult();
    }

}
