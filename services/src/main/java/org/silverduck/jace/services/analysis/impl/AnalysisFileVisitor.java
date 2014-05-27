package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Created by ihietala on 27.5.2014.
 */
public class AnalysisFileVisitor extends InitialAnalysisFileVisitor {
    private static final Log LOG = LogFactory.getLog(AnalysisFileVisitor.class);

    /**
     * Files to analyse
     */
    private final List<String> files;


    public AnalysisFileVisitor(AnalysisSetting setting, Analysis analysis, List<String> files) {
        super(setting, analysis);
        this.files = files;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOG.debug("visitFile: " + file.getFileName());
        Project project = getSetting().getProject();
        String fileName = file.getFileName().toString();

        Path localDir = Paths.get(project.getPluginConfiguration().getLocalDirectory());
        String relativePath = file.toString().replace(localDir.toString(), "").replace("\\", "/");

        if (files.contains(relativePath)) {
            return super.visitFile(file, attrs);
        } else {
            return FileVisitResult.CONTINUE;
        }
    }
}