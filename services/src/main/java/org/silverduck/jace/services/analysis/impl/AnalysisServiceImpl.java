package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    private static final Log LOG = LogFactory.getLog(AnalysisServiceImpl.class);

    @EJB
    private AnalysisDao analysisDao;

    @EJB
    AnalysisService analysisService;

    @Override
    public void addAnalysisSetting(AnalysisSetting setting) {
        analysisDao.add(setting);
        analysisService.initialAnalysis(setting);
    }

    @Override
    public void analyseProject(@Observes PullingCompleteEvent event) {
        throw new RuntimeException("Analysis not yet implemented. Not analysing project "
            + event.getProject().getName());
    }

    @Override
    public List<AnalysisSetting> findAllAnalysisSettings() {
        return analysisDao.findAllAnalysisSettings();
    }

    @Override
    public AnalysisSetting findAnalysisSettingById(Long id) {
        return analysisDao.findAnalysisSettingById(id);
    }

    /**
     * Performs initial analysis of the file tree and initializes SLOs
     * 
     * @param setting
     */
    @Asynchronous
    public void initialAnalysis(AnalysisSetting setting) {
        String localDirectory = setting.getProject().getPluginConfiguration().getLocalDirectory();
        try {
            Files.walkFileTree(Paths.get(localDirectory), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                    LOG.fatal("preVisitDirectory: " + dir.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    LOG.fatal("visitFile: " + file.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    LOG.fatal("visitFileFailed: " + file.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return null;
                }
            });
        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }

    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisDao.findAnalysisSettingById(id);
        analysisDao.remove(setting);
    }

    @Override
    public void updateAnalysisSetting(AnalysisSetting setting) {
        analysisDao.update(setting);
    }
}
