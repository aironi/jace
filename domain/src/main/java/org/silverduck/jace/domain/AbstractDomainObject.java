package org.silverduck.jace.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * Base class for all domain objects in the project. Contains mandatory fields for all domain objects.
 */
@MappedSuperclass
public abstract class AbstractDomainObject implements Serializable {

    @Column(name = "Created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "Updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public Date getCreated() {
        return created;
    }

    public Long getId() {
        return id;
    }

    public Date getUpdated() {
        return updated;
    }

    @PrePersist
    public void onPrePersist() {
        setCreated(new Date());
    }

    @PreUpdate
    public void onPreUpdate() {
        setUpdated(new Date());
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
