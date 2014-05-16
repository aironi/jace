package org.silverduck.jace.dao.project;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface ProjectDao extends AbstractDao<Project> {

    List<Project> findAllProjects();

    Project findProjectById(Long projectId);

}
