package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.JavaParameter;
import org.silverduck.jace.domain.slo.JavaSourceSLO;
import org.silverduck.jace.domain.slo.JavaType;
import org.silverduck.jace.domain.slo.OtherFileSLO;
import org.silverduck.jace.domain.slo.SLO;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Initial Analysis implementation. Performs the initial analysis on the source tree and scans all of the found files.
 * 
 * Parses ".java"-files with ASTParser and creates appropriate JavaSourceSLO objects.
 * 
 * Parses other files... todo
 * 
 */
public class InitialAnalysisFileVisitor implements FileVisitor<Path> {

    private static final Log LOG = LogFactory.getLog(InitialAnalysisFileVisitor.class);

    private final Analysis analysis;

    final Set<String> features = new HashSet<String>();

    private final AnalysisSetting setting;

    public InitialAnalysisFileVisitor(AnalysisSetting setting, Analysis analysis) {
        this.setting = setting;
        this.analysis = analysis;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public AnalysisSetting getSetting() {
        return setting;
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

    private void processFeature(String featureName, SLO slo) {
        if (!features.contains((featureName))) {
            features.add(featureName);
            Feature feature = new Feature();
            feature.setName(featureName);
            analysis.getProject().addFeature(feature);
            slo.setFeature(feature);
        } else {
            for (Feature feature : analysis.getProject().getFeatures()) {
                if (feature.getName().equals(featureName)) {
                    slo.setFeature(feature);
                    break;
                }
            }
        }
    }

    private void processJavaFile(Path file, String relativePath) throws IOException {
        LOG.debug("Processing file: " + file.getFileName().toString());
        final JavaSourceSLO slo = new JavaSourceSLO(relativePath);

        ASTParser astParser = ASTParser.newParser(AST.JLS3);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setSource(FileUtils.readFileToString(new File(file.toAbsolutePath().toString())).toCharArray());
        final CompilationUnit cu = (CompilationUnit) astParser.createAST(null);

        cu.accept(new ASTVisitor() {
            private JavaMethod currentMethod = null;

            private Map<String, String> imports = new HashMap<String, String>();

            public boolean visit(PackageDeclaration node) {
                slo.setPackageName(node.getName().toString());

                if (setting.getAutomaticFeatureMapping()) {
                    String[] packageParts = slo.getPackageName().split("\\.");
                    String featureName;
                    if (packageParts.length > 0) {
                        int index;
                        if ("impl".equals(packageParts[packageParts.length - 1])) {
                            index = packageParts.length - 2;
                        } else {
                            index = packageParts.length - 1;
                        }
                        featureName = packageParts[index];
                    } else {
                        featureName = slo.getPackageName();
                    }
                    processFeature(featureName, slo);
                }
                return true;
            }

            public boolean visit(TypeDeclaration node) {
                if (node.isPackageMemberTypeDeclaration()) {
                    slo.setClassName(node.getName().toString());
                }
                return true;
            }

            public boolean visit(ImportDeclaration node) {
                String packageName = node.getName().toString();
                String className = packageName.substring(packageName.lastIndexOf('.') + 1);
                imports.put(className, packageName);
                return true;
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                LOG.debug("visit(MethodDeclaration): Visiting JavaMethod " + node.getName());
                currentMethod = new JavaMethod();
                currentMethod.setName(node.getName().toString());
                currentMethod.setStartLine(cu.getLineNumber(node.getStartPosition()));
                currentMethod.setEndLine(cu.getLineNumber(node.getStartPosition() + node.getLength()));

                if (node.getReturnType2() != null) {
                    currentMethod.setReturnType(new JavaType(resolveQualifiedType(node.getReturnType2().toString()),
                        node.getReturnType2().toString()));
                }

                for (int i = 0; i < node.parameters().size(); i++) {
                    Object parameter = node.parameters().get(i);
                    SingleVariableDeclaration var = (SingleVariableDeclaration) parameter;

                    String name = var.getName().toString();
                    String fullyQualifiedName;
                    String type = var.getType().toString();
                    fullyQualifiedName = resolveQualifiedType(type);

                    JavaType javaType = new JavaType(fullyQualifiedName, type);
                    JavaParameter javaParameter = new JavaParameter(javaType, name);
                    currentMethod.addParameter(javaParameter);
                }
                slo.addMethod(currentMethod);

                return true;
            }

            private String resolveQualifiedType(String type) {
                String fullyQualifiedName;
                if (type.contains(".")) {
                    fullyQualifiedName = type;
                } else {
                    fullyQualifiedName = imports.get(type);
                }
                return fullyQualifiedName;
            }

        });
        getAnalysis().addJavaSourceSlo(slo);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOG.debug("visitFile: " + file.getFileName());

        Project project = getSetting().getProject();
        String fileName = file.getFileName().toString();

        Path localDir = Paths.get(project.getPluginConfiguration().getLocalDirectory());
        String relativePath = file.toString().replace(localDir.toString(), "");

        if (fileName.endsWith(".java")) {
            processJavaFile(file, relativePath);
        } else {
            OtherFileSLO slo = new OtherFileSLO();
            slo.setPath(relativePath);
            String featureName = file.getParent().getFileName().toString();
            List<String> nonFeatures = Arrays.asList(new String[] { "META-INF", "WEB-INF" }); // stupid functionality,
                                                                                              // replace it
            // Skip non-features
            if (!nonFeatures.contains(featureName)) {
                processFeature(featureName, slo);
                analysis.addOtherFileSLO(slo);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        LOG.debug("visitFileFailed: " + file.getFileName());
        return FileVisitResult.CONTINUE;
    }
};
