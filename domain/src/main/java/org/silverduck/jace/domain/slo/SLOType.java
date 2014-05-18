package org.silverduck.jace.domain.slo;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * SLO Type
 */
public enum SLOType implements LocalizedEnum {
    SOURCE("SLOType.source"),

    OTHER_FILE("SLOType.file"),

    RESOURCE("SLOType.resource");

    private final String resourceKey;

    SLOType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return null;
    }
}
