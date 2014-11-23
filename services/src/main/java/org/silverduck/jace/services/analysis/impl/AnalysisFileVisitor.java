package org.silverduck.jace.services.analysis.impl;

import org.silverduck.jace.dao.vcs.DiffDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOType;
import org.silverduck.jace.domain.vcs.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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
    private final Map<String, SLO> filesMap = new HashMap<>();
    private final Set<SLO> slos;

    public AnalysisFileVisitor(Analysis analysis, Set<SLO> slos) {
        super(analysis);
        Set<String> features = new HashSet<String>();
        for (Feature f : analysis.getProject().getFeatures()) {
            features.add(f.getName());
        }
        super.addFeatures(features);
        this.slos = slos;
        for (SLO slo : slos) {
            filesMap.put(slo.getPath(), slo);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOG.debug("visitFile: " + file.getFileName());
        Project project = getSetting().getProject();

        Path localDir = Paths.get(project.getPluginConfiguration().getLocalDirectory());
        String relativePath = file.toString().replace(localDir.toString(), "").replace("\\", "/");

        if (filesMap.keySet().contains(relativePath)) {
            return super.visitFile(file, attrs);
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    @Override
    public SLO resolveSLO(String relativePath, SLOType sloType) {
        SLO slo = filesMap.get(relativePath);
        if (slo == null) {
            throw new IllegalStateException("SLO could not be found in the filesMap");
        }
        slo.setSloType(sloType); // update the previously unknown type.
        return slo;
    }
}