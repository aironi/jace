package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A object representing a single diff in repository.
 */
@Entity
@Table(name = "Diff")
public class Diff extends AbstractDomainObject {

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "CommitRID")
    private Commit commit;

    @Column(name = "ModificationType")
    @Enumerated
    private ModificationType modificationType;

    @Column(name = "NewPath")
    private String newPath;

    @Column(name = "OldPath")
    private String oldPath;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ParsedDiffRID")
    private ParsedDiff parsedDiff;

    @ManyToOne
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public Commit getCommit() {
        return commit;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getOldPath() {
        return oldPath;
    }

    public ParsedDiff getParsedDiff() {
        return parsedDiff;
    }

    public Project getProject() {
        return project;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public void setParsedDiff(ParsedDiff diff) {
        this.parsedDiff = diff;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
