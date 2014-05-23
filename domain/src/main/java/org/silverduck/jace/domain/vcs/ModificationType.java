package org.silverduck.jace.domain.vcs;

/**
 * Reflects the git ChangeType directly
 */
public enum ModificationType {
    ADD,

    MODIFY,

    DELETE,

    RENAME,

    COPY;
}
