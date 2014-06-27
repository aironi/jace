package org.silverduck.jace.dao.vcs.impl;

import org.silverduck.jace.dao.AbstractDaoImpl;
import org.silverduck.jace.dao.vcs.DiffDao;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.vcs.Diff;

/**
 * Created by Iiro Hietala on 22.5.2014.
 */
public class DiffDaoImpl extends AbstractDaoImpl<Diff> implements DiffDao {

    @Override
    public void removeAllDiffs(Project project) {
        // TODO: Implement
    }
}
