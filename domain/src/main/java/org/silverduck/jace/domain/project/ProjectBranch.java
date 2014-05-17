package org.silverduck.jace.domain.project;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A branch in a project
 */
@Entity
@Table(name = "ProjectBranch")
public class ProjectBranch extends AbstractDomainObject {

    @Column(name = "Branch")
    private String branch;

    @ManyToOne()
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public ProjectBranch(Project project, String branch) {
        this();
        this.project = project;
        this.branch = branch;
    }

    public ProjectBranch() {
        super();
    }

    public String getBranch() {
        return branch;
    }

    public Project getProject() {
        return project;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
