package org.silverduck.jace.rest;

import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.analysis.impl.ScoredCommit;
import org.silverduck.jace.services.project.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@Stateless
@Path("projects")
@Produces("application/xml")
public class ProjectsRest {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectsRest.class);

    @EJB
    private ProjectService projectService;

    @GET
    public List<Project> getProjects() {
        LOG.info("getProjects() called");
        return projectService.findAllProjects();
    }


    @POST
    @Path(value = "/add")
    public void addProject(Project project) {
        projectService.addProject(project);
    }

    @PUT
    @Path(value = "/update")
    public void updateProject(Project project) {
        projectService.updateProject(project);
    }

    @DELETE
    @Path(value = "/delete/{id}")
    public void deleteProject(@PathParam("id") Long id) {
        projectService.removeProjectById(id);
    }

}