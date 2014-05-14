package org.silverduck.jace.services.analysis.impl;

import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;

/**
 * Created by ihietala on 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    @Override
    public void analyseProject(@Observes PullingCompleteEvent event) {
        throw new RuntimeException("Analysis not yet implemented. Not analysing project "
            + event.getProject().getName());
    }

}
