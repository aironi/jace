package org.silverduck.jace.domain.project;

import org.hibernate.validator.constraints.Length;
import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.vcs.PluginConfiguration;
import org.silverduck.jace.domain.vcs.PluginType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Entity
@Table(name = "Project")
@NamedQueries({ @NamedQuery(name = "findAllProjects", query = "SELECT p from Project p"),
        @NamedQuery(name = "findProjectById", query = "SELECT p from Project p WHERE p.id = :id") })
public class Project extends AbstractDomainObject {

    public static Project newProject() {
        Project p = new Project();

        p.setName("J-Ace");
        p.getPluginConfiguration().setPluginType(PluginType.GIT);
        p.getPluginConfiguration().setCloneUrl("https://github.com/aironi/jace.git");
        p.getPluginConfiguration().setCommitIdPattern("Jace #(\\d+)");
        p.getPluginConfiguration().setLocalDirectory("");
        p.getPluginConfiguration().setUserName("");
        p.getPluginConfiguration().setPassword("");
        p.getReleaseInfo().setVersionFileType(VersionFileType.XML);
        p.getReleaseInfo().setPathToVersionFile("/build/pom.xml");
        p.getReleaseInfo().setPattern("/project/version");
        /*
         * p.setName(""); p.getPluginConfiguration().setPluginType(PluginType.GIT);
         * p.getPluginConfiguration().setCloneUrl(""); p.getPluginConfiguration().setLocalDirectory("");
         * p.getReleaseInfo().setVersionFileType(VersionFileType.XML); p.getReleaseInfo().setPathToVersionFile("");
         * p.getReleaseInfo().setPattern("");
         */
        return p;
    }

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Analysis> analyses = new ArrayList<Analysis>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AnalysisSetting> analysisSetting = new ArrayList<AnalysisSetting>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProjectBranch> branches = new ArrayList<ProjectBranch>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Feature> features = new ArrayList<Feature>();

    // unique = true, since the project name is used as the clone dir under working dir.
    @Column(name = "Name", unique = true)
    @NotNull
    @Length(min = 1, max = 255)
    private String name;

    @Embedded
    private PluginConfiguration pluginConfiguration = new PluginConfiguration();

    @Embedded
    private ReleaseInfo releaseInfo = new ReleaseInfo();

    public Project() {
    }

    public void addAnalysis(Analysis analysis) {
        if (!analyses.contains(analysis)) {
            this.analyses.add(analysis);
            analysis.setProject(this);
        }
    }

    public void addAnalysisSetting(AnalysisSetting setting) {
        if (!analysisSetting.contains(setting)) {
            setting.setProject(this);
            analysisSetting.add(setting);
        }
    }

    public void addBranch(ProjectBranch branch) {
        if (!branches.contains(branch)) {
            branches.add(branch);
            branch.setProject(this);
        }
    }

    public void addFeature(Feature feature) {
        if (!features.contains(feature)) {
            features.add(feature);
            feature.setProject(this);
        }
    }

    public List<Analysis> getAnalyses() {
        return Collections.unmodifiableList(analyses);
    }

    public List<AnalysisSetting> getAnalysisSetting() {
        return Collections.unmodifiableList(analysisSetting);
    }

    public List<ProjectBranch> getBranches() {
        return Collections.unmodifiableList(branches);
    }

    public List<Feature> getFeatures() {
        return Collections.unmodifiableList(features);
    }

    public String getName() {
        return name;
    }

    public PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public void removeAllBranches() {
        List<ProjectBranch> removedBranches = new ArrayList<ProjectBranch>(branches);
        for (ProjectBranch b : removedBranches) {
            removeBranch(b);
        }
    }

    public void removeAnalysis(Analysis analysis) {
        if (analyses.contains(analysis)) {
            this.analyses.remove(analysis);
            analysis.setProject(null);
        }
    }

    public void removeAnalysisSetting(AnalysisSetting setting) {
        if (analysisSetting.contains(setting)) {
            analysisSetting.remove(setting);
            setting.setProject(null);
        }
    }

    public void removeBranch(ProjectBranch branch) {
        if (branches.contains(branch)) {
            branches.remove(branch);
            branch.setProject(null);
        }
    }

    public void removeFeature(Feature feature) {
        if (features.contains(feature)) {
            features.remove(feature);
            feature.setProject(null);

        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPluginConfiguration(PluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }

    public void setReleaseInfo(ReleaseInfo releaseInfo) {
        this.releaseInfo = releaseInfo;
    }
}
