package org.silverduck.jace.domain.feature;

import org.silverduck.jace.domain.AbstractDomainObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by ihietala on 17.5.2014.
 */
@Entity
@Table(name = "FeatureMapping")
public class FeatureMapping extends AbstractDomainObject {

    @Column(name = "MappingType")
    @Enumerated(EnumType.STRING)
    private MappingType mappingType;

    @Column(name = "sourcePattern")
    private String sourcePattern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FeatureRID")
    private Feature targetFeature;

}
