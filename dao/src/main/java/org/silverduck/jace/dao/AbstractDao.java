package org.silverduck.jace.dao;

import org.silverduck.jace.domain.analysis.Analysis;
import org.silverduck.jace.domain.project.Project;

/**
 * This interface defines the basic CRUD operations for an entity.
 *
 * @author Iiro Hietala 17.5.2014.
 */
public interface AbstractDao<T> {
    /**
     * Add an entity into database
     * 
     * @param t
     *            An entity to persist for the first time
     */
    void add(T t);

    /**
     * Find an entity with given type and identifier
     * 
     * @param clazz
     *            The type of the entity Class
     * @param id
     *            ID to find
     * @return
     */
    Object find(Class<T> clazz, Long id);

    /**
     * Refresh an Entity from the database
     * 
     * @param t
     *            The entity to refresh
     */
    void refresh(T t);

    /**
     * Remove an Entity from the database
     * 
     * @param t
     *            Th entity to Remove
     */
    void remove(T t);

    /**
     * Update an Entity into database
     * 
     * @param t
     *            The entity to update
     * @return
     */
    T update(T t);
}
