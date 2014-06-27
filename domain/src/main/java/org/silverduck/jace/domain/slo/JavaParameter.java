package org.silverduck.jace.domain.slo;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Iiro Hietala on 18.5.2014.
 */
@Entity
@Table(name = "JavaParameter")
public class JavaParameter extends AbstractDomainObject {

    @ManyToOne()
    @JoinColumn(name = "JavaMethodRID")
    private JavaMethod javaMethod;

    @Embedded
    private JavaType javaType;

    @Column(name = "Name")
    private String name;

    public JavaParameter() {
        super();
    }

    public JavaParameter(JavaType javaType, String name) {
        this();
        this.javaType = javaType;
        this.name = name;
    }

    public JavaMethod getJavaMethod() {
        return javaMethod;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public String getName() {
        return name;
    }

    public void setJavaMethod(JavaMethod javaMethod) {
        this.javaMethod = javaMethod;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    public void setName(String name) {
        this.name = name;
    }
}
