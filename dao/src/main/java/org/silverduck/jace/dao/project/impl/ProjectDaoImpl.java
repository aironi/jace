package org.silverduck.jace.dao.project.impl;

import org.silverduck.jace.dao.project.ProjectDao;
import org.silverduck.jace.domain.project.Project;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by ihietala on 13.5.2014.
 */
@Stateless(name = "ProjectDaoEJB")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ProjectDaoImpl extends AbstractDaoImpl<Project> implements ProjectDao {

    @Override
    public List<Project> findAllProjects() {
        Query query = getEntityManager().createNamedQuery("findAllProjects");
        return query.getResultList();
    }

}
