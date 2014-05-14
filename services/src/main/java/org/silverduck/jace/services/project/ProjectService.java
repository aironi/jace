package org.silverduck.jace.services.project;

import org.silverduck.jace.domain.project.Project;

import javax.ejb.Schedule;

/**
 * Created by ihietala on 14.5.2014.
 */
public interface ProjectService {

    void addProject(Project project);

    void pullProject(Project project);

    void pullProjects();

    void removeProject(Project project);

    void updateProject(Project project);
}
