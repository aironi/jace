package org.silverduck.jace.services.vcs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.domain.vcs.Plugin;
import org.silverduck.jace.services.vcs.GitService;

import javax.ejb.Stateless;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implements Plugin interface for git version control system. Uses JGit.
 * 
 * TODO: Consider using JCA since at the moment we're doing File access from EJB which is restricted by spec.
 * 
 * @author Iiro Hietala
 */
@Stateless(name = "GitServiceEJB")
public class GitServiceImpl implements Plugin, GitService {

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
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = builder.setGitDir(new File(localDirectory)).build();
        } catch (IOException e) {
            throw new JaceRuntimeException("Failed to find a git repository in directory ' " + localDirectory + "'", e);
        }

        Git git = new Git(repository);
        try {
            git.checkout().setName(branch).call();
        } catch (GitAPIException e) {
            throw new JaceRuntimeException("Failed to checkout branch '" + branch + "' to local directory ' "
                + localDirectory + "'", e);
        }
        repository.close();
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
    public void cloneRepo(String cloneUrl, String localDirectory) {
        try {
            Git.cloneRepository().setURI(cloneUrl).setDirectory(new File(localDirectory)).call();
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
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = builder.setGitDir(new File(localDirectory + "/.git")).readEnvironment().findGitDir().build();
            LOG.fatal("Full branch (localdir=" + localDirectory + "): " + repository.getFullBranch());
            LOG.fatal("Repo info: " + repository.toString());
            LOG.fatal("All refs: " + repository.getAllRefs());
        } catch (IOException e) {
            throw new JaceRuntimeException("Failed to find a git repository in directory ' " + localDirectory + "'", e);
        }

        List<Ref> branchList;
        Git git = new Git(repository);
        try {
            branchList = git.branchList().call();
            LOG.fatal("Got branch list: " + branchList);
        } catch (GitAPIException e) {
            throw new JaceRuntimeException("Failed to fetch a branch list for git repo ' " + localDirectory + "'", e);
        }

        repository.close();

        List<String> branches = new ArrayList<String>();
        for (Ref branch : branchList) {
            branches.add(branch.getName());
        }
        Collections.sort(branches);
        return Collections.unmodifiableList(branches);
    }

    @Override
    public void pull(String localDirectory) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = builder.setGitDir(new File(localDirectory)).build();
        } catch (IOException e) {
            throw new JaceRuntimeException("Failed to find a git repository in directory ' " + localDirectory + "'", e);
        }

        Git git = new Git(repository);
        try {
            git.pull().setRebase(true).call();
        } catch (GitAPIException e) {
            throw new JaceRuntimeException("Failed to pull with rebase into directory ' " + localDirectory + "'", e);
        }
        repository.close();
    }
}
