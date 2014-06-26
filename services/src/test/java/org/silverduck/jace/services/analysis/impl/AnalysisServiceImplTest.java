package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.io.FileUtils;
import org.apache.openejb.api.LocalClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.silverduck.jace.common.exception.ExceptionHelper;
import org.silverduck.jace.dao.EjbTestCase;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.ProjectService;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;

/**
 * Integration tests for Analysis operations
 */
@RunWith(BlockJUnit4ClassRunner.class)
@LocalClient
public class AnalysisServiceImplTest extends EjbTestCase {

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
}
