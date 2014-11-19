package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(Line.class);

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
        // Turns out some diffs contain null characters in lines (don't ask me)
        if (line.contains("\0")) {
            line = line.replace("\0", "");
            LOG.warn("Removing null characters from line. Result: {}", line);
        }

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
