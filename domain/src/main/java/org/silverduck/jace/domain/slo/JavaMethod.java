package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
@Table(name = "JavaMethod")
public class JavaMethod extends AbstractDomainObject {

    @Column(name = "EndLine")
    private Integer endLine;

    @ManyToOne
    @JoinColumn(name = "JavaSourceSLORID")
    private JavaSourceSLO javaSourceSLO;

    @Column(name = "MethodName")
    private String name;

    @OneToMany(mappedBy = "javaMethod", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JavaParameter> parameters = new ArrayList<JavaParameter>();

    @Embedded
    private JavaType returnType;

    @Column(name = "startLine")
    private Integer startLine;

    public void addParameter(JavaParameter javaParameter) {
        if (!this.parameters.contains(javaParameter)) {
            parameters.add(javaParameter);
            javaParameter.setJavaMethod(this);
        }
    }

    public Integer getEndLine() {
        return endLine;
    }

    public JavaSourceSLO getJavaSourceSLO() {
        return javaSourceSLO;
    }

    public String getName() {
        return name;
    }

    public List<JavaParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public JavaType getReturnType() {
        return returnType;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public void removeParameter(JavaParameter javaParameter) {
        if (this.parameters.contains(javaParameter)) {
            parameters.remove(javaParameter);
        }
    }

    public void setEndLine(Integer endLine) {
        this.endLine = endLine;
    }

    public void setJavaSourceSLO(JavaSourceSLO javaSourceSLO) {
        this.javaSourceSLO = javaSourceSLO;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReturnType(JavaType returnType) {
        this.returnType = returnType;
    }

    public void setStartLine(Integer startLine) {
        this.startLine = startLine;
    }
}
