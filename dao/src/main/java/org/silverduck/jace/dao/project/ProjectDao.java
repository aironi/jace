package org.silverduck.jace.dao.project;

import org.silverduck.jace.domain.project.Project;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by ihietala on 14.5.2014.
 */
public interface ProjectDao {

    void add(Project project);

    List<Project> findAllProjects();

    void remove(Project project);

    Project update(Project project);
}
