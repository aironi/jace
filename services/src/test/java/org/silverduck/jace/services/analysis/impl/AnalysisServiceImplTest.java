package org.silverduck.jace.services.analysis.impl;

import org.silverduck.jace.domain.slo.SLO;
import org.apache.commons.io.FileUtils;
import org.apache.openejb.api.LocalClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.silverduck.jace.dao.EjbTestCase;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.domain.vcs.Hunk;
import org.silverduck.jace.domain.vcs.Line;
import org.silverduck.jace.domain.vcs.ModificationType;
import org.silverduck.jace.domain.vcs.ParsedDiff;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Integration tests for Analysis operations
 */
@RunWith(BlockJUnit4ClassRunner.class)
@LocalClient
public class AnalysisServiceImplTest extends EjbTestCase {

    @EJB
    private AnalysisDao analysisDao;

    @EJB
    private AnalysisService analysisService;

    private Project project;

    @EJB
    private ProjectService projectService;

    @After
    public void cleanDirs() {
        try {

            FileUtils.deleteDirectory(new File(project.getPluginConfiguration().getLocalDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
            Assert
                .fail("Couldn't clean up after tests. The subsequent test runs will most probably fail. Please clean up the following directory manually: "
                    + project.getPluginConfiguration().getLocalDirectory());
        }
    }

    @Before
    public void initProject() {
        project = Project.newProject();
    }

    @Test
    public void testInitialAnalysis() throws Exception {

        AnalysisSetting setting = AnalysisSetting.newAnalysisSetting();
        setting.setProject(project);
        setting.setBranch("refs/remotes/origin/master");

        if (projectService.addProject(project).get()) {

            Boolean result = analysisService.addAnalysisSetting(setting).get();
        } else {
            Assert.fail("Could not add a new test project");
        }

    }

    @Test
    public void testListScoredCommitsByRelease() throws ExecutionException, InterruptedException {

        AnalysisSetting setting = AnalysisSetting.newAnalysisSetting();
        setting.setProject(project);
        setting.setBranch("refs/remotes/origin/master");

        if (projectService.addProject(project).get()) {

            Boolean result = analysisService.addAnalysisSetting(setting).get();
            if (result) {
                SLO slo1 = analysisDao.findSLOByQualifiedClassName(
                    "org.silverduck.jace.services.analysis.impl.AnalysisServiceImplTest", project.getId());

                Assert.assertNotNull("SLO was not found", slo1);
                Diff diff = new Diff();
                Commit commit = new Commit();
                commit.setCommitId("Test-1 - Test");
                diff.setCommit(commit);
                diff.setModificationType(ModificationType.MODIFY);
                diff.setOldPath(slo1.getPath());
                diff.setNewPath(slo1.getPath());
                ParsedDiff parsedDiff = new ParsedDiff();
                parsedDiff.setDiff(diff);
                Hunk hunk = new Hunk();
                Line addedLine1 = new Line(110, "String test = \"test\";");
                hunk.addAddedLine(addedLine1);
                parsedDiff.addHunk(hunk);
                diff.setParsedDiff(parsedDiff);

                ChangedFeature cf = new ChangedFeature(slo1.getAnalysis(), slo1.getFeature(), slo1, diff);

                getEntityManager().persist(cf);
                List<ScoredCommit> scoredCommits = analysisService.listScoredCommitsByRelease(project.getId(), "0.2");
                Assert.assertEquals("Wrong amount of scored commits returned", 1, scoredCommits.size());
            }
        }
    }
}
