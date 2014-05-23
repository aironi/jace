package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ihietala on 23.5.2014.
 */
@Entity
@Table(name = "Hunk")
public class Hunk extends AbstractDomainObject {

    @OneToMany(mappedBy = "hunk", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Line> addedLines = new ArrayList<Line>();

    @Column(name = "NewLineCount")
    private Integer newLineCount;

    @Column(name = "NewStartLine")
    private Integer newStartLine;

    @Column(name = "OldLineCount")
    private Integer oldLineCount;

    @Column(name = "OldStartLine")
    private Integer oldStartLine;

    @ManyToOne
    @JoinColumn(name = "ParsedDiffRID")
    private ParsedDiff parsedDiff;

    @OneToMany(mappedBy = "hunk", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Line> removedLines = new ArrayList<Line>();

    public void addAddedLine(Line line) {
        if (!addedLines.contains(line)) {
            addedLines.add(line);
            line.setHunk(this);
        }
    }

    public void addRemovedLine(Line line) {
        if (!removedLines.contains(line)) {
            removedLines.add(line);
            line.setHunk(this);
        }
    }

    public List<Line> getAddedLines() {
        return addedLines;
    }

    public Integer getNewLineCount() {
        return newLineCount;
    }

    public Integer getNewStartLine() {
        return newStartLine;
    }

    public Integer getOldLineCount() {
        return oldLineCount;
    }

    public Integer getOldStartLine() {
        return oldStartLine;
    }

    public List<Line> getRemovedLines() {
        return removedLines;
    }

    public void removeAddedLine(Line line) {
        if (addedLines.contains(line)) {
            addedLines.remove(line);
            line.setHunk(null);
        }
    }

    public void removeRemovedLine(Line line) {
        if (removedLines.contains(line)) {
            removedLines.remove(line);
            line.setHunk(null);
        }
    }

    public void setNewLineCount(Integer newLineCount) {
        this.newLineCount = newLineCount;
    }

    public void setNewStartLine(Integer newStartLine) {
        this.newStartLine = newStartLine;
    }

    public void setOldLineCount(Integer oldLineCount) {
        this.oldLineCount = oldLineCount;
    }

    public void setOldStartLine(Integer oldStartLine) {
        this.oldStartLine = oldStartLine;
    }

    public ParsedDiff getParsedDiff() {
        return parsedDiff;
    }

    public void setParsedDiff(ParsedDiff parsedDiff) {
        this.parsedDiff = parsedDiff;
    }
}
