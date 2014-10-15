package org.silverduck.jace.services.vcs.impl;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverduck.jace.domain.vcs.ParsedDiff;

/**
 * Created by Iiro Hietala on 23.5.2014.
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
        String diff = "diff --git a/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java b/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "index ca6f217..41f82e2 100644\n"
            + "--- a/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "+++ b/testing/src/main/java/org/silverduck/jacetesting/domain/tool/Wrench.java\n"
            + "@@ -7,0 +8 @@\n"
            + "+    // One Line added\n"
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



        ParsedDiff parsedDiff = service.parseDiff(diff);
        Assert.assertEquals("Wrong amount of Hunks", 4, parsedDiff.getHunks().size());
        Assert.assertTrue("Wrong contents in first added line", parsedDiff.getHunks().get(0).getAddedLines().get(0)
            .getLine().contains("One Line"));
        Assert.assertTrue("Wrong contents in second added line", parsedDiff.getHunks().get(1).getAddedLines().get(0)
            .getLine().contains("weight"));
        Assert.assertEquals("Wrong line number in added rows", (int) 12, (int) parsedDiff.getHunks().get(1)
            .getAddedLines().get(0).getLineNumber());

    }

    @Test
    public void testParseDiff_Jace14() {

        String diff = "diff --git a/services/src/main/java/org/silverduck/jace/services/vcs/impl/GitServiceImpl.java b/services/src/main/java/org/silverduck/jace/services/vcs/impl/GitServiceImpl.java\n" +
                "index bae06a9..0527ff7 100644\n" +
                "--- a/services/src/main/java/org/silverduck/jace/services/vcs/impl/GitServiceImpl.java\n" +
                "+++ b/services/src/main/java/org/silverduck/jace/services/vcs/impl/GitServiceImpl.java\n" +
                "@@ -4,32 +4,21 @@\n" +
                " import org.apache.commons.lang3.StringUtils;\n" +
                " import org.apache.commons.logging.Log;\n" +
                " import org.apache.commons.logging.LogFactory;\n" +
                "-import org.eclipse.jgit.api.CloneCommand;\n" +
                "-import org.eclipse.jgit.api.CreateBranchCommand;\n" +
                "-import org.eclipse.jgit.api.DiffCommand;\n" +
                "-import org.eclipse.jgit.api.Git;\n" +
                "-import org.eclipse.jgit.api.ListBranchCommand;\n" +
                "-import org.eclipse.jgit.api.MergeCommand;\n" +
                "-import org.eclipse.jgit.api.MergeResult;\n" +
                "-import org.eclipse.jgit.api.PullCommand;\n" +
                "-import org.eclipse.jgit.api.PullResult;\n" +
                "+import org.eclipse.jgit.api.*;\n" +
                " import org.eclipse.jgit.api.errors.GitAPIException;\n" +
                " import org.eclipse.jgit.api.errors.RefAlreadyExistsException;\n" +
                " import org.eclipse.jgit.diff.DiffEntry;\n" +
                " import org.eclipse.jgit.diff.DiffFormatter;\n" +
                "-import org.eclipse.jgit.errors.MissingObjectException;\n" +
                " import org.eclipse.jgit.lib.ObjectId;\n" +
                " import org.eclipse.jgit.lib.ObjectReader;\n" +
                " import org.eclipse.jgit.lib.Ref;\n" +
                " import org.eclipse.jgit.lib.Repository;\n" +
                "-import org.eclipse.jgit.lib.TextProgressMonitor;\n" +
                " import org.eclipse.jgit.merge.MergeStrategy;\n" +
                "-import org.eclipse.jgit.merge.Merger;\n" +
                " import org.eclipse.jgit.revwalk.RevCommit;\n" +
                "-import org.eclipse.jgit.revwalk.RevSort;\n" +
                "+\n" +
                " import org.eclipse.jgit.revwalk.RevWalk;\n" +
                " import org.eclipse.jgit.storage.file.FileRepositoryBuilder;\n" +
                "-import org.eclipse.jgit.transport.CredentialsProvider;\n" +
                "+\n" +
                " import org.eclipse.jgit.transport.FetchResult;\n" +
                " import org.eclipse.jgit.transport.TrackingRefUpdate;\n" +
                " import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;\n" +
                "@@ -49,8 +38,6 @@\n" +
                " import java.io.File;\n" +
                " import java.io.IOException;\n" +
                " import java.io.StringReader;\n" +
                "-import java.io.StringWriter;\n" +
                "-import java.io.Writer;\n" +
                " import java.util.ArrayList;\n" +
                " import java.util.Collection;\n" +
                " import java.util.Collections;\n" +
                "@@ -69,6 +56,9 @@\n" +
                " public class GitServiceImpl implements GitService {\n" +
                " \n" +
                "     private static final Log LOG = LogFactory.getLog(GitServiceImpl.class);\n" +
                "+\n" +
                "+    private static final String REF_HEAD = \"HEAD\";\n" +
                "+    public static final String HUNK_IDENTIFIER = \"@@\";\n" +
                " \n" +
                "     /**\n" +
                "      * Checkout a branch to local directory\n" +
                "@@ -196,7 +186,7 @@\n" +
                "             int newLineNumber = 0;\n" +
                "             for (String line : diffLines) {\n" +
                " \n" +
                "-                if (line.contains(\"@@\")) {\n" +
                "+                if (line.contains(HUNK_IDENTIFIER)) {\n" +
                "                     // It's a hunk\n" +
                "                     hunk = new Hunk();\n" +
                "                     parsedDiff.addHunk(hunk);\n" +
                "@@ -247,10 +237,10 @@\n" +
                "                         oldLineNumber++;\n" +
                "                     } else if (line.startsWith(\"+\")) {\n" +
                "                         newLineNumber++;\n" +
                "-                        LOG.fatal(\"Adding line to to hunk: \" + hunk);\n" +
                "+                        LOG.debug(\"GitServiceImpl.parseDiff: Adding line to to hunk: \" + hunk);\n" +
                "                         if (hunk != null) {\n" +
                "-                            LOG.fatal(\"hunk.newstartline=\" + hunk.getNewStartLine());\n" +
                "-                            LOG.fatal(\"The diff is: \" + diff);\n" +
                "+                            LOG.debug(\"GitServiceImpl.parseDiff: hunk.newstartline=\" + hunk.getNewStartLine());\n" +
                "+                            LOG.debug(\"GitServiceImpl.parseDiff: The diff is: \" + diff);\n" +
                "                         }\n" +
                "                         hunk.addAddedLine(new Line(hunk.getNewStartLine() + newLineNumber - 1, line));// FIXME: NPE here\n" +
                "                     } else if (line.startsWith(\"-\")) {\n" +
                "@@ -261,98 +251,228 @@\n" +
                "             }\n" +
                " \n" +
                "         } catch (IOException e) {\n" +
                "-            LOG.fatal(\"Failed to parse git diff\", e);\n" +
                "+            throw new IllegalStateException(\"Failed to parse git diff\", e);\n" +
                "         }\n" +
                " \n" +
                "         return parsedDiff;\n" +
                "     }\n" +
                " \n" +
                "-    @Override\n" +
                "-    public List<Diff> pull(String localDirectory, String userName, String passWord) {\n" +
                "-        List<Diff> diffs = new ArrayList<Diff>();\n" +
                "-        Repository repository = resolveRepository(localDirectory);\n" +
                " \n" +
                "+    @Override\n" +
                "+    public List<Diff> pull(String localDirectory, String username, String password) {\n" +
                "+        LOG.info(\"GitServiceImpl.pull() called with localDirectory = \" + localDirectory);\n" +
                "+        Repository repository = resolveRepository(localDirectory);\n" +
                "         Git git = new Git(repository);\n" +
                "+\n" +
                "+        String fullBranchName = resolveFullBranch(repository);\n" +
                "+        Ref oldHead = null;\n" +
                "         try {\n" +
                "+            oldHead = git.getRepository().getRef(fullBranchName);\n" +
                "+        } catch (IOException e) {\n" +
                "+            throw new IllegalStateException(e);\n" +
                "+        }\n" +
                "+        PullResult pullResult = pullInternal(git, username, password);\n" +
                "+\n" +
                "+        LOG.info(\"GitServiceImpl.pull: MergeStatus=\" + pullResult.getMergeResult().getMergeStatus());\n" +
                "+        if (!pullResult.getMergeResult().getMergeStatus().isSuccessful()) {\n" +
                "+            throw new IllegalStateException(\"The merging was not successful when pulling.\");\n" +
                "+        }\n" +
                "+\n" +
                "+        return resolveChanges(git, pullResult, oldHead);\n" +
                "+    }\n" +
                "+\n" +
                "+    private MergeResult mergeInternal(Git git, FetchResult fetchResult) {\n" +
                "+        throw new IllegalStateException(\"Not implemented\");\n" +
                "+        /*\n" +
                "+        LOG.info(\"GitServiceImpl.mergeInternal() called\");\n" +
                "+        MergeCommand mergeCommand = git.merge();\n" +
                "+        mergeCommand.include(\"HEAD^\");\n" +
                "+        mergeCommand.setFastForward(MergeCommand.FastForwardMode.NO_FF);\n" +
                "+        mergeCommand.setStrategy(MergeStrategy.THEIRS);\n" +
                "+        try {\n" +
                "+            return mergeCommand.call();\n" +
                "+        } catch (GitAPIException e) {\n" +
                "+            throw new IllegalStateException(e);\n" +
                "+        }\n" +
                "+        */\n" +
                "+    }\n" +
                "+\n" +
                "+    private FetchResult fetchInternal(Git git, String username, String password) {\n" +
                "+        LOG.info(\"GitServiceImpl.fetchInternal() called\");\n" +
                "+\n" +
                "+        FetchResult fetchResult = null;\n" +
                "+        try {\n" +
                "+\n" +
                "+            FetchCommand fetchCommand = git.fetch();\n" +
                "+\n" +
                "+            if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {\n" +
                "+                UsernamePasswordCredentialsProvider credProv = new UsernamePasswordCredentialsProvider(username,\n" +
                "+                        password);\n" +
                "+                fetchCommand.setCredentialsProvider(credProv);\n" +
                "+            }\n" +
                "+\n" +
                "+            fetchResult = fetchCommand.call();\n" +
                "+        } catch (Exception e) {\n" +
                "+            throw new JaceRuntimeException(\"Failed to fetch into directory ' \" + git.getRepository().getDirectory() + \"'\", e);\n" +
                "+        } finally {\n" +
                "+            git.getRepository().close();\n" +
                "+        }\n" +
                "+        LOG.info(\"GitServiceImpl.fetchInternal: Returning fetchResult.fetchResult: \" + fetchResult != null ? fetchResult : \"null\");\n" +
                "+        return fetchResult;\n" +
                "+    }\n" +
                "+\n" +
                "+    /**\n" +
                "+     * Performs pull operation\n" +
                "+     * @param git\n" +
                "+     * @param userName\n" +
                "+     * @param passWord\n" +
                "+     * @return\n" +
                "+     */\n" +
                "+    private PullResult pullInternal(Git git, String userName, String passWord) {\n" +
                "+        LOG.info(\"GitServiceImpl.pullInternal() called\");\n" +
                "+\n" +
                "+        PullResult pullResult = null;\n" +
                "+        try {\n" +
                "+\n" +
                "             PullCommand pullCommand = git.pull();\n" +
                " \n" +
                "             if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passWord)) {\n" +
                "                 UsernamePasswordCredentialsProvider credProv = new UsernamePasswordCredentialsProvider(userName,\n" +
                "-                    passWord);\n" +
                "+                        passWord);\n" +
                "                 pullCommand.setCredentialsProvider(credProv);\n" +
                "             }\n" +
                " \n" +
                "-            PullResult pullResult = pullCommand.call();\n" +
                "-\n" +
                "-            if (!pullResult.getMergeResult().getMergeStatus().isSuccessful()) {\n" +
                "-\n" +
                "-                LOG.fatal(\"Merge was not successful\");\n" +
                "-\n" +
                "-            } else {\n" +
                "-                // If pull succeeded...\n" +
                "-                FetchResult fetchResult = pullResult.getFetchResult();\n" +
                "-                Collection<TrackingRefUpdate> trackingRefUpdates = fetchResult.getTrackingRefUpdates();\n" +
                "-                // Iterate through all changed references\n" +
                "-                String fullBranch = repository.getFullBranch();\n" +
                "-                for (TrackingRefUpdate trackingRefUpdate : trackingRefUpdates) {\n" +
                "-                    if (!fullBranch.equals(trackingRefUpdate.getRemoteName())) {\n" +
                "-                        continue;\n" +
                "-                    }\n" +
                "-\n" +
                "-                    // Get old object id and new object id of each changed ref\n" +
                "-                    ObjectId oldObjectId = trackingRefUpdate.getOldObjectId();\n" +
                "-                    ObjectId newObjectId = trackingRefUpdate.getNewObjectId();\n" +
                "-                    Iterable<RevCommit> commits = git.log().addRange(oldObjectId, newObjectId).call();\n" +
                "-\n" +
                "-                    // Only process current branch changes\n" +
                "-                    List<RevCommit> revCommits = new ArrayList<RevCommit>();\n" +
                "-\n" +
                "-                    for (RevCommit commit : commits) {\n" +
                "-                        revCommits.add(commit);\n" +
                "-                    }\n" +
                "-                    Collections.reverse(revCommits);\n" +
                "-                    ObjectId prevCommitId = oldObjectId;\n" +
                "-\n" +
                "-                    for (RevCommit commit : revCommits) {\n" +
                "-                        Commit jaceCommit = new Commit();\n" +
                "-                        jaceCommit.setMessage(commit.getFullMessage().toString());\n" +
                "-                        // Add these.\n" +
                "-                        jaceCommit.setAuthorName(commit.getAuthorIdent().getName());\n" +
                "-                        jaceCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());\n" +
                "-                        jaceCommit.setAuthorTimeZone(commit.getAuthorIdent().getTimeZone());\n" +
                "-                        jaceCommit.setAuthorTimeZoneOffSet(commit.getAuthorIdent().getTimeZoneOffset());\n" +
                "-                        jaceCommit.setAuthorDateOfChange(commit.getAuthorIdent().getWhen());\n" +
                "-\n" +
                "-                        // Diff the old and new revisions and iterate the diffs.\n" +
                "-                        DiffCommand diffCommand = git.diff().setOldTree(resolveTreeIterator(repository, prevCommitId))\n" +
                "-                            .setNewTree(resolveTreeIterator(repository, commit.toObjectId()));\n" +
                "-                        List<DiffEntry> diffEntries = diffCommand.call();\n" +
                "-\n" +
                "-                        for (DiffEntry diffEntry : diffEntries) {\n" +
                "-                            ByteArrayOutputStream diffOut = new ByteArrayOutputStream();\n" +
                "-                            DiffFormatter diffFormatter = new DiffFormatter(diffOut);\n" +
                "-                            diffFormatter.setRepository(repository);\n" +
                "-                            diffFormatter.format(diffEntry);\n" +
                "-                            // Collect changed data and store it in db temporarily until analysis\n" +
                "-                            Diff diff = new Diff();\n" +
                "-                            diff.setOldPath(diffEntry.getOldPath());\n" +
                "-                            diff.setNewPath(diffEntry.getNewPath());\n" +
                "-                            diff.setParsedDiff(parseDiff(diffOut.toString())); // FIXME: encoding\n" +
                "-                            diff.setModificationType(ModificationType.valueOf(diffEntry.getChangeType().name()));\n" +
                "-                            diff.setCommit(jaceCommit);\n" +
                "-                            diffs.add(diff);\n" +
                "-                        }\n" +
                "-                        prevCommitId = commit.toObjectId();\n" +
                "-                    }\n" +
                "-                }\n" +
                "-            }\n" +
                "+            pullResult = pullCommand.call();\n" +
                "         } catch (Exception e) {\n" +
                "-            throw new JaceRuntimeException(\"Failed to pull with rebase into directory ' \" + localDirectory + \"'\", e);\n" +
                "+            throw new JaceRuntimeException(\"Failed to pull with rebase into directory ' \" + git.getRepository().getDirectory() + \"'\", e);\n" +
                "         } finally {\n" +
                "-            repository.close();\n" +
                "+            git.getRepository().close();\n" +
                "+        }\n" +
                "+        LOG.info(\"GitServiceImpl.pullInternal: Returning pullResult.fetchResult: \" + pullResult != null ? pullResult.getFetchResult() : \"null\");\n" +
                "+        return pullResult;\n" +
                "+    }\n" +
                "+\n" +
                "+\n" +
                "+    /**\n" +
                "+     * Resolves changes based on pullResult and oldHead ref of the repo.\n" +
                "+     *\n" +
                "+     * @param git\n" +
                "+     * @param pullResult\n" +
                "+     * @param oldHead\n" +
                "+     * @return\n" +
                "+     */\n" +
                "+    private List<Diff> resolveChanges(Git git, PullResult pullResult, Ref oldHead)  {\n" +
                "+        LOG.info(\"GitServiceImpl.resolveChanges() called\");\n" +
                "+        List<Diff> diffs = new ArrayList<Diff>();\n" +
                "+\n" +
                "+        String fullBranch = resolveFullBranch(git.getRepository());\n" +
                "+\n" +
                "+        if (pullResult.getMergeResult().getMergeStatus() == MergeResult.MergeStatus.FAST_FORWARD) {\n" +
                "+            Ref newHead = pullResult.getFetchResult().getAdvertisedRef(fullBranch);\n" +
                "+            LOG.info(\"Old head Ref: \" + oldHead.getName() + \" objectId=\" + oldHead.getObjectId() + \" - New head Ref: Name=\" + newHead.getName() + \" objectId=\" + newHead.getObjectId());\n" +
                "+            List<RevCommit> revCommits = resolveRevCommits(git, oldHead.getObjectId(), newHead.getObjectId());\n" +
                "+            diffs.addAll(resolveDiffs(git, oldHead.getObjectId(), revCommits));\n" +
                "         }\n" +
                " \n" +
                "         return diffs;\n" +
                "+    }\n" +
                "+\n" +
                "+    /**\n" +
                "+     * Resolves Diffs from a list of RevCommits starting from 'startCommitId'.\n" +
                "+     * @param git\n" +
                "+     * @param startCommitId ObjectId to start diffing\n" +
                "+     * @param revCommits RevCommits to iterate\n" +
                "+     * @return\n" +
                "+     */\n" +
                "+    private List<Diff> resolveDiffs(Git git, ObjectId startCommitId, List<RevCommit> revCommits) {\n" +
                "+        LOG.info(\"GitServiceImpl.resolveDiffs() called. startCommitId=\" + startCommitId);\n" +
                "+        List<Diff> diffs = new ArrayList<Diff>();\n" +
                "+\n" +
                "+        for (RevCommit commit : revCommits) {\n" +
                "+            LOG.info(\"GitServiceImpl.resolveDiffs: Creating new Commit\");\n" +
                "+            Commit jaceCommit = new Commit();\n" +
                "+            jaceCommit.setMessage(commit.getFullMessage().toString());\n" +
                "+            // Add these.\n" +
                "+            jaceCommit.setAuthorName(commit.getAuthorIdent().getName());\n" +
                "+            jaceCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());\n" +
                "+            jaceCommit.setAuthorTimeZone(commit.getAuthorIdent().getTimeZone());\n" +
                "+            jaceCommit.setAuthorTimeZoneOffSet(commit.getAuthorIdent().getTimeZoneOffset());\n" +
                "+            jaceCommit.setAuthorDateOfChange(commit.getAuthorIdent().getWhen());\n" +
                "+            LOG.info(\"GitServiceImpl.resolveDiffs: Created new Commit: \" + jaceCommit.toHumanReadable());\n" +
                "+\n" +
                "+            LOG.info(\"GitServiceImpl.resolveDiffs: Diffing old and new versions...\");\n" +
                "+            // Diff the old and new revisions and iterate the diffs.\n" +
                "+            DiffCommand diffCommand = git.diff().setOldTree(resolveTreeIterator(git.getRepository(), startCommitId))\n" +
                "+                    .setNewTree(resolveTreeIterator(git.getRepository(), commit.toObjectId()));\n" +
                "+            try {\n" +
                "+                List<DiffEntry> diffEntries = diffCommand.call();\n" +
                "+                LOG.info(\"GItServiceImpl.resolveDiffs: There are \" + diffEntries.size() + \" diffs. Parsing them...\");\n" +
                "+\n" +
                "+                diffs.addAll(parseDiffEntries(git, jaceCommit, diffEntries));\n" +
                "+                startCommitId = commit.toObjectId();\n" +
                "+            } catch (Exception e) {\n" +
                "+                throw new IllegalStateException(e);\n" +
                "+            }\n" +
                "+        }\n" +
                "+        return diffs;\n" +
                "+    }\n" +
                "+\n" +
                "+    private List<Diff> parseDiffEntries(Git git, Commit jaceCommit, List<DiffEntry> diffEntries) throws IOException {\n" +
                "+        LOG.info(\"GitServiceImpl.parseDiffEntries() called\");\n" +
                "+        List<Diff> diffs = new ArrayList<Diff>();\n" +
                "+        for (DiffEntry diffEntry : diffEntries) {\n" +
                "+            LOG.info(\"GitServiceImpl.parseDiffEntries: Parsing diffEntry: \" + diffEntry.toString());\n" +
                "+            ByteArrayOutputStream diffOut = new ByteArrayOutputStream();\n" +
                "+            DiffFormatter diffFormatter = new DiffFormatter(diffOut);\n" +
                "+            diffFormatter.setRepository(git.getRepository());\n" +
                "+            diffFormatter.format(diffEntry);\n" +
                "+\n" +
                "+            LOG.info(\"GitServiceImpl.parseDIffEntries: Creating new J-ACE Diff\");\n" +
                "+            // Collect changed data and store it in db temporarily until analysis\n" +
                "+            Diff diff = new Diff();\n" +
                "+            diff.setOldPath(diffEntry.getOldPath());\n" +
                "+            diff.setNewPath(diffEntry.getNewPath());\n" +
                "+            diff.setParsedDiff(parseDiff(diffOut.toString())); // FIXME: encoding\n" +
                "+            diff.setModificationType(ModificationType.valueOf(diffEntry.getChangeType().name()));\n" +
                "+            diff.setCommit(jaceCommit);\n" +
                "+            diffs.add(diff);\n" +
                "+        }\n" +
                "+        return diffs;\n" +
                "+    }\n" +
                "+\n" +
                "+    /**\n" +
                "+     * Resolves List of RevCommit objects in chronological order between the given two objectIds.\n" +
                "+     * @param git\n" +
                "+     * @param oldObjectId\n" +
                "+     * @param newObjectId\n" +
                "+     * @return\n" +
                "+     */\n" +
                "+    private List<RevCommit> resolveRevCommits(Git git, ObjectId oldObjectId, ObjectId newObjectId) {\n" +
                "+        LOG.info(\"GitServiceImpl.resolveRevCommits() called. oldObjectId=\" + oldObjectId + \" newObjectId=\" + newObjectId);\n" +
                "+        Iterable<RevCommit> commits = null;\n" +
                "+        try {\n" +
                "+            commits = git.log().addRange(oldObjectId, newObjectId).call();\n" +
                "+        } catch (Exception e) {\n" +
                "+            throw new IllegalStateException(e);\n" +
                "+        }\n" +
                "+\n" +
                "+        // Only process current branch changes\n" +
                "+        List<RevCommit> revCommits = new ArrayList<RevCommit>();\n" +
                "+\n" +
                "+        for (RevCommit commit : commits) {\n" +
                "+            revCommits.add(commit);\n" +
                "+        }\n" +
                "+        Collections.reverse(revCommits);\n" +
                "+        LOG.info(\"Returning \" + revCommits.size() + \" RevCommits\");\n" +
                "+        return revCommits;\n" +
                "+    }\n" +
                "+\n" +
                "+    private String resolveFullBranch(Repository repository) {\n" +
                "+        try {\n" +
                "+            return repository.getFullBranch();\n" +
                "+        } catch (IOException e) {\n" +
                "+            throw new IllegalStateException(e);\n" +
                "+        }\n" +
                "     }\n" +
                " \n" +
                "     /**\n" +
                "@@ -372,11 +492,21 @@\n" +
                "         return repository;\n" +
                "     }\n" +
                " \n" +
                "-    private AbstractTreeIterator resolveTreeIterator(Repository repository, ObjectId objectId) throws IOException {\n" +
                "+    /**\n" +
                "+     * Resolves TreeIterator for a given objectId\n" +
                "+     * @param repository\n" +
                "+     * @param objectId\n" +
                "+     * @return\n" +
                "+     */\n" +
                "+    private AbstractTreeIterator resolveTreeIterator(Repository repository, ObjectId objectId) {\n" +
                "         final CanonicalTreeParser treeParser = new CanonicalTreeParser();\n" +
                "         final ObjectReader objectReader = repository.newObjectReader();\n" +
                "         try {\n" +
                "-            treeParser.reset(objectReader, new RevWalk(repository).parseTree(objectId));\n" +
                "+            try {\n" +
                "+                treeParser.reset(objectReader, new RevWalk(repository).parseTree(objectId));\n" +
                "+            } catch (IOException e) {\n" +
                "+                throw new IllegalStateException(e);\n" +
                "+            }\n" +
                "             return treeParser;\n" +
                "         } finally {\n" +
                "             objectReader.release();";

        ParsedDiff parsedDiff = service.parseDiff(diff);
        Assert.assertEquals("Wrong amount of Hunks", 7, parsedDiff.getHunks().size());
        Assert.assertEquals("Wrong contents in first added line", "+import org.eclipse.jgit.api.*;", parsedDiff.getHunks().get(0).getAddedLines().get(0).getLine());
        Assert.assertEquals("Wrong contents in first removed line", "-import org.eclipse.jgit.api.CloneCommand;", parsedDiff.getHunks().get(0).getRemovedLines().get(0).getLine());

        Assert.assertEquals("Wrong line number in first added row", (int) 7, (int) parsedDiff.getHunks().get(0)
                .getAddedLines().get(0).getLineNumber());

        Assert.assertEquals("Wrong line number in first removed row", (int) 7, (int) parsedDiff.getHunks().get(0)
                .getRemovedLines().get(0).getLineNumber());

    }
}
