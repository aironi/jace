package org.silverduck.jace.services.analysis.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ihietala on 30.6.2014.
 */
@XmlRootElement
public class ScoredCommit {

    private String releaseVersion;
    private String commitId;
    private Long score = 0L;
    private Integer directChanges = 0;
    private List<Integer> dependenciesPerLevel = new ArrayList<Integer>();

    public ScoredCommit(String releaseVersion, String commitId, Long score) {
        this.releaseVersion = releaseVersion;
        this.commitId = commitId;
        this.score = score;
    }

    public String getCommitId() {
        return commitId;
    }

    public Long getScore() {
        return score;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public List<Integer> getDependenciesPerLevel() {
        return dependenciesPerLevel;
    }

    public void setDependenciesPerLevel(List<Integer> dependenciesPerLevel) {
        this.dependenciesPerLevel = dependenciesPerLevel;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public Integer getDirectChanges() {
        return directChanges;
    }

    public void setDirectChanges(Integer directChanges) {
        this.directChanges = directChanges;
    }
}
