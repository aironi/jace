package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.feature.Feature;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ihietala on 18.5.2014.
 */

@Entity
@Table(name = "SLO")
@DiscriminatorColumn(name = "SloType")
@DiscriminatorValue(value = "SOURCE")
public class JavaSourceSLO extends SLO {

    @Column(name = "ClassName")
    private String className;

    @OneToMany(mappedBy = "javaSourceSLO", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JavaMethod> javaMethods = new ArrayList<JavaMethod>();

    @Column(name = "PackageName")
    private String packageName;

    public JavaSourceSLO() {
        super();
    }

    public JavaSourceSLO(String relativePath) {
        super(relativePath, SLOType.SOURCE);
    }

    public void addMethod(JavaMethod method) {
        if (!this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setJavaSourceSLO(this);
        }
    }

    public String getClassName() {
        return className;
    }

    public List<JavaMethod> getJavaMethods() {
        return Collections.unmodifiableList(javaMethods);
    }

    public String getPackageName() {
        return packageName;
    }

    public void removeMethod(JavaMethod method) {
        if (this.javaMethods.contains(method)) {
            javaMethods.add(method);
            method.setJavaSourceSLO(this);
        }
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
