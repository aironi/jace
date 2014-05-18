package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 * Software Life-cycle Object Represents a file, a method, a resource, or any
 */
@MappedSuperclass
public abstract class SLO extends AbstractDomainObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

    @Column(name = "Path")
    private String path;

    @Column(name = "SLOType")
    @Enumerated(EnumType.STRING)
    private SLOType sloType;

    public SLO() {
        super();
    }

    public SLO(String path, SLOType type) {
        this();
        this.path = path;
        this.sloType = type;
    }

    public String getPath() {
        return path;
    }

    public SLOType getSloType() {
        return sloType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSloType(SLOType sloType) {
        this.sloType = sloType;
    }

}
