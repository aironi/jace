package org.silverduck.jace.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Iiro on 20.10.2014.
 */
@ApplicationPath("/")
public class RestApp extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet();
        // register root resource
        classes.add(AnalysisRest.class);
        classes.add(ProjectsRest.class);
        return classes;
    }
}