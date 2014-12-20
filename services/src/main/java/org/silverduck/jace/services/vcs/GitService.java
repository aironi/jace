package org.silverduck.jace.services.vcs;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.silverduck.jace.domain.vcs.Plugin;

import javax.ejb.Local;

/**
 * @author Iiro Hietala 14.5.2014.
 */
@Local
public interface GitService extends Plugin {

    /**
     * TODO: Remove
     * @param analysisId
     * @param localDirectory
     * @param commit
     * @param previousCommit
     */
    void reset(Long analysisId, String localDirectory, RevCommit commit, ObjectId previousCommit);

    /**
     * TODO: Remove
     * @param repository
     * @param git
     * @return
     */
    Ref resolveCurrentRef(String localDirectory);
}
