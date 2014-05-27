package org.silverduck.jace.domain.feature;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.SLO;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
 * Represents a softeare feature
 */
@Entity
@Table(name = "Feature")
public class Feature extends AbstractDomainObject {

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SLO> slos = new ArrayList<SLO>();

    @Column(name = "name")
    private String name;


    @ManyToOne
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public void addSlo(SLO slo) {
        if (!this.slos.contains(slo)) {
            this.slos.add(slo);
            slo.setFeature(this);
        }
    }



    public List<SLO> getSlos() {
        return Collections.unmodifiableList(slos);
    }

    public String getName() {
        return name;
    }



    public Project getProject() {
        return project;
    }

    public void removeSlos(SLO slo) {
        if (this.slos.contains(slo)) {
            slos.remove(slo);
            slo.setFeature(null);
        }
    }



    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
