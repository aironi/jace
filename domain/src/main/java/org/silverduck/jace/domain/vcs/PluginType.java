package org.silverduck.jace.domain.vcs;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Created by ihietala on 13.5.2014.
 */
public enum PluginType implements LocalizedEnum {
    GIT("plugintype.git");

    private final String resourceKey;

    PluginType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }

}
