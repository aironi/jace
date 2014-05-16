package org.silverduck.jace.services.project;

import org.silverduck.jace.domain.project.Project;

import javax.ejb.Schedule;
import java.util.Collection;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface ProjectService {

    void addProject(Project project);

    List<Project> findAllProjects();

    Project findProjectById(Long projectId);

    void pullProject(Project project);

    void pullProjects();

    void removeProject(Project project);

    void removeProjectById(Long finalItemId);

    void updateProject(Project project);
}
