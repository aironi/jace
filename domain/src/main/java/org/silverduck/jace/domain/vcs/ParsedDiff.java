package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * The ParsedDiff contains the lsit of Hunks that relate to a Diff.
 */
@Entity
@Table(name = "ParsedDiff")
public class ParsedDiff extends AbstractDomainObject {

    @OneToOne(mappedBy = "parsedDiff")
    private Diff diff;

    @OneToMany(mappedBy = "parsedDiff", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Hunk> hunks = new ArrayList<Hunk>();

    public void addHunk(Hunk hunk) {
        if (!hunks.contains(hunk)) {
            hunks.add(hunk);
            hunk.setParsedDiff(this);
        }
    }

    public Diff getDiff() {
        return diff;
    }

    public List<Hunk> getHunks() {
        return hunks;
    }

    public void removeHunk(Hunk hunk) {
        if (hunks.contains(hunk)) {
            this.hunks.remove(hunk);
            hunk.setParsedDiff(null);
        }
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }
}
