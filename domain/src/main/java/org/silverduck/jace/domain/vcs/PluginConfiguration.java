package org.silverduck.jace.domain.vcs;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    @Column(name = "CommitIdPattern")
    private String commitIdPattern;

    @Column(name = "LocalDirectory")
    private String localDirectory;

    @Column(name = "PassWord")
    private String password;

    @NotNull
    @Column(name = "PluginType")
    @Enumerated(EnumType.STRING)
    private PluginType pluginType;

    @Column(name = "UserName")
    private String userName;

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getCommitIdPattern() {
        return commitIdPattern;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public String getPassword() {
        return password;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public String getUserName() {
        return userName;
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public void setCommitIdPattern(String commitIdPattern) {
        this.commitIdPattern = commitIdPattern;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
