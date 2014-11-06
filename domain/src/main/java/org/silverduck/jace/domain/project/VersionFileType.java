package org.silverduck.jace.domain.project;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Possible types for a version file
 *
 * @author Iiro Hietala 13.5.2014.
 */
public enum VersionFileType implements LocalizedEnum {
    PROPERTIES("versionFileType.properties"),

    XML("versionFileType.xml");

    private String resourceKey;

    VersionFileType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }

}
