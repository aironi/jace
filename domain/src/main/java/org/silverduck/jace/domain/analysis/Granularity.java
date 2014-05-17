package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Created by ihietala on 17.5.2014.
 */
public enum Granularity implements LocalizedEnum {
    FILE("granularity.file"),

    METHOD("granularity.method");

    private String resourceKey;

    Granularity(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }

}
