package org.silverduck.jace.domain.analysis.slo;

import org.silverduck.jace.domain.AbstractDomainObject;

import org.silverduck.jace.domain.slo.SLO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * An object that represents an import to other module.
 */
@Entity
@Table(name = "SLOImport")
public class SLOImport extends AbstractDomainObject {

    @Column(name = "qualifiedClassName")
    private String qualifiedClassName;

    @ManyToOne()
    @JoinColumn(name = "SLORID")
    private SLO slo;

    public SLOImport() {
        super();
    }

    public SLOImport(String qualifiedClassName) {
        this();
        setQualifiedClassName(qualifiedClassName);
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public SLO getSlo() {
        return slo;
    }

    public void setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void setSlo(SLO slo) {
        this.slo = slo;
    }
}
