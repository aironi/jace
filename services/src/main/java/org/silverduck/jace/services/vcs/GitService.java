package org.silverduck.jace.services.vcs;

import javax.ejb.Local;
import java.util.List;

/**
 * Created by ihietala on 14.5.2014.
 */
@Local
public interface GitService {
    void checkout(String localDirectory, String branch);

    void cloneRepo(String cloneUrl, String localDirectory);

    List<String> listBranches(String localDirectory);

    void pull(String localDirectory);
}
