package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ihietala on 23.5.2014.
 */
@Entity
@Table(name = "Commit")
public class Commit extends AbstractDomainObject {

    @Column(name = "CommitId")
    private String commitId;

    @OneToMany(mappedBy = "commit", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Diff> diffs = new ArrayList<Diff>();

    @Column(name = "Message")
    private String message;

    private void addDiff(Diff diff) {
        if (!diffs.contains(diff)) {
            diffs.add(diff);
            diff.setCommit(this);
        }
    }

    public String getCommitId() {
        return commitId;
    }

    public List<Diff> getDiffs() {
        return diffs;
    }

    public String getMessage() {
        return message;
    }

    private void removeDiff(Diff diff) {
        if (diffs.contains(diff)) {
            diffs.remove(diff);
            diff.setCommit(null);
        }
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
