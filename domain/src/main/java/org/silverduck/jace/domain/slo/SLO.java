package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.Feature;

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

    @ManyToOne
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

    @ManyToOne
    @JoinColumn(name = "FeatureRID")
    private Feature feature;

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

    public Analysis getAnalysis() {
        return analysis;
    }

    public Feature getFeature() {
        return feature;
    }

    public String getPath() {
        return path;
    }

    public SLOType getSloType() {
        return sloType;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSloType(SLOType sloType) {
        this.sloType = sloType;
    }
}
