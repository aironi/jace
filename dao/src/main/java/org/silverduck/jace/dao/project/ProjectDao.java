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

    /**
     * Lists all projects
     * @return
     */
    List<Project> listAllProjects();

    /**
     * Finds a project by specific ID. Returns null no project is found.
     * @param projectId
     * @return
     */
    Project findProjectById(Long projectId);

}
