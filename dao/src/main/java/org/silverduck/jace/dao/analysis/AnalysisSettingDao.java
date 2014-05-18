package org.silverduck.jace.dao.analysis;

import org.silverduck.jace.dao.AbstractDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;

import java.util.List;

/**
 * @author Iiro Hietala 17.5.2014.
 */

public interface AnalysisSettingDao extends AbstractDao<AnalysisSetting> {
    List<AnalysisSetting> findAllAnalysisSettings();

    AnalysisSetting findAnalysisSettingById(Long analysisSettingId);
}
