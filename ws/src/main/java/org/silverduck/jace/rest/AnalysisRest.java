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
@Path("analysis")
@Produces("application/xml")
public class AnalysisRest {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisRest.class);

    @EJB
    private AnalysisService analysisService;

    @GET
    @Path(value = "/scoredCommitsByRelease/{projectId}/{releaseVersion}")
    public List<ScoredCommit> getScoredCommitsByRelease(@PathParam("projectId") Long projectId, @PathParam("releaseVersion") String releaseVersion) {
        LOG.error("getScoredCommitsByRelease({}, {})", projectId, releaseVersion);
        LOG.error("getScoredCommitsByRelease: analysisService is {}", analysisService);
        return analysisService.listScoredCommitsByRelease(projectId, releaseVersion);
    }


}