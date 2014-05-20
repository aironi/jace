package org.silverduck.jace.services.project;

import org.silverduck.jace.domain.project.Project;

import javax.ejb.Schedule;
import java.util.Collection;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface ProjectService {

    java.util.concurrent.Future<Boolean> addProject(Project project);

    List<Project> findAllProjects();

    Project findProjectById(Long projectId);

    void pullProject(Project project);

    void pullProjects();

    void removeProject(Project project);

    void removeProjectById(Long finalItemId);

    java.util.concurrent.Future<Boolean> updateProject(Project project);
}
