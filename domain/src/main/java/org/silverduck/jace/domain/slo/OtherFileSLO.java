package org.silverduck.jace.domain.slo;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by ihietala on 20.5.2014.
 */
@Entity
@Table(name = "SLO")
@DiscriminatorColumn(name = "SloType")
@DiscriminatorValue(value = "OTHER_FILE")
public class OtherFileSLO extends SLO {

}
