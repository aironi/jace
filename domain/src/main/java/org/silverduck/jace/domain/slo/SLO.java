package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.slo.SLOImport;
import org.silverduck.jace.domain.feature.Feature;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Software Life-cycle Object, SLO. A SLO amy represent a source file, a resource, or any other
 * file that is analysed.
 *
 * The SLOs with SLOType.SOURCE hold a special meaning as they may contain dependencies.
 */
@Entity
@Table(name = "SLO")
@NamedQueries({
        @NamedQuery(name = "findByPath",
                query = "SELECT s FROM SLO s JOIN s.analysis.project p " +
                        "WHERE s.path = :path AND s.sloStatus = org.silverduck.jace.domain.slo.SLOStatus.CURRENT " +
                        "AND p.id = :projectRID " +
                        "ORDER BY s.created DESC"),
        @NamedQuery(name = "findByQualifiedClassName",
                query = "SELECT s FROM SLO s " +
                        "JOIN s.analysis.project p " +
                        "WHERE s.qualifiedClassName = :qualifiedClassName " +
                        "AND s.sloStatus = org.silverduck.jace.domain.slo.SLOStatus.CURRENT " +
                        "AND p.id = :projectRID " +
                        "ORDER BY s.created DESC"),
        @NamedQuery(name = "listSLOs",
                query = "SELECT s FROM SLO s " +
                        "JOIN s.analysis.project p " +
                        "WHERE p.id = :projectRID " +
                        "AND s.sloStatus = org.silverduck.jace.domain.slo.SLOStatus.CURRENT"),
        @NamedQuery(name = "updateStatus",
                query = "UPDATE SLO SET sloStatus = :status WHERE id IN :ids")
})
public class SLO extends AbstractDomainObject {
    @ManyToOne
    @JoinColumn(name = "AnalysisRID")
    private Analysis analysis;

    @Column(name = "ClassName")
    private String className;

    @ManyToMany(mappedBy = "dependsOn")
    private List<SLO> dependantOf = new ArrayList<SLO>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "SLODependencies", joinColumns = { @JoinColumn(name = "SLODependsOnRID") }, inverseJoinColumns = { @JoinColumn(name = "SLODependantOfRID") })
    private List<SLO> dependsOn = new ArrayList<SLO>();

    @OneToMany(mappedBy = "extending", fetch = FetchType.LAZY)
    private List<SLO> extendedBy = new ArrayList<SLO>();

    @ManyToOne
    @JoinColumn(name = "childRID")
    private SLO extending;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "FeatureRID")
    private Feature feature;

    @OneToMany(mappedBy = "slo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JavaMethod> javaMethods = new ArrayList<JavaMethod>();

    @Column(name = "PackageName")
    private String packageName;

    @Column(name = "Path")
    private String path;

    @Column(name = "QualifiedClassName")
    private String qualifiedClassName;

    @OneToMany(mappedBy = "slo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SLOImport> sloImports = new ArrayList<SLOImport>();

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

    public void addDependency(SLO dependency) {
        if (!this.getDependsOn().contains(dependency)) {
            this.getDependsOn().add(dependency);
            this.getDependantOf().add(this);
        }
    }

    public void addMethod(JavaMethod method) {
        if (!this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setSlo(this);
        }
    }

    public void addSLOImport(SLOImport sloImport) {
        if (!this.getSloImports().contains(sloImport)) {
            getSloImports().add(sloImport);
            sloImport.setSlo(this);
        }
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public String getClassName() {
        return className;
    }

    public List<SLO> getDependantOf() {
        return dependantOf;
    }

    public List<SLO> getDependsOn() {
        return dependsOn;
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

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public List<SLOImport> getSloImports() {
        return sloImports;
    }

    public SLOStatus getSloStatus() {
        return sloStatus;
    }

    public SLOType getSloType() {
        return sloType;
    }

    public void removeDependency(SLO slo) {
        if (this.getDependsOn().contains(slo)) {
            this.getDependsOn().remove(slo);
            this.getDependantOf().remove(this);
        }
    }

    public void removeMethod(JavaMethod method) {
        if (this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setSlo(this);
        }
    }

    public void removeSLOImport(SLOImport sloImport) {
        if (this.getSloImports().contains(sloImport)) {
            getSloImports().remove(sloImport);
            sloImport.setSlo(null);
        }
    }

    public void removeSLOImports() {
        for (SLOImport sloImport : getSloImports()) {
            sloImport.setSlo(null);
        }
        getSloImports().clear();
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

    public void setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void setSloStatus(SLOStatus sloStatus) {
        this.sloStatus = sloStatus;
    }

    public void setSloType(SLOType sloType) {
        this.sloType = sloType;
    }

    @Transient
    public void clearDependsOnList() {
        List<SLO> dependsOn = new ArrayList<SLO>(getDependsOn());
        for (SLO slo : dependsOn) {
            slo.removeDependency(slo);
        }
    }
}
