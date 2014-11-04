package org.silverduck.jace.services.project;

import org.silverduck.jace.domain.feature.FeatureMapping;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Diff;

import javax.ejb.Schedule;
import java.util.Collection;
import java.util.List;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public interface ProjectService {

    /**
     * Add a Project and clone repo to local directory.
     * 
     * @param project
     *            Projec to Add
     * @return Future<Boolean> representing the result
     */
    java.util.concurrent.Future<Boolean> addProject(Project project);

    /**
     * Change a branch in repository for a project.
     */
    void changeBranch(String localDirectory, String branch);

    /**
     * Find All Projects
     * 
     * @return All projects
     */
    List<Project> findAllProjects();

    /**
     * Find Project by unique Identifier
     * 
     * @param projectId
     *            Unique Identifier of the project
     * @return Project or null
     */
    Project findProjectById(Long projectId);

    /**
     * Pull a project from repository
     * 
     * @param project
     *            Project to pull
     */
    List<Diff> pullProject(Project project);

    /**
     * Pull all proejcts from repositories
     */
    void pullProjects();

    /**
     * Refresh Project
     * 
     * @param project
     *            Project to Refresh
     */
    void refresh(Project project);

    /**
     * Remove a project.
     * 
     * @param project
     */
    void removeProject(Project project);

    /**
     * Remove a project by Unique Identifier
     * 
     * @param projectId
     *            Unique Identifier of the project
     */
    void removeProjectById(Long projectId);

    /**
     * Update project data
     * 
     * @param project
     *            Project with updated data
     * @return
     */
    java.util.concurrent.Future<Boolean> updateProject(Project project);

}
