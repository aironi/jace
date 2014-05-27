package org.silverduck.jace.dao;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.project.Project;

/**
 * @author Iiro Hietala 17.5.2014.
 */
public interface AbstractDao<T> {
    /**
     * Add (persist) entity into DB
     * 
     * @param t
     *            Entity to persist for the first time
     */
    void add(T t);

    /**
     * Finds using EntityManager
     * 
     * @param clazz
     *            Entity Class
     * @param id
     *            ID to find
     * @return
     */
    Object find(Class<?> clazz, Long id);

    /**
     * Refresh an Entity from DB
     * 
     * @param t
     *            Entity to refresh
     */
    void refresh(T t);

    /**
     * Remove an Entity from DB
     * 
     * @param t
     *            Entity to Remove
     */
    void remove(T t);

    /**
     * Update an Entity into DB
     * 
     * @param t
     *            Entity to update
     * @return
     */
    T update(T t);
}
