package org.silverduck.jace.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PrePersist;

/**
 * @author Iiro Hietala 14.5.2014.
 */
public abstract class AbstractDaoImpl<T> implements AbstractDao<T> {

    @PersistenceContext(unitName = "jace-unit")
    private EntityManager em;

    /**
     * Add a non-persisted entity into database
     * 
     * @param entity
     *            A non-persisted entity to add
     */
    public void add(T entity) {
        if (em == null) {
            throw new RuntimeException("The EntityManager was null!");
        }
        em.persist(entity);
        em.flush();
    }

    @Override
    public Object find(Class<?> clazz, Long id) {
        return em.find(clazz, id);
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * Refresh a entity from database
     * 
     * @param entity
     *            Entity to refresh
     * @return
     */
    public void refresh(T entity) {
        em.refresh(entity);
    }

    /**
     * Remove a persisted entity from database
     * 
     * @param entity
     *            A persisted entity to remove
     */
    public void remove(T entity) {
        em.remove(entity);
    }

    /**
     * Update a persisted entity into databasee
     * 
     * @param entity
     *            A persisted entity to update into database
     * @return
     */
    public T update(T entity) {
        return em.merge(entity);
    }
}
