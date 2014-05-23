package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.common.localization.LocalizedEnum;

/**
 * Created by ihietala on 17.5.2014.
 */
public enum AnalysisStatus implements LocalizedEnum {
    COMPLETE("analysisStatus.complete"),

    INITIAL_ANALYSIS("analysisStatus.initialAnalysis"),

    ANALYSING("analysisStatus.analysing"),

    ERROR("analysisStatus.error"),

    DISABLED("analysisStatus.disabled");

    private String resourceKey;

    AnalysisStatus(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }
}
