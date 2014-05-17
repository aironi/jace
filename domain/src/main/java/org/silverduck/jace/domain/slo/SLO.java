package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Software Life-cycle Object Represents a file, a method, a resource, or any
 */
@Entity
@Table(name = "SLO")
public class SLO extends AbstractDomainObject {

    @Column(name = "Path")
    private String path;

    @Column(name = "SLOType")
    @Enumerated(EnumType.STRING)
    private SLOType sloType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

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
