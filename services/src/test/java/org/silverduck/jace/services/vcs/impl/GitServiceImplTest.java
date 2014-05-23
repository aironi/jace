package org.silverduck.jace.services.vcs.impl;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverduck.jace.domain.vcs.ParsedDiff;

/**
 * Created by ihietala on 23.5.2014.
 */
public class GitServiceImplTest {
    GitServiceImpl service = new GitServiceImpl();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testParseDiff() throws Exception {
        String diffString = "diff --git a/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java b/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "index ca6f217..41f82e2 100644\n"
            + "--- a/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "+++ b/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "@@ -9,7 +9,7 @@\n"
            + " \n"
            + "     private Long milliMetres;\n"
            + " \n"
            + "-    private String nickName;\n"
            + "+    private Long weight;\n"
            + " \n"
            + "     public String getColor() {\n"
            + "         return color;\n"
            + "@@ -19,8 +19,8 @@\n"
            + "         return milliMetres;\n"
            + "     }\n"
            + " \n"
            + "-    public String getNickName() {\n"
            + "-        return nickName;\n"
            + "+    public Long getWeight() {\n"
            + "+        return weight;\n"
            + "     }\n"
            + " \n"
            + "     public void setColor(String color) {\n"
            + "@@ -31,7 +31,7 @@\n"
            + "         this.milliMetres = milliMetres;\n"
            + "     }\n"
            + " \n"
            + "-    public void setNickName(String nickName) {\n"
            + "-        this.nickName = nickName;\n"
            + "+    public void setWeight(Long weight) {\n" + "+        this.weight = weight;\n" + "     }\n" + " }\n";

        // :D

        ParsedDiff parsedDiff = service.parseDiff(diffString);
        Assert.assertEquals("Wrong amount of Hunks", 3, parsedDiff.getHunks().size());
        Assert.assertTrue("Wrong contents in first added line", parsedDiff.getHunks().get(0).getAddedLines().get(0).getLine().contains("weight"));
        Assert.assertEquals("Wrong line number in added rows", (int)12, (int)parsedDiff.getHunks().get(0).getAddedLines().get(0)
            .getLineNumber());

    }
}
