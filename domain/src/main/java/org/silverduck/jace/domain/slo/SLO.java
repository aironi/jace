package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.Feature;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Software Life-cycle Object Represents a source file, resource, etc.
 */
@Entity
@Table(name = "SLO")
@NamedQueries({
        @NamedQuery(name = "findByPath", query = "SELECT s FROM SLO s JOIN s.analysis.project p WHERE s.path = :path AND s.sloStatus = org.silverduck.jace.domain.slo.SLOStatus.CURRENT AND p.id = :projectRID ORDER BY s.created DESC"),
        @NamedQuery(name = "updateStatus", query = "UPDATE SLO SET sloStatus = :status WHERE id IN :ids") })
public class SLO extends AbstractDomainObject {
    /*
     * Add Imports that this SLO uses Add fully qualified type helper construct a 'ripple' collection (where this SLO is
     * used) -> needs another phase after analysing the SLOs (analyseDependencies to AnalysisService)
     */
    @ManyToOne
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

    @Column(name = "ClassName")
    private String className;

    @ManyToMany(mappedBy = "dependsOn")
    private List<SLO> dependantOf = new ArrayList<SLO>();

    /**
     * File level dependencies for ripple-effects
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "SLODependencies", joinColumns = { @JoinColumn(name = "SLODependsOnRID") }, inverseJoinColumns = { @JoinColumn(name = "SLODependantOfRID") })
    private List<SLO> dependsOn = new ArrayList<SLO>();

    /**
     * This SLO is super class of these SLOs
     */
    @OneToMany(mappedBy = "extending", fetch = FetchType.LAZY)
    private List<SLO> extendedBy = new ArrayList<SLO>();

    /**
     * This SLO is extending superclass
     */
    @ManyToOne
    @JoinColumn(name = "childRID")
    private SLO extending;

    /**
     * This file releates to this feature on file level
     */
    @ManyToOne
    @JoinColumn(name = "FeatureRID")
    private Feature feature;

    @OneToMany(mappedBy = "slo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JavaMethod> javaMethods = new ArrayList<JavaMethod>();

    @Column(name = "PackageName")
    private String packageName;

    @Column(name = "Path")
    private String path;

    @Column(name = "SLOStatus")
    @Enumerated(EnumType.STRING)
    private SLOStatus sloStatus;

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
        this.sloStatus = SLOStatus.CURRENT;
    }

    public void addDependency(SLO slo) {
        if (!this.dependsOn.contains(slo)) {
            this.dependsOn.add(slo);
            this.dependantOf.add(this);
        }
    }

    public void addMethod(JavaMethod method) {
        if (!this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setSlo(this);
        }
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public String getClassName() {
        return className;
    }

    public List<SLO> getExtendedBy() {
        return extendedBy;
    }

    public SLO getExtending() {
        return extending;
    }

    public Feature getFeature() {
        return feature;
    }

    public List<JavaMethod> getJavaMethods() {
        return Collections.unmodifiableList(javaMethods);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPath() {
        return path;
    }

    public SLOStatus getSloStatus() {
        return sloStatus;
    }

    public SLOType getSloType() {
        return sloType;
    }

    public void removeDependency(SLO slo) {
        if (this.dependsOn.contains(slo)) {
            this.dependsOn.remove(slo);
            this.dependantOf.remove(this);
        }
    }

    public void removeMethod(JavaMethod method) {
        if (this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setSlo(this);
        }
    }

    public void setAnalysis(Analysis analysis) {
        this.analysis = analysis;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setExtending(SLO parent) {
        this.extending = parent;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSloStatus(SLOStatus sloStatus) {
        this.sloStatus = sloStatus;
    }

    public void setSloType(SLOType sloType) {
        this.sloType = sloType;
    }
}
