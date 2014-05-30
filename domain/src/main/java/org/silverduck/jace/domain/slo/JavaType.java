package org.silverduck.jace.domain.slo;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Represetns a JavaType (i.e. ParameterType, VariableType, etc.)
 * 
 */
@Embeddable
public class JavaType {
    @Column(name = "FullyQualifiedType", length = 2048)
    private String fullyQualifiedType;

    @Column(name = "Type", length = 2048)
    private String type;

    public JavaType() {
        super();
    }

    public JavaType(String fullyQualifiedType, String type) {
        this();
        this.fullyQualifiedType = fullyQualifiedType;
        this.type = type;
    }

    public String getFullyQualifiedType() {
        return fullyQualifiedType;
    }

    public String getType() {
        return type;
    }

    public void setFullyQualifiedType(String fullyQualifiedName) {
        this.fullyQualifiedType = fullyQualifiedName;
    }

    public void setType(String name) {
        this.type = name;
    }
}
