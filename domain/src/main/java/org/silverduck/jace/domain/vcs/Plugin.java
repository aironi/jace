package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.project.Project;

import java.util.List;

/**
 * Plugin interface defines the operations to be implemented for each VCS plugin
 *
 * @author Iiro Hietala 13.5.2014.
 */
public interface Plugin {

    void checkout(String localDirectory, String branch, String startPoint);

    /**
     * Clone a repository
     *
     * @param cloneUrl
     *            Remote URL
     * @param localDirectory
     *            Local Directory
     * @param userName
     *            Username (optional, may be null)
     * @param passWord
     *            Password (optional, may be null))
     */
    void cloneRepo(String cloneUrl, String localDirectory, String userName, String passWord);

    /**
     * List all branches in a repository
     * @param localDirectory The local directory where the local repository resides
     * @return List of Strings containing the exact branch names
     */
    List<String> listBranches(String localDirectory);

    /**
     * Performs a 'pull' operation in to a repository. The end result is that most recent changes are
     * updated to the current branch.
     * @param project Project to pull
     * @param analysis
     * @return
     */
    void pull(Project project, Analysis analysis);

}
