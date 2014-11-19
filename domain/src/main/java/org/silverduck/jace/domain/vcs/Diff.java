package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.*;

/**
 * The Diff object represents a single diff of a file.
 */
@Entity
@Table(name = "Diff")
@NamedQueries({
        @NamedQuery(name = "listDiffs",
                query = "SELECT d FROM Diff d JOIN d.analysis a " +
                        "WHERE a.id = :analysisId " +
                        "ORDER BY d.created")
})
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
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

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

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }
}
