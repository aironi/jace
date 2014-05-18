package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.slo.JavaSourceSLO;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnalysisSettingRID")
    private AnalysisSetting analysisSetting;

    @Column(name = "AnalysisStatus")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    // Initial Anaysis, i.e. the root of all analyses.
    @Column(name = "InitialAnalysis")
    private Boolean initialAnalysis;

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<JavaSourceSLO> slos = new HashSet<JavaSourceSLO>();

    public Analysis() {
    }

    public void addSLO(JavaSourceSLO slo) {
        if (!slos.contains(slo)) {
            slos.add(slo);
        }
    }

    public Boolean getInitialAnalysis() {
        return initialAnalysis;
    }

    public Set<JavaSourceSLO> getSlos() {
        return Collections.unmodifiableSet(slos);
    }

    public void removeSLO(JavaSourceSLO slo) {
        if (slos.contains(slo)) {
            slos.remove(slo);
        }
    }

    public void setInitialAnalysis(Boolean initialAnalysis) {
        this.initialAnalysis = initialAnalysis;
    }
}
