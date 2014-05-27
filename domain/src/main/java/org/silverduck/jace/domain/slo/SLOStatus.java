package org.silverduck.jace.domain.slo;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Created by ihietala on 27.5.2014.
 */
public enum SLOStatus implements LocalizedEnum {

    CURRENT("sloStatus.current"),

    OLD("sloStatus.old"),

    DELETED("sloStatus.deleted");

    private final String resourceKey;

    private SLOStatus(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }

}
