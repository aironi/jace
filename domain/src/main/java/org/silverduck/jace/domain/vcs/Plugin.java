package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.project.Project;

import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
public interface Plugin {

    void checkout(String localDirectory, String branch);

    void cloneRepo(String cloneUrl, String localDirectory);

    List<String> listBranches(String localDirectory);

    List<Diff> pull(String localDirectory);

}
