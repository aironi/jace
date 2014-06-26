package org.silverduck.jace.domain.feature;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.vcs.Diff;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Represents a Changed Feature. The Granularity may be file- or method based.
 */
@Entity
@Table(name = "ChangedFeature")
@NamedQueries({
        @NamedQuery(name = "findChangedFeaturesByProject", query = "SELECT cf FROM ChangedFeature cf JOIN cf.analysis.project p WHERE p.id = :projectRID"),
        @NamedQuery(name = "findChangedFeaturesByRelease", query = "SELECT cf FROM ChangedFeature cf "
            + "JOIN cf.analysis a WHERE a.releaseVersion = :releaseVersion"),
        @NamedQuery(name = "findFeatureNamesByReleaseVersion", query = "SELECT f.name FROM ChangedFeature cf "
            + " JOIN cf.analysis a JOIN cf.feature f WHERE a.releaseVersion = :releaseVersion GROUP BY f.name"),
        @NamedQuery(name = "findAllCommitIds", query = "SELECT c.commitId FROM ChangedFeature cf "
            + "JOIN cf.analysis.project p JOIN cf.diff.commit c WHERE p.id = :projectRID GROUP BY c.commitId") })
public class ChangedFeature extends AbstractDomainObject {

    @ManyToOne()
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "DiffRID")
    private Diff diff;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FeatureRID")
    private Feature feature;

    /**
     * Method level change
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "MethodRID")
    private JavaMethod method;

    /**
     * File level change
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SLORID")
    private SLO slo;

    public ChangedFeature() {
        super();
    }

    public ChangedFeature(Feature feature, SLO oldSlo, Diff diff) {
        this();
        this.feature = feature;
        this.slo = oldSlo;
        this.diff = diff;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public Diff getDiff() {
        return diff;
    }

    public Feature getFeature() {
        return feature;
    }

    public JavaMethod getMethod() {
        return method;
    }

    public SLO getSlo() {
        return slo;
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public void setDiff(Diff diff) {
        this.diff = diff;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setMethod(JavaMethod method) {
        this.method = method;
    }

    public void setSlo(SLO slo) {
        this.slo = slo;
    }
}
