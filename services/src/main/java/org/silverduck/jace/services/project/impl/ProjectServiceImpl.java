package org.silverduck.jace.services.project.impl;

import org.silverduck.jace.dao.project.ProjectDao;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.project.ProjectService;
import org.silverduck.jace.services.vcs.GitService;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.List;

/**
 * Pulls projects from configured repositories
 * 
 * @author Iiro Hietala
 */
@Stateless(name = "ProjectPollingServiceEJB")
public class ProjectServiceImpl implements ProjectService {

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
    public void addProject(Project project) {
        projectDao.add(project);
    }

    /**
     * Pulls a single project from remote repository to locally configured directory
     * 
     * @param project
     */
    @Asynchronous
    public void pullProject(Project project) {
        switch (project.getPluginConfiguration().getPluginType()) {
        case GIT:
            gitService.pull(project.getPluginConfiguration().getCloneUrl());
            PullingCompleteEvent event = new PullingCompleteEvent(project);
            pullingCompleteEvent.fire(event);
            break;
        default:
            throw new RuntimeException("Non-supported plugin was configured for a project '" + project.getName()
                + "' with id '" + project.getId() + "'");
        }
    }

    /**
     * Pulls all configured projects from remote repository to locally configured directories
     */
    @Override
    @Schedule(minute = "5")
    public void pullProjects() {
        List<Project> projects = projectDao.findAllProjects();
        for (Project project : projects) {
            projectPollingService.pullProject(project);
        }
    }

    @Override
    public void removeProject(Project project) {
        projectDao.remove(project);
    }

    @Override
    public void updateProject(Project project) {
        projectDao.update(project);
    }

}
