package org.silverduck.jace.domain.vcs;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;
import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author Iiro Hietala 13.5.2014.
 */

@Embeddable
public class PluginConfiguration {

    @Length(min = 6, max = 512)
    @NotNull
    @URL
    @Column(name = "CloneUrl")
    private String cloneUrl;

    @Column(name = "LocalDirectory")
    private String localDirectory;

    @NotNull
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
