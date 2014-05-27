package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.feature.ChangedFeature;
import org.silverduck.jace.domain.project.Project;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.silverduck.jace.domain.slo.SLO;

/**
 * Represents an analysis that is performed on a repository. Contains the SLOs (Software Life-Cycle Objects) that are
 * found under the repository.
 */
@Entity
@Table(name = "Analysis")
@NamedQueries({
        @NamedQuery(name = "findAllAnalyses", query = "SELECT a FROM Analysis a ORDER BY a.created DESC"),
        @NamedQuery(name = "findAllReleases", query = "SELECT a.releaseVersion FROM Analysis a JOIN a.project p WHERE p.id = :projectRID "
            + "GROUP BY a.releaseVersion") })
public class Analysis extends AbstractDomainObject {

    @ManyToOne()
    @JoinColumn(name = "AnalysisSettingRID")
    private AnalysisSetting analysisSetting;

    @Column(name = "AnalysisStatus")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChangedFeature> changedFeatures = new ArrayList<ChangedFeature>();

    // Initial Anaysis, i.e. the root of all analyses. TODO: Rename to baseAnalysis
    @Column(name = "InitialAnalysis")
    private Boolean initialAnalysis;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "ProjectRID", updatable = true)
    private Project project;

    @Column(name = "ReleaseVersion")
    private String releaseVersion;

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<SLO> slos = new HashSet<SLO>();

    public Analysis() {
    }

    public void addChangedFeature(ChangedFeature changedFeature) {
        if (!changedFeatures.contains(changedFeature)) {
            changedFeatures.add(changedFeature);
            changedFeature.setAnalysis(this);
        }
    }

    public void addSlo(SLO slo) {
        if (!slos.contains(slo)) {
            slos.add(slo);
            slo.setAnalysis(this);
        }
    }

    public AnalysisStatus getAnalysisStatus() {
        return analysisStatus;
    }

    public List<ChangedFeature> getChangedFeatures() {
        return Collections.unmodifiableList(changedFeatures);
    }

    public Boolean getInitialAnalysis() {
        return initialAnalysis;
    }

    public Project getProject() {
        return project;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public Set<SLO> getSlos() {
        return Collections.unmodifiableSet(slos);
    }

    public void removeChangedFeature(ChangedFeature changedFeature) {
        if (changedFeatures.contains(changedFeature)) {
            changedFeatures.remove(changedFeature);
            changedFeature.setAnalysis(null);
        }
    }

    public void removeSlo(SLO slo) {
        if (slos.contains(slo)) {
            slos.remove(slo);
            slo.setAnalysis(null);
        }
    }

    public void setAnalysisStatus(AnalysisStatus analysisStatus) {
        this.analysisStatus = analysisStatus;
    }

    public void setInitialAnalysis(Boolean initialAnalysis) {
        this.initialAnalysis = initialAnalysis;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
}
