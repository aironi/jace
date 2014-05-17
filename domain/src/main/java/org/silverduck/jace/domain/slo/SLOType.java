package org.silverduck.jace.domain.slo;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * SLO Type TODO: Think about granularity, should the METHOD be elsewhere?
 */
public enum SLOType implements LocalizedEnum {
    SOURCE("SLOType.source"),

    FILE("SLOType.file"),

    REOSURCE("SLOType.resource"),

    METHOD("SLOType.method");

    private final String resourceKey;

    SLOType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return null;
    }
}
