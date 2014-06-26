package org.silverduck.jace.services.project.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.FileUtils;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.properties.JaceProperties;
import org.silverduck.jace.dao.project.ProjectDao;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.ProjectBranch;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.services.vcs.GitService;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Pulls projects from configured repositories
 * 
 * @author Iiro Hietala
 */
@Stateless(name = "ProjectPollingServiceEJB")
public class ProjectServiceImpl implements ProjectService {

    Log LOG = LogFactory.getLog(ProjectServiceImpl.class);

    @Inject
    Event<AddingProjectCompleteEvent> addingProjectCompleteEvent;

    @EJB
    private GitService gitService;

    @EJB
    private ProjectDao projectDao;

    /**
     * We need this to achieve async. behaviour
     */
    @EJB
    private ProjectService projectPollingService;

    @Inject
    Event<PullingCompleteEvent> pullingCompleteEvent;

    @Override
    @Asynchronous
    public Future<Boolean> addProject(Project project) {
        String targetDir = JaceProperties.getProperty("workingDirectory") + '/' + project.getName();
        project.getPluginConfiguration().setLocalDirectory(targetDir);

        switch (project.getPluginConfiguration().getPluginType()) {
        case GIT:
            LOG.info("addProject(): Cloning git repository...");
            gitService.cloneRepo(project.getPluginConfiguration().getCloneUrl(), project.getPluginConfiguration()
                .getLocalDirectory(), project.getPluginConfiguration().getUserName(), project.getPluginConfiguration()
                .getPassword());
            for (String branch : gitService.listBranches(project.getPluginConfiguration().getLocalDirectory())) {
                project.addBranch(new ProjectBranch(project, branch));
            }
            break;
        default:
            throw new RuntimeException("Unsupported plugin type encountered when adding project '" + project.getName()
                + "'");
        }

        projectDao.add(project);
        // The event-system does NOT work with Vaadin 7.1.5
        // Error:
        // Caused by: java.lang.IllegalStateException: CDI listener identified, but there is no active UI available.
        // at com.vaadin.cdi.internal.UIScopedContext.get(UIScopedContext.java:100)

        // http://dev.vaadin.com/ticket/12393
        //

        // AddingProjectCompleteEvent event = new AddingProjectCompleteEvent(project);
        // addingProjectCompleteEvent.fire(event);
        return new AsyncResult<Boolean>(Boolean.TRUE);

    }

    @Override
    public void changeBranch(String localDirectory, String branch) {
        gitService.checkout(localDirectory, branch);
    }

    @Override
    public List<Project> findAllProjects() {
        return projectDao.findAllProjects();
    }

    @Override
    public Project findProjectById(Long projectId) {
        return projectDao.findProjectById(projectId);
    }

    /**
     * Pulls a single project from remote repository to locally configured directory
     * 
     * @param project
     */
    public List<Diff> pullProject(Project project) {
        List<Diff> diffs = new ArrayList<Diff>();
        switch (project.getPluginConfiguration().getPluginType()) {
        case GIT:
            diffs = gitService.pull(project.getPluginConfiguration().getLocalDirectory(), project
                .getPluginConfiguration().getUserName(), project.getPluginConfiguration().getPassword());

            project.removeAllBranches();
            for (String branch : gitService.listBranches(project.getPluginConfiguration().getLocalDirectory())) {
                project.addBranch(new ProjectBranch(project, branch));
            }
            updateProject(project);
            // Vaadin doesn't support this yet... http://dev.vaadin.com/ticket/12393
            // PullingCompleteEvent event = new PullingCompleteEvent(project);
            // pullingCompleteEvent.fire(event);
            break;
        default:
            throw new RuntimeException("Non-supported plugin was configured for a project '" + project.getName()
                + "' with id '" + project.getId() + "'");
        }
        return diffs;
    }

    /**
     * Pulls all configured projects from remote repository to locally configured directories
     */
    @Override
    // @Schedule(minute = "5")
    public void pullProjects() {
        List<Project> projects = projectDao.findAllProjects();
        for (Project project : projects) {
            projectPollingService.pullProject(project);
        }
    }

    @Override
    public void refresh(Project project) {
        projectDao.refresh(project);
    }

    @Override
    public void removeProject(Project project) {
        try {

            FileUtils.deleteDirectory(new File(project.getPluginConfiguration().getLocalDirectory()));
        } catch (IOException e) {
            LOG.warn("Couldn't clean up local directory '" + project.getPluginConfiguration().getLocalDirectory()
                + "' when removing project '" + project.getName() + "'.\nCause: " + ExceptionHelper.toHumanReadable(e));
        }
        projectDao.remove(project);
    }

    @Override
    public void removeProjectById(Long projectId) {
        removeProject(findProjectById(projectId));
    }

    @Override
    @Asynchronous
    public Future<Boolean> updateProject(Project project) {
        projectDao.update(project);
        return new AsyncResult<Boolean>(Boolean.TRUE);
    }

}
