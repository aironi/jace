package org.silverduck.jace.domain.feature;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents a softeare feature
 */
@Entity
@Table(name = "Feature")
public class Feature extends AbstractDomainObject {

    @Column(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
