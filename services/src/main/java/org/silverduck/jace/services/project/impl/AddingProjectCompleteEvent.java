package org.silverduck.jace.services.project.impl;

import org.silverduck.jace.domain.project.Project;

/**
 * Created by ihietala on 18.5.2014.
 */
public class AddingProjectCompleteEvent {
    private Project project;

    public AddingProjectCompleteEvent(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
