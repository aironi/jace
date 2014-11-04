package org.silverduck.jace.services.analysis.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;
import org.silverduck.jace.common.xml.XmlUtils;
import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.analysis.AnalysisSetting;
import org.silverduck.jace.domain.analysis.slo.SLOImport;
import org.silverduck.jace.domain.feature.Feature;
import org.silverduck.jace.domain.feature.FeatureMapping;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.project.ReleaseInfo;
import org.silverduck.jace.domain.slo.JavaMethod;
import org.silverduck.jace.domain.slo.JavaParameter;
import org.silverduck.jace.domain.slo.JavaType;
import org.silverduck.jace.domain.slo.SLO;
import org.silverduck.jace.domain.slo.SLOStatus;
import org.silverduck.jace.domain.slo.SLOType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Initial Analysis implementation. Performs the initial analysis on the source tree and scans all of the found files.
 * 
 * Parses ".java"-files with ASTParser and creates appropriate SLO objects.
 *
 * @author Iiro Hietala
 */
public class InitialAnalysisFileVisitor implements FileVisitor<Path> {

    private static final Logger LOG = LoggerFactory.getLogger(InitialAnalysisFileVisitor.class);

    private final Analysis analysis;

    final Set<String> features = new HashSet<String>();

    private final AnalysisSetting setting;

    public InitialAnalysisFileVisitor(AnalysisSetting setting, Analysis analysis) {
        this.setting = setting;
        this.analysis = analysis;
    }

    protected void addFeatures(Collection<String> features) {
        this.features.addAll(features);
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

    private void processJavaFile(Path file, String relativePath) throws IOException {
        LOG.debug("Processing file: " + file.getFileName().toString());
        final SLO slo = new SLO(relativePath, SLOType.SOURCE);
        slo.setSloStatus(SLOStatus.CURRENT);

        ASTParser astParser = ASTParser.newParser(AST.JLS3);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setSource(FileUtils.readFileToString(new File(file.toAbsolutePath().toString())).toCharArray());

        final CompilationUnit cu = (CompilationUnit) astParser.createAST(null);

        cu.accept(new ASTVisitor() {
            private JavaMethod currentMethod = null;

            private Map<String, String> imports = new HashMap<String, String>();

            Map<String, String> variables = new HashMap<String, String>();

            public boolean visit(PackageDeclaration node) {
                slo.setPackageName(node.getName().toString());

                String featureName = null;
                for (FeatureMapping featureMapping : analysis.getProject().getFeatureMappings()) {
                    if (featureMapping.appliesTo(slo)) {
                        featureName = featureMapping.getFeatureName();
                        LOG.debug("Found a FeatureMapping that matches the SLO {}. Setting feature name to {}", slo.getPath(), featureName);
                    }
                }

                if (featureName == null && setting.getAutomaticFeatureMapping()) {
                    LOG.debug("Couldn't find a feature mapping for SLO {}. Resolving automatically.", slo.getPath());
                    String[] packageParts = slo.getPackageName().split("\\.");

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

                }

                if (featureName != null) {
                    processFeature(featureName, slo);
                }
                return true;
            }

            public boolean visit(EnumDeclaration node) {
                if (node.isPackageMemberTypeDeclaration()) {
                    slo.setClassName(node.getName().toString());
                    slo.setQualifiedClassName(slo.getPackageName() + "." + slo.getClassName());
                }

                return true;
            }
            public boolean visit(TypeDeclaration node) {

                if (node.isPackageMemberTypeDeclaration()) {
                    slo.setClassName(node.getName().toString());
                    slo.setQualifiedClassName(slo.getPackageName() + "." + slo.getClassName());
                }

                return true;
            }

            public boolean visit(MethodInvocation node) {

                // TODO: Not done. Need to find out the calling types. One way:
                // 1. Store the method invocations temporarily into associated JavaMethod (transient)
                // 2. On second pass (Note: not implemented), evaluate the return types of the method calls and
                // associate them persistently
                // OR
                // 1. Figure out how to get the ASTParser resolveBindings working outside of Eclipse Plugin framework...
                if (currentMethod != null) {

                    Expression exp = node.getExpression();

                    String name = null;
                    if (exp != null) {
                        name = exp.toString();
                    }
                    String methodCall = node.getName().toString();
                    String type = variables.get(name);
                    // // currentMethod.addMethodCall(new JavaMethodCall(name, type,
                    // cu.getLineNumber(node.getStartPosition())));

                }
                return true;
            }

            public boolean visit(VariableDeclarationFragment node) {

                // if (currentMethod != null) {
                String name = node.getName().toString();

                String type = null;
                try {
                    type = BeanUtils.getProperty(node.getParent(), "type");
                } catch (IllegalAccessException e) {
                    // Bummer
                } catch (InvocationTargetException e) {
                    // Bummer
                } catch (NoSuchMethodException e) {
                    // Bummer
                }

                String fullyQualifiedType = resolveQualifiedType(type);
                variables.put(name, fullyQualifiedType);
                // }

                return true;
            }

            public boolean visit(ImportDeclaration node) {
                String packageName = node.getName().toString();
                String className = packageName.substring(packageName.lastIndexOf('.') + 1);
                imports.put(className, packageName);
                slo.addSLOImport(new SLOImport(packageName));
                return true;
            }

            @Override
            public void endVisit(MethodDeclaration node) {
                if (currentMethod != null) {
                    slo.addMethod(currentMethod);
                    currentMethod = null;
                }
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                LOG.debug("visit(MethodDeclaration): Visiting JavaMethod " + node.getName());
                // variables = new HashMap<String, String>();

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
                    String type = var.getType().toString();
                    String fullyQualifiedName = resolveQualifiedType(type);

                    JavaType javaType = new JavaType(fullyQualifiedName, type);
                    JavaParameter javaParameter = new JavaParameter(javaType, name);
                    currentMethod.addParameter(javaParameter);
                }

                return true;
            }

            private String resolveQualifiedType(String type) {
                String fullyQualifiedType;
                if (type.contains(".")) {
                    fullyQualifiedType = type;
                } else {
                    fullyQualifiedType = imports.get(type);
                }
                if (fullyQualifiedType == null) {
                    fullyQualifiedType = type; // Couldn't resolve. Perhaps imports are used with .* wildcard.
                                               // FIXME: This might introduce a Fatal issue with type resolving that is
                                               // related to
                                               // the fact that we are not resolving bindings. Consider using something
                                               // else than ASTParser
                }
                return fullyQualifiedType;
            }

        });
        getAnalysis().addSlo(slo);
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

    private void processReleaseFile(Path file) {
        ReleaseInfo releaseInfo = getSetting().getProject().getReleaseInfo();
        String release = null;
        FileInputStream is = null;

        try {
            is = new FileInputStream(file.toAbsolutePath().toFile());
            switch (releaseInfo.getVersionFileType()) {
            case PROPERTIES:
                Properties p = new Properties();
                try {
                    p.load(is);
                    is.close();
                } catch (IOException e) {
                    LOG.warn("processReleaseFile(): Couldn't open properties file to determine release version", e);
                }
                release = p.getProperty(releaseInfo.getPattern());
                break;
            case XML:
                try {
                    String xml = FileUtils.readFileToString(file.toAbsolutePath().toFile());
                    String xmlNoNs = XmlUtils.removeNameSpaces(xml);
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    InputSource input = new InputSource(new StringReader(xmlNoNs));
                    release = xPath.evaluate(releaseInfo.getPattern(), input);
                } catch (XPathExpressionException e) {
                    LOG.warn("processReleaseFile(): Couldn't evaluate XPath to determine release version", e);
                } catch (IOException e) {
                    LOG.warn("processReleaseFile(): Couldn't evaluate XPath to determine release version", e);
                }

                break;
            default:
                release = "Unknown";
                break;
            }
            is.close();
        } catch (FileNotFoundException e) {
            LOG.warn("processReleaseFile(): File not found.", e);
        } catch (IOException e) {
            LOG.warn("processReleaseFile(): Exception when closing file.", e);
        }

        analysis.setReleaseVersion(release);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LOG.debug("visitFile: " + file.getFileName());

        Project project = getSetting().getProject();
        String fileName = file.getFileName().toString();

        Path localDir = Paths.get(project.getPluginConfiguration().getLocalDirectory());
        String relativePath = file.toString().replace(localDir.toString(), "").replace("\\", "/");

        if (project.getReleaseInfo().getPathToVersionFile().equals(relativePath)) {
            processReleaseFile(file);
        }
        if (fileName.endsWith(".java")) {
            processJavaFile(file, relativePath);
        } else {
            SLO slo = new SLO(relativePath, SLOType.OTHER_FILE);
            String featureName = null;
            for (FeatureMapping featureMapping : analysis.getProject().getFeatureMappings()) {
                if (featureMapping.appliesTo(slo)) {
                    featureName = featureMapping.getFeatureName();
                    LOG.debug("Found a FeatureMapping that matches the SLO {}. Setting feature name to {}", slo.getPath(), featureName);
                }
            }
            if (featureName == null && setting.getAutomaticFeatureMapping()) {
                featureName = file.getParent().getFileName().toString();
            }
            List<String> nonFeatures = Arrays.asList(new String[] { "META-INF", "WEB-INF" }); // stupid functionality,
                                                                                              // replace it with
                                                                                              // configurable one
            // Skip non-features completely
            if (!nonFeatures.contains(featureName)) {
                if (featureName != null) {
                    processFeature(featureName, slo);
                }
                analysis.addSlo(slo);
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
