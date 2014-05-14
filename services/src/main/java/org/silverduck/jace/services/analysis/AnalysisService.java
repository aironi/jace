package org.silverduck.jace.services.analysis;

import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.enterprise.event.Observes;

/**
 * Created by ihietala on 14.5.2014.
 */
public interface AnalysisService {
    void analyseProject(@Observes PullingCompleteEvent event);
}
