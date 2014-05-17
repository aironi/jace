package org.silverduck.jace.domain.analysis;

import org.silverduck.jace.domain.AbstractDomainObject;
import org.silverduck.jace.domain.slo.SLO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Set;

/**
 * Created by ihietala on 17.5.2014.
 */
@Entity
@Table(name = "Analysis")
public class Analysis extends AbstractDomainObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AnalysisSettingRID")
    private AnalysisSetting analysisSetting;

    @Column(name = "AnalysisStatus")
    @Enumerated(EnumType.STRING)
    private AnalysisStatus analysisStatus;

    @OneToMany(mappedBy = "analysis", fetch = FetchType.LAZY)
    private Set<SLO> slos;

    public Analysis() {
    }

    public void addSLO(SLO slo) {
        if (!slos.contains(slo)) {
            slos.add(slo);
        }
    }

    public Set<SLO> getSlos() {
        return Collections.unmodifiableSet(slos);
    }

    public void removeSLO(SLO slo) {
        if (slos.contains(slo)) {
            slos.remove(slo);
        }
    }
}
