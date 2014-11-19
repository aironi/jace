package org.silverduck.jace.dao.vcs;

import org.eclipse.jgit.revwalk.RevCommit;
import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.vcs.Commit;
import org.silverduck.jace.domain.vcs.Diff;

import java.util.List;

/**
 * Created by Iiro on 16.11.2014.
 */
public interface DiffDao extends AbstractDao<Diff> {
    List<Diff> listDiffs(Long analysisId, int firstResult, int maxResults);

    void addCommit(Commit commit);

}
