package org.silverduck.jace.dao.vcs;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Diff;

/**
 * Created by ihietala on 22.5.2014.
 */
public interface DiffDao extends AbstractDao<Diff> {

    void removeAllDiffs(Project project);
}
