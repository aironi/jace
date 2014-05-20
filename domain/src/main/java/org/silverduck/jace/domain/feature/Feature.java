package org.silverduck.jace.domain.feature;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents a softeare feature
 */
@Entity
@Table(name = "Feature")
public class Feature extends AbstractDomainObject {

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
