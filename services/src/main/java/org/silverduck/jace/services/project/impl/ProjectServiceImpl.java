package org.silverduck.jace.services.project.impl;

import org.apache.commons.io.FileUtils;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.common.properties.JaceProperties;
import org.silverduck.jace.dao.project.ProjectDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.ProjectBranch;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.services.vcs.GitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Pulls projects from configured repositories
 * 
 * @author Iiro Hietala
 */
@Stateless(name = "ProjectPollingServiceEJB")
public class ProjectServiceImpl implements ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);

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
            gitService.cloneRepo(project.getPluginConfiguration().getCloneUrl(),
                    project.getPluginConfiguration().getLocalDirectory(),
                    project.getPluginConfiguration().getUserName(),
                    project.getPluginConfiguration().getPassword());
            for (String branch : gitService.listBranches(project.getPluginConfiguration().getLocalDirectory())) {
                project.addBranch(new ProjectBranch(project, branch));
            }
            break;
        default:
            throw new RuntimeException("Unsupported plugin type encountered when adding project '" + project.getName()
                + "'");
        }

        projectDao.add(project);

        return new AsyncResult<Boolean>(Boolean.TRUE);

    }

    @Override
    public void changeBranch(String localDirectory, String branch, String startPoint) {
        gitService.checkout(localDirectory, branch, startPoint);
    }

    @Override
    public List<Project> findAllProjects() {
        return projectDao.listAllProjects();
    }

    @Override
    public Project findProjectById(Long projectId) {
        return projectDao.findProjectById(projectId);
    }

    /**
     * Pulls a single project from remote repository to locally configured directory
     *
     * @param project
     * @param analysis
     */
    public void pullProject(Project project, Analysis analysis) {

        switch (project.getPluginConfiguration().getPluginType()) {
        case GIT:
            gitService.pull(project, analysis);

            project.removeAllBranches();
            for (String branch : gitService.listBranches(project.getPluginConfiguration().getLocalDirectory())) {
                project.addBranch(new ProjectBranch(project, branch));
            }
            updateProject(project);
            break;
        default:
            throw new RuntimeException("Non-supported plugin was configured for a project '" + project.getName()
                + "' with id '" + project.getId() + "'");
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
