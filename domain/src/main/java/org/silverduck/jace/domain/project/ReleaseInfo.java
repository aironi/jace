package org.silverduck.jace.domain.project;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Created by ihietala on 13.5.2014.
 */
@Embeddable
public class ReleaseInfo {

    @Column(name = "PathToVersionFile")
    private String pathToVersionFile;

    /**
     * Regular expression pattern or XPath depending on versionFileType
     */
    @Column(name = "pattern")
    private String pattern;

    @Column(name = "versionFileType")
    @Enumerated(EnumType.STRING)
    private VersionFileType versionFileType;

    public String getPathToVersionFile() {
        return pathToVersionFile;
    }

    public String getPattern() {
        return pattern;
    }

    public VersionFileType getVersionFileType() {
        return versionFileType;
    }

    public void setPathToVersionFile(String pathToVersionFile) {
        this.pathToVersionFile = pathToVersionFile;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setVersionFileType(VersionFileType versionFileType) {
        this.versionFileType = versionFileType;
    }
}
