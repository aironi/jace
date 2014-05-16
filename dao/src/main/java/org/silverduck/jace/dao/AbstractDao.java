package org.silverduck.jace.dao;

import org.silverduck.jace.domain.project.Project;

/**
 * @author Iiro Hietala 17.5.2014.
 */
public interface AbstractDao<T> {
    void add(T t);

    void remove(T t);

    T update(T t);
}
