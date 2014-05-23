package org.silverduck.jace.dao.analysis;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.analysis.Analysis;

import org.silverduck.jace.domain.slo.JavaSourceSLO;

import java.util.List;

/**
 * Created by ihietala on 18.5.2014.
 */
public interface AnalysisDao extends AbstractDao<Analysis> {

    JavaSourceSLO findJavaSourceSLO(String path);

    List<Analysis> listAll();
}
