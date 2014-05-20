package org.silverduck.jace.domain.feature;

import org.eclipse.persistence.jpa.config.Cascade;
import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.project.Project;
import org.silverduck.jace.domain.slo.JavaSourceSLO;
import org.silverduck.jace.domain.slo.OtherFileSLO;

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
    private List<JavaSourceSLO> javaSourceSlos = new ArrayList<JavaSourceSLO>();

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OtherFileSLO> otherFileSlos = new ArrayList<OtherFileSLO>();

    @ManyToOne
    @JoinColumn(name = "ProjectRID")
    private Project project;

    public void addJavaSourceSlo(JavaSourceSLO slo) {
        if (!this.javaSourceSlos.contains(slo)) {
            this.javaSourceSlos.add(slo);
            slo.setFeature(this);
        }
    }

    public void addOtherFileSlo(OtherFileSLO otherFileSLO) {
        if (!otherFileSlos.contains(otherFileSLO)) {
            otherFileSlos.add(otherFileSLO);
            otherFileSLO.setFeature(this);
        }
    }

    public List<JavaSourceSLO> getJavaSourceSlos() {
        return Collections.unmodifiableList(javaSourceSlos);
    }

    public String getName() {
        return name;
    }

    public List<OtherFileSLO> getOtherFileSlos() {
        return Collections.unmodifiableList(otherFileSlos);
    }

    public Project getProject() {
        return project;
    }

    public void removeJavaSourceSlo(JavaSourceSLO slo) {
        if (this.javaSourceSlos.contains(slo)) {
            javaSourceSlos.remove(slo);
            slo.setFeature(null);
        }
    }

    public void removeOtherFileSlo(OtherFileSLO otherFileSLO) {
        if (otherFileSlos.contains(otherFileSLO)) {
            otherFileSlos.remove(otherFileSLO);
            otherFileSLO.setFeature(null);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
