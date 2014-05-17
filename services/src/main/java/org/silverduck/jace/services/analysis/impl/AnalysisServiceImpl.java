package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.silverduck.jace.common.exception.JaceRuntimeException;
import org.silverduck.jace.dao.analysis.AnalysisDao;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOType;
import org.silverduck.jace.services.analysis.AnalysisService;
import org.silverduck.jace.services.project.impl.PullingCompleteEvent;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Iiro Hietala 13.5.2014.
 */
@Stateless(name = "AnalysisServiceEJB")
public class AnalysisServiceImpl implements AnalysisService {

    private class InitialAnalysisFileVisitor implements FileVisitor<Path> {

        private final Analysis analysis;

        private final AnalysisSetting setting;

        public InitialAnalysisFileVisitor(AnalysisSetting setting, Analysis analysis) {
            this.setting = setting;
            this.analysis = analysis;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            LOG.debug("postVisitDirectory: " + dir.getFileName());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            LOG.debug("preVisitDirectory: '" + dir.getFileName() + "'");

            if (".git".equals(dir.getFileName().toString())) {
                LOG.debug("Skipping git dir!");
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            LOG.debug("visitFile: " + file.getFileName());

            Project project = setting.getProject();
            Path relativePath = file.relativize(Paths.get(project.getPluginConfiguration().getLocalDirectory()));

            if (file.getFileName().toString().endsWith(".java")) {
                SLO slo = new SLO(relativePath.toString(), SLOType.SOURCE);

                ASTParser astParser = ASTParser.newParser(AST.JLS3);
                astParser.setKind(ASTParser.K_COMPILATION_UNIT);

                astParser.setSource(FileUtils.readFileToString(new File(file.toAbsolutePath().toString()))
                    .toCharArray());
                final CompilationUnit cu = (CompilationUnit) astParser.createAST(null);
                cu.accept(new ASTVisitor() {

                    Set<String> names = new HashSet<String>();

                    public boolean visit(VariableDeclarationFragment node) {
                        SimpleName name = node.getName();
                        names.add(name.getIdentifier());
                        LOG.fatal("visit(Variable): Found variable '" + name + " at line "
                            + cu.getLineNumber(name.getStartPosition()));
                        return false; // do not continue to avoid usage info
                    }

                    public boolean visit(SimpleName node) {
                        if (this.names.contains(node.getIdentifier())) {
                            LOG.fatal("visit(node): Variable '" + node + " is used at line "
                                + cu.getLineNumber(node.getStartPosition()));
                        }
                        return true;
                    }

                    @Override
                    public void endVisit(MethodDeclaration node) {
                        LOG.fatal("endVisit(Method): Method " + node.getName() + " was visited");
                    }

                });
                // analysis.addSLO(slo);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            LOG.debug("visitFileFailed: " + file.getFileName());
            return FileVisitResult.CONTINUE;
        }
    };

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
        LOG.fatal("localdir: " + localDirectory);
        try {
            Analysis analysis = new Analysis();
            Files.walkFileTree(Paths.get(localDirectory), new InitialAnalysisFileVisitor(setting, analysis));

        } catch (IOException e) {
            throw new JaceRuntimeException("Couldn't perform initial analysis.", e);
        }

    }

    @Override
    public void initialAnalysis(Long analysisSettingId) {
        analysisService.initialAnalysis(analysisDao.findAnalysisSettingById(analysisSettingId));
    }

    @Override
    public void removeAnalysisSettingById(Long id) {
        AnalysisSetting setting = analysisDao.findAnalysisSettingById(id);
        analysisDao.remove(setting);
    }

    @Override
    public void triggerAnalysis(Long analysisSettingId) {

    }

    @Override
    public void updateAnalysisSetting(AnalysisSetting setting) {
        analysisDao.update(setting);
    }
}
