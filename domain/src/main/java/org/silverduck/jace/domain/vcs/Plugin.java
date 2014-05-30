package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.project.Project;

import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
public interface Plugin {

    void checkout(String localDirectory, String branch);

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

    List<String> listBranches(String localDirectory);

    List<Diff> pull(String localDirectory);

}
