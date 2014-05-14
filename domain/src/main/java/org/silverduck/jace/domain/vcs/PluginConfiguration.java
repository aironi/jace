package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Created by ihietala on 13.5.2014.
 */

@Embeddable
public class PluginConfiguration {

    @Column(name = "CloneUrl")
    private String cloneUrl;

    @Column(name = "LocalDirectory")
    private String localDirectory;

    @Column(name = "PluginType")
    @Enumerated(EnumType.STRING)
    private PluginType pluginType;

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }
}
