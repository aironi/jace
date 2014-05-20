package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.JavaSourceSLO;
import org.silverduck.jace.domain.slo.OtherFileSLO;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an analysis that is performed on a repository. Contains the SLOs (Software Life-Cycle Objects) that are
 * found under the repository.
 */
@Entity
@Table(name = "Analysis")
public class Analysis extends AbstractDomainObject {

    @ManyToOne()
    @JoinColumn(name = "AnalysisSettingRID")
    private AnalysisSetting analysisSetting;

    @Column(name = "AnalysisStatus")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    // Initial Anaysis, i.e. the root of all analyses.
    @Column(name = "InitialAnalysis")
    private Boolean initialAnalysis;

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<JavaSourceSLO> javaSourceSlos = new HashSet<JavaSourceSLO>();

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<OtherFileSLO> otherFileSlos = new HashSet<OtherFileSLO>();

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "ProjectRID", updatable = true)
    private Project project;

    public Analysis() {
    }

    public void addJavaSourceSlo(JavaSourceSLO slo) {
        if (!javaSourceSlos.contains(slo)) {
            javaSourceSlos.add(slo);
            slo.setAnalysis(this);
        }
    }

    public void addOtherFileSLO(OtherFileSLO slo) {
        if (!otherFileSlos.contains(slo)) {
            this.otherFileSlos.add(slo);
            slo.setAnalysis(this);
        }
    }

    public Boolean getInitialAnalysis() {
        return initialAnalysis;
    }

    public Set<JavaSourceSLO> getJavaSourceSlos() {
        return Collections.unmodifiableSet(javaSourceSlos);
    }

    public Project getProject() {
        return project;
    }

    public void removeJavaSourceSlo(JavaSourceSLO slo) {
        if (javaSourceSlos.contains(slo)) {
            javaSourceSlos.remove(slo);
            slo.setAnalysis(null);
        }
    }

    public void removeOtherFileSLO(OtherFileSLO slo) {
        if (otherFileSlos.contains(slo)) {
            this.otherFileSlos.remove(slo);
            slo.setAnalysis(null);
        }
    }

    public void setInitialAnalysis(Boolean initialAnalysis) {
        this.initialAnalysis = initialAnalysis;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
