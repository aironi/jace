package org.silverduck.jace.domain.vcs;

/**
 * Created by ihietala on 13.5.2014.
 */
public interface Plugin {

    void checkout(String cloneUrl, String branch);

    void cloneRepo(String cloneUrl, String localDirectory);

    void pull(String cloneUrl);

}
