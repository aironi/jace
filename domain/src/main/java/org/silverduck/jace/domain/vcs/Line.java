package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents a line in source code that relates to a Hunk.
 *
 * Created by Iiro Hietala on 23.5.2014.
 */
@Entity
@Table(name = "Line")
public class Line extends AbstractDomainObject {

    @ManyToOne
    @JoinColumn(name = "HunkRID")
    private Hunk hunk;

    @Column(name = "Line")
    @Lob
    private String line;

    @Column(name = "LineNumber")
    private Integer lineNumber;

    public Line() {
    }

    public Line(int lineNumber, String line) {
        this();
        this.lineNumber = lineNumber;
        this.line = line;
    }

    public Hunk getHunk() {
        return hunk;
    }

    public String getLine() {
        return line;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setHunk(Hunk hunk) {
        this.hunk = hunk;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
}
