package org.silverduck.jace.domain.feature;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Represents the possible mapping types.
 *
 * Created by Iiro Hietala on 17.5.2014.
 */
public enum MappingType implements LocalizedEnum {
    PACKAGE_NAME("mappingType.packageName"),

    CONTAINING_DIRECTORY_NAME("mappingType.directoryName"),

    FILE_NAME("mappingType.fileName");


    private final String resourceKey;

    MappingType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return this.resourceKey;
    }
}
