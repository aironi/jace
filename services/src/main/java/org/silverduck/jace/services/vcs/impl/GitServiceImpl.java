package org.silverduck.jace.services.vcs.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.domain.vcs.Diff;
import org.silverduck.jace.domain.vcs.Hunk;
import org.silverduck.jace.domain.vcs.Line;
import org.silverduck.jace.domain.vcs.ModificationType;
import org.silverduck.jace.domain.vcs.ParsedDiff;
import org.silverduck.jace.services.vcs.GitService;

import javax.ejb.Stateless;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements Plugin interface for git version control system. Uses JGit.
 * 
 * TODO: Consider using JCA since at the moment we're doing File access from EJB which is restricted by spec.
 * 
 * @author Iiro Hietala
 */
@Stateless(name = "GitServiceEJB")
public class GitServiceImpl implements GitService {

    private static final Log LOG = LogFactory.getLog(GitServiceImpl.class);

    /**
     * Checkout a branch to local directory
     * 
     * @param localDirectory
     *            Local directory with existing git repository
     * @param branch
     *            Branch to checkout
     */
    @Override
    public void checkout(String localDirectory, String branch) {
        Repository repository = resolveRepository(localDirectory);
        try {
            if (!branch.equals(repository.getFullBranch())) {
                // Change branch if it differs from current branch
                Git git = new Git(repository);

                String shortName;
                if (branch.contains("/")) {
                    shortName = branch.substring(branch.lastIndexOf("/") + 1);
                } else {
                    shortName = branch; // most probably doesn't ever occur nor is sane in git sense
                }
                try {
                    git.branchCreate().setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint(branch).setName(shortName).call();
                } catch (GitAPIException e) {
                    if (e instanceof RefAlreadyExistsException) {
                        // No worries, the branch already exists locally
                    } else {
                        throw new JaceRuntimeException("Failed to checkout branch '" + branch
                            + "' to local directory '" + localDirectory + "'", e);
                    }
                }

                try {
                    git.checkout().setName(shortName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
                } catch (GitAPIException e) {
                    throw new JaceRuntimeException("Failed to checkout branch '" + branch + "' to local directory ' "
                        + localDirectory + "'", e);
                }

            }
        } catch (IOException e) {
            throw new JaceRuntimeException("Failed to find a git repository in directory ' " + localDirectory + "'", e);
        } finally {
            repository.close();
        }
    }

    /**
     * Clone a git repository from given clone URL to the local directory
     * 
     * @param cloneUrl
     *            URL to be cloned
     * @param localDirectory
     *            Local directory to clone into
     */
    @Override
    public void cloneRepo(String cloneUrl, String localDirectory, String userName, String passWord) {
        try {
            CloneCommand cloneCommand = Git.cloneRepository().setURI(cloneUrl).setDirectory(new File(localDirectory));

            if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passWord)) {
                UsernamePasswordCredentialsProvider credProv = new UsernamePasswordCredentialsProvider(userName,
                    passWord);
                cloneCommand.setCredentialsProvider(credProv);
            }
            // Writer out = new StringWriter();
            // cloneCommand.setProgressMonitor(new TextProgressMonitor(out));
            Git call = cloneCommand.call();

            call.close();
            LOG.info("cloneRepo(): Clone OK: " + cloneUrl);
        } catch (GitAPIException e) {
            throw new JaceRuntimeException("Failed to clone a remote git repository from URI '" + cloneUrl
                + "' into directory ' " + localDirectory + "'", e);
        }
    }

    /**
     * List all REMOTE branches for a local repository
     * 
     * @param localDirectory
     *            Local directory with existing git repository
     * @return List of Branch names in an alphabetically sorted unmodifiable list
     */
    @Override
    public List<String> listBranches(String localDirectory) {
        Repository repository = resolveRepository(localDirectory);

        List<Ref> branchList;
        Git git = new Git(repository);
        try {
            branchList = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        } catch (GitAPIException e) {
            throw new JaceRuntimeException("Failed to fetch a branch list for git repo ' " + localDirectory + "'", e);
        } finally {
            repository.close();
        }

        List<String> branches = new ArrayList<String>();
        for (Ref branch : branchList) {
            branches.add(branch.getName());
        }
        Collections.sort(branches);
        return Collections.unmodifiableList(branches);
    }

    /**
     * Parses a git diff and produces a ParsedDiff object
     * 
     * @param diff
     *            Diff to parse
     * @return
     */
    protected ParsedDiff parseDiff(String diff) {
        ParsedDiff parsedDiff = new ParsedDiff();

        try {
            List<String> diffLines = IOUtils.readLines(new StringReader(diff));
            Hunk hunk = null;
            int oldLineNumber = 0;
            int newLineNumber = 0;
            for (String line : diffLines) {

                if (line.contains("@@")) {
                    // It's a hunk
                    hunk = new Hunk();
                    parsedDiff.addHunk(hunk);

                    // A very fine implementation of
                    // http://www.gnu.org/software/diffutils/manual/diffutils.html#Detailed-Unified
                    Pattern pattern = Pattern.compile("-(\\d+) \\+(\\d+)");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        hunk.setOldStartLine(Integer.parseInt(matcher.group(1)));
                        hunk.setOldLineCount(1);
                        hunk.setNewStartLine(Integer.parseInt(matcher.group(2)));
                        hunk.setNewLineCount(1);
                    } else {
                        pattern = Pattern.compile("-(\\d+),(\\d+) \\+(\\d+)");
                        matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            hunk.setOldStartLine(Integer.parseInt(matcher.group(1)));
                            hunk.setOldLineCount(Integer.parseInt(matcher.group(2)));
                            hunk.setNewStartLine(Integer.parseInt(matcher.group(3)));
                            hunk.setNewLineCount(1);
                        } else {
                            pattern = Pattern.compile("-(\\d+) \\+(\\d+),(\\d+)");
                            matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                hunk.setOldStartLine(Integer.parseInt(matcher.group(1)));
                                hunk.setOldLineCount(1);
                                hunk.setNewStartLine(Integer.parseInt(matcher.group(2)));
                                hunk.setNewLineCount(Integer.parseInt(matcher.group(3)));
                            } else {
                                pattern = Pattern.compile("-(\\d+),(\\d+) \\+(\\d+),(\\d+)");
                                matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    hunk.setOldStartLine(Integer.parseInt(matcher.group(1)));
                                    hunk.setOldLineCount(Integer.parseInt(matcher.group(2)));
                                    hunk.setNewStartLine(Integer.parseInt(matcher.group(3)));
                                    hunk.setNewLineCount(Integer.parseInt(matcher.group(4)));
                                }
                            }
                        }
                    }

                    oldLineNumber = newLineNumber = 0;
                } else if (hunk != null) {
                    // Reading a hunk
                    if (!line.startsWith("-") && !line.startsWith("+")) {
                        newLineNumber++;
                        oldLineNumber++;
                    } else if (line.startsWith("+")) {
                        newLineNumber++;
                        LOG.fatal("Adding line to to hunk: " + hunk);
                        if (hunk != null) {
                            LOG.fatal("hunk.newstartline=" + hunk.getNewStartLine());
                            LOG.fatal("The diff is: " + diff);
                        }
                        hunk.addAddedLine(new Line(hunk.getNewStartLine() + newLineNumber - 1, line));// FIXME: NPE here
                    } else if (line.startsWith("-")) {
                        oldLineNumber++;
                        hunk.addRemovedLine(new Line(hunk.getOldStartLine() + oldLineNumber - 1, line));
                    }
                }
            }

        } catch (IOException e) {
            LOG.fatal("Failed to parse git diff", e);
        }

        return parsedDiff;
    }

    @Override
    public List<Diff> pull(String localDirectory, String userName, String passWord) {
        List<Diff> diffs = new ArrayList<Diff>();
        Repository repository = resolveRepository(localDirectory);

        Git git = new Git(repository);
        try {
            PullCommand pullCommand = git.pull();

            if (!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(passWord)) {
                UsernamePasswordCredentialsProvider credProv = new UsernamePasswordCredentialsProvider(userName,
                    passWord);
                pullCommand.setCredentialsProvider(credProv);
            }

            PullResult pullResult = pullCommand.call();

            if (!pullResult.getMergeResult().getMergeStatus().isSuccessful()) {

                LOG.fatal("Merge was not successful");

            } else {
                // If pull succeeded...
                FetchResult fetchResult = pullResult.getFetchResult();
                Collection<TrackingRefUpdate> trackingRefUpdates = fetchResult.getTrackingRefUpdates();
                // Iterate through all changed references
                String fullBranch = repository.getFullBranch();
                for (TrackingRefUpdate trackingRefUpdate : trackingRefUpdates) {
                    if (!fullBranch.equals(trackingRefUpdate.getRemoteName())) {
                        continue;
                    }

                    // Get old object id and new object id of each changed ref
                    ObjectId oldObjectId = trackingRefUpdate.getOldObjectId();
                    ObjectId newObjectId = trackingRefUpdate.getNewObjectId();
                    Iterable<RevCommit> commits = git.log().addRange(oldObjectId, newObjectId).call();

                    // Only process current branch changes
                    List<RevCommit> revCommits = new ArrayList<RevCommit>();

                    for (RevCommit commit : commits) {
                        revCommits.add(commit);
                    }
                    Collections.reverse(revCommits);
                    ObjectId prevCommitId = oldObjectId;

                    for (RevCommit commit : revCommits) {
                        Commit jaceCommit = new Commit();
                        jaceCommit.setMessage(commit.getFullMessage().toString());
                        // Add these.
                        jaceCommit.setAuthorName(commit.getAuthorIdent().getName());
                        jaceCommit.setAuthorEmail(commit.getAuthorIdent().getEmailAddress());
                        jaceCommit.setAuthorTimeZone(commit.getAuthorIdent().getTimeZone());
                        jaceCommit.setAuthorTimeZoneOffSet(commit.getAuthorIdent().getTimeZoneOffset());
                        jaceCommit.setAuthorDateOfChange(commit.getAuthorIdent().getWhen());

                        // Diff the old and new revisions and iterate the diffs.
                        DiffCommand diffCommand = git.diff().setOldTree(resolveTreeIterator(repository, prevCommitId))
                            .setNewTree(resolveTreeIterator(repository, commit.toObjectId()));
                        List<DiffEntry> diffEntries = diffCommand.call();

                        for (DiffEntry diffEntry : diffEntries) {
                            ByteArrayOutputStream diffOut = new ByteArrayOutputStream();
                            DiffFormatter diffFormatter = new DiffFormatter(diffOut);
                            diffFormatter.setRepository(repository);
                            diffFormatter.format(diffEntry);
                            // Collect changed data and store it in db temporarily until analysis
                            Diff diff = new Diff();
                            diff.setOldPath(diffEntry.getOldPath());
                            diff.setNewPath(diffEntry.getNewPath());
                            diff.setParsedDiff(parseDiff(diffOut.toString())); // FIXME: encoding
                            diff.setModificationType(ModificationType.valueOf(diffEntry.getChangeType().name()));
                            diff.setCommit(jaceCommit);
                            diffs.add(diff);
                        }
                        prevCommitId = commit.toObjectId();
                    }
                }
            }
        } catch (Exception e) {
            throw new JaceRuntimeException("Failed to pull with rebase into directory ' " + localDirectory + "'", e);
        } finally {
            repository.close();
        }

        return diffs;
    }

    /**
     * Resolves the .git dir for a given dir and builds a Repository object
     * 
     * @param localDirectory
     * @return
     */
    private Repository resolveRepository(String localDirectory) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = builder.setGitDir(new File(localDirectory + "/.git")).build();
        } catch (IOException e) {
            throw new JaceRuntimeException("Failed to find a git repository in directory ' " + localDirectory + "'", e);
        }
        return repository;
    }

    private AbstractTreeIterator resolveTreeIterator(Repository repository, ObjectId objectId) throws IOException {
        final CanonicalTreeParser treeParser = new CanonicalTreeParser();
        final ObjectReader objectReader = repository.newObjectReader();
        try {
            treeParser.reset(objectReader, new RevWalk(repository).parseTree(objectId));
            return treeParser;
        } finally {
            objectReader.release();
        }
    }
}
