package org.silverduck.jace.domain.analysis;

import org.hibernate.validator.constraints.Length;
import org.silverduck.jace.domain.AbstractDomainObject;
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
import javax.persistence.Table;

/**
 * @author Iiro Hietala 16.5.2014.
 */
@Entity
@Table(name = "AnalysisSetting")
@NamedQueries({ @NamedQuery(name = "findAllAnalysisSettings", query = "SELECT s FROM AnalysisSetting s"),
        @NamedQuery(name = "findAnalysisSettingById", query = "SELECT s FROM AnalysisSetting s WHERE s.id = :id") })
public class AnalysisSetting extends AbstractDomainObject {

    public static AnalysisSetting newAnalysisSetting() {
        AnalysisSetting setting = new AnalysisSetting();
        setting.setBranch("");
        setting.setAutomaticFeatureMapping(true);
        setting.setEnabled(true);
        return setting;
    }

    @Column(name = "AutomaticFeatureMapping")
    private Boolean automaticFeatureMapping;

    @Length(min = 1, max = 500)
    @Column(name = "Branch")
    private String branch;

    @Column(name = "Enabled")
    private Boolean enabled;

    @Column(name = "Granularity")
    @Enumerated(EnumType.STRING)
    private Granularity granularity;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public Boolean getAutomaticFeatureMapping() {
        return automaticFeatureMapping;
    }

    public String getBranch() {
        return branch;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    public Project getProject() {
        return project;
    }

    public void setAutomaticFeatureMapping(Boolean automaticFeatureMapping) {
        this.automaticFeatureMapping = automaticFeatureMapping;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setGranularity(Granularity granularity) {
        this.granularity = granularity;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
