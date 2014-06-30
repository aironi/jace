package org.silverduck.jace.services.analysis.impl;

/**
 * Created by ihietala on 30.6.2014.
 */
public class ScoredCommit {

    private String commitId;

    private Long score;

    public ScoredCommit(String commitId, Long score) {
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
}
