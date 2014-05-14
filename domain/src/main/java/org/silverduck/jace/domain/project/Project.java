package org.silverduck.jace.domain.project;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.vcs.PluginConfiguration;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by ihietala on 13.5.2014.
 */
@Entity
@Table(name = "Project")
@NamedQueries({ @NamedQuery(name = "findAllProjects", query = "SELECT p from Project p"),
        @NamedQuery(name = "findProjectByName", query = "SELECT p from Project p WHERE p.name = :Name") })
public class Project extends AbstractDomainObject {

    @Column(name = "Name")
    private String name;

    @Embedded
    private PluginConfiguration pluginConfiguration = new PluginConfiguration();

    @Embedded
    private ReleaseInfo releaseInfo = new ReleaseInfo();

    public String getName() {
        return name;
    }

    public PluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
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
