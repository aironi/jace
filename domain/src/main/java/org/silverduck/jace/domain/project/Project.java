package org.silverduck.jace.domain.project;

import org.hibernate.validator.constraints.Length;
import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.vcs.PluginConfiguration;
import org.silverduck.jace.domain.vcs.PluginType;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
        p.setName("");
        p.getPluginConfiguration().setPluginType(PluginType.GIT);
        p.getPluginConfiguration().setCloneUrl("");
        p.getPluginConfiguration().setLocalDirectory("");
        p.getReleaseInfo().setVersionFileType(VersionFileType.XML);
        p.getReleaseInfo().setPathToVersionFile("");
        p.getReleaseInfo().setPattern("");
        return p;
    }

    @OneToMany(mappedBy = "project")
    private List<AnalysisSetting> analysisSetting;

    @Column(name = "Name")
    @NotNull
    @Length(min = 1, max = 255)
    private String name;

    @Embedded
    private PluginConfiguration pluginConfiguration = new PluginConfiguration();

    @Embedded
    private ReleaseInfo releaseInfo = new ReleaseInfo();

    public void addAnalysisSetting(AnalysisSetting setting) {
        if (!analysisSetting.contains(setting)) {
            setting.setProject(this);
            ;
            analysisSetting.add(setting);
        }
    }

    public List<AnalysisSetting> getAnalysisSetting() {
        return Collections.unmodifiableList(analysisSetting);
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

    public void removeAnalysisSetting(AnalysisSetting setting) {
        if (analysisSetting.contains(setting)) {
            analysisSetting.remove(setting);
            setting.setProject(null);
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
