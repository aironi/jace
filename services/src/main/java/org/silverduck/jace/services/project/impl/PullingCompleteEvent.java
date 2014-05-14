package org.silverduck.jace.services.project.impl;

import org.silverduck.jace.domain.project.Project;

/**
 * Created by ihietala on 13.5.2014.
 */
public class PullingCompleteEvent {
    private Project project;

    public PullingCompleteEvent(Project project) {

        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
