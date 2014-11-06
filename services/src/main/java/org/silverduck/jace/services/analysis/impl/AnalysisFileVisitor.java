package org.silverduck.jace.services.analysis.impl;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The AnalysisFileVisitor analyses all of the files that are defined in the constructor.
 *
 * Created by Iiro Hietala on 27.5.2014.
 */
public class AnalysisFileVisitor extends InitialAnalysisFileVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(AnalysisFileVisitor.class);
        /**
         * Files to analyse
         */
    private final List<String> files;

    public AnalysisFileVisitor(AnalysisSetting setting, Analysis analysis, List<String> files) {
        super(setting, analysis);
        Set<String> features = new HashSet<String>();
        for (Feature f : analysis.getProject().getFeatures()) {
            features.add(f.getName());
        }
        super.addFeatures(features);
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