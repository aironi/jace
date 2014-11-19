package org.silverduck.jace.dao.analysis.impl;

import junit.framework.Assert;
import org.apache.openejb.api.LocalClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.silverduck.jace.dao.EjbTestCase;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOStatus;
import org.silverduck.jace.domain.slo.SLOType;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Iiro Hietala on 24.5.2014.
 */
@RunWith(BlockJUnit4ClassRunner.class)
@LocalClient
public class AnalysisDaoTest extends EjbTestCase {

    @EJB
    private AnalysisDao analysisDao;

    @Test
    public void testFindMethodByLineNumber() throws Exception {
        SLO slo = new SLO("/my/relative/test/path/java.java", SLOType.SOURCE);
        JavaMethod method = new JavaMethod();
        method.setName("testMethod");
        method.setStartLine(100);
        method.setEndLine(200);
        slo.addMethod(method);
        JavaMethod method2 = new JavaMethod();
        method2.setStartLine(202);
        method2.setEndLine(220);
        method2.setSlo(slo);
        method2.setName("testMethod2");
        slo.addMethod(method2);
        getEntityManager().persist(slo); // cascades method

        Assert.assertNotNull("The method was not persisted through cascade", method.getId());
        Assert.assertNotNull("The invalidMethod was not persisted through cascade", method2.getId());

        JavaMethod found = analysisDao.findMethodByLineNumber(slo, 130);
        Assert.assertEquals("Wrong JavaMethod returned", method, found);
        found = analysisDao.findMethodByLineNumber(slo, 202);
        Assert.assertEquals("Wrong JavaMethod returned", method2, found);
    }

    @Test
    public void testFindSlo() throws Exception {
        SLO slo = new SLO("/my/relative/test/path/java.java", SLOType.SOURCE);
        SLO sloInvalid = new SLO("/my/relative/test/path/java2.java", SLOType.SOURCE);
        SLO sloOld = new SLO("/my/relative/test/path/java.java", SLOType.SOURCE);
        sloOld.setSloStatus(SLOStatus.OLD);
        Analysis analysis = new Analysis();
        Project project = Project.newProject();
        analysis.setProject(project);
        slo.setAnalysis(analysis);
        sloInvalid.setAnalysis(analysis);
        sloOld.setAnalysis(analysis);
        getEntityManager().persist(project);
        getEntityManager().persist(analysis);
        getEntityManager().persist(slo);
        getEntityManager().persist(sloInvalid);
        getEntityManager().persist(sloOld);
        SLO found = analysisDao.findSLO("/my/relative/test/path/java.java", project.getId());
        Assert.assertEquals("Wrong SLO returned", slo, found);
    }

    @Test
    public void testListAllReleases() {
        Project project = new Project();
        project.setName("Test project");
        getEntityManager().persist(project);

        Analysis a1 = new Analysis();
        a1.setProject(project);
        a1.setReleaseVersion("1.0");
        Analysis a2 = new Analysis();
        a2.setProject(project);
        a2.setReleaseVersion("1.0");
        Analysis a3 = new Analysis();
        a3.setReleaseVersion("1.1");
        a3.setProject(project);

        getEntityManager().persist(a1);
        getEntityManager().persist(a2);
        getEntityManager().persist(a3);
        List<String> found = analysisDao.listAllReleases(project.getId());
        Assert.assertEquals("Wrong amount of versions returned", 2, found.size());

    }



    @Test
    public void testListChangedFeaturesByRelease() {
        Project project = new Project();
        project.setName("Test");
        getEntityManager().persist(project);

        Feature feature1 = new Feature();
        feature1.setName("Feature 1");
        Feature feature2 = new Feature();
        feature2.setName("Feature 2");
        Feature feature3 = new Feature();
        feature3.setName("Feature 3");
        SLO slo1 = new SLO("/my/path/java.java", SLOType.SOURCE);
        SLO slo2 = new SLO("/my/path/java2.java", SLOType.SOURCE);
        SLO slo3 = new SLO("/my/path/java3.java", SLOType.SOURCE);
        slo1.setFeature(feature1);
        slo2.setFeature(feature1);
        slo3.setFeature(feature2);
        slo1.setSloStatus(SLOStatus.OLD);
        slo2.setSloStatus(SLOStatus.DELETED);
        slo3.setSloStatus(SLOStatus.CURRENT);
        Analysis previousAnalysis = new Analysis();
        previousAnalysis.setProject(project);
        previousAnalysis.setReleaseVersion("1.0");
        previousAnalysis.addSlo(slo1);
        previousAnalysis.addSlo(slo2);
        previousAnalysis.addSlo(slo3);
        ChangedFeature cf1 = new ChangedFeature(previousAnalysis, feature1, slo1, null);
        ChangedFeature cf2 = new ChangedFeature(previousAnalysis, feature1, slo2, null);
        ChangedFeature cf3 = new ChangedFeature(previousAnalysis, feature2, slo3, null);
        previousAnalysis.addChangedFeature(cf1);
        previousAnalysis.addChangedFeature(cf2);
        previousAnalysis.addChangedFeature(cf3);
        getEntityManager().persist(previousAnalysis);

        SLO newSlo1 = new SLO("/my/path/java.java", SLOType.SOURCE);
        SLO newSlo2 = new SLO("/my/path/java2.java", SLOType.SOURCE);
        // third not changed
        Analysis newAnalysis = new Analysis();
        ChangedFeature newCf1 = new ChangedFeature(newAnalysis, feature1, newSlo1, null);
        ChangedFeature newCf2 = new ChangedFeature(newAnalysis, feature1, newSlo2, null);
        newAnalysis.setProject(project);
        newAnalysis.setReleaseVersion("1.1");
        newAnalysis.addSlo(newSlo1);
        newAnalysis.addSlo(newSlo2);
        newAnalysis.addChangedFeature(newCf1);
        newAnalysis.addChangedFeature(newCf2);
        getEntityManager().persist(newAnalysis);

        List<ChangedFeature> list = analysisDao.listChangedFeaturesByProjectAndRelease(project.getId(), "1.1");
        Assert.assertEquals("Wrong amount of features returned", 2, list.size());
        Assert.assertTrue("newCf1 was not contained in list", list.contains(newCf1));
        Assert.assertTrue("newCf2 was not contained in list", list.contains(newCf2));
    }

    @Test
    public void testListChangedFeaturesByReleaseGrouped() {
        Project project = new Project();
        project.setName("Test");
        getEntityManager().persist(project);
        Feature feature1 = new Feature();
        feature1.setName("Feature 1");
        Feature feature2 = new Feature();
        feature2.setName("Feature 2");
        Feature feature3 = new Feature();
        feature3.setName("Feature 3");
        SLO slo1 = new SLO("/my/path/java.java", SLOType.SOURCE);
        SLO slo2 = new SLO("/my/path/java2.java", SLOType.SOURCE);
        SLO slo3 = new SLO("/my/path/java3.java", SLOType.SOURCE);
        slo1.setFeature(feature1);
        slo2.setFeature(feature1);
        slo3.setFeature(feature2);
        slo1.setSloStatus(SLOStatus.OLD);
        slo2.setSloStatus(SLOStatus.DELETED);
        slo3.setSloStatus(SLOStatus.CURRENT);
        Analysis previousAnalysis = new Analysis();
        previousAnalysis.setProject(project);
        previousAnalysis.setReleaseVersion("1.0");
        previousAnalysis.addSlo(slo1);
        previousAnalysis.addSlo(slo2);
        previousAnalysis.addSlo(slo3);
        ChangedFeature cf1 = new ChangedFeature(previousAnalysis, feature1, slo1, null);
        ChangedFeature cf2 = new ChangedFeature(previousAnalysis, feature1, slo2, null);
        ChangedFeature cf3 = new ChangedFeature(previousAnalysis, feature2, slo3, null);
        previousAnalysis.addChangedFeature(cf1);
        previousAnalysis.addChangedFeature(cf2);
        previousAnalysis.addChangedFeature(cf3);
        getEntityManager().persist(previousAnalysis);

        SLO newSlo1 = new SLO("/my/path/java.java", SLOType.SOURCE);
        SLO newSlo2 = new SLO("/my/path/java2.java", SLOType.SOURCE);
        // third not changed
        Analysis newAnalysis = new Analysis();
        ChangedFeature newCf1 = new ChangedFeature(newAnalysis, feature1, newSlo1, null);
        ChangedFeature newCf2 = new ChangedFeature(newAnalysis, feature1, newSlo2, null);
        newAnalysis.setProject(project);
        newAnalysis.setReleaseVersion("1.1");
        newAnalysis.addSlo(newSlo1);
        newAnalysis.addSlo(newSlo2);
        newAnalysis.addChangedFeature(newCf1);
        newAnalysis.addChangedFeature(newCf2);
        getEntityManager().persist(newAnalysis);

        List<String> list = analysisDao.listChangedFeaturesNamesByRelease(previousAnalysis.getProject().getId(), "1.1");
        Assert.assertEquals("Wrong amount of features returned", 1, list.size());
        Assert.assertTrue("Feature 1 was not contained in list", list.contains("Feature 1"));

    }

    @Test
    public void testUpdateSloAsDeleted() {
        SLO slo1 = new SLO("/my/relative/test/path/java.java", SLOType.SOURCE);
        SLO slo2 = new SLO("/my/relative/test/path/java2.java", SLOType.SOURCE);
        SLO slo3 = new SLO("/my/relative/test/path/java3.java", SLOType.SOURCE);
        getEntityManager().persist(slo1);
        getEntityManager().persist(slo2);
        getEntityManager().persist(slo3);

        List<Long> ids = new ArrayList<Long>();
        ids.add(slo1.getId());
        ids.add(slo3.getId());

        analysisDao.updateSlosAsDeleted(ids);
        getEntityManager().refresh(slo1);
        getEntityManager().refresh(slo2);
        getEntityManager().refresh(slo3);
        Assert.assertEquals("SLO 1 was not marked as DELETED", SLOStatus.DELETED, slo1.getSloStatus());
        Assert.assertEquals("SLO 3 was not marked as DELETED", SLOStatus.DELETED, slo3.getSloStatus());
        Assert.assertEquals("SLO 3 was marked as DELETED", SLOStatus.CURRENT, slo2.getSloStatus());
    }

    @Test
    public void testUpdateSloAsOld() {
        SLO slo1 = new SLO("/my/relative/test/path/java.java", SLOType.SOURCE);
        SLO slo2 = new SLO("/my/relative/test/path/java2.java", SLOType.SOURCE);
        SLO slo3 = new SLO("/my/relative/test/path/java3.java", SLOType.SOURCE);
        getEntityManager().persist(slo1);
        getEntityManager().persist(slo2);
        getEntityManager().persist(slo3);

        List<Long> ids = new ArrayList<Long>();
        ids.add(slo1.getId());
        ids.add(slo3.getId());

        analysisDao.updateSlosAsOld(ids);
        getEntityManager().refresh(slo1);
        getEntityManager().refresh(slo2);
        getEntityManager().refresh(slo3);
        Assert.assertEquals("SLO 1 was not marked as OLD", SLOStatus.OLD, slo1.getSloStatus());
        Assert.assertEquals("SLO 3 was not marked as OLD", SLOStatus.OLD, slo3.getSloStatus());
        Assert.assertEquals("SLO 3 was marked as OLD", SLOStatus.CURRENT, slo2.getSloStatus());
    }
}
