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

    public void add(T entity) {
        if (em == null) {
            throw new RuntimeException("The EntityManager was null!");
        }
        em.persist(entity);
        em.flush();
        em.clear();
    }

    @Override
    public T find(Class<T> clazz, Long id) {
        return (T) em.find(clazz, id);
    }

    protected EntityManager getEntityManager() {
        return em;
    }

    public void refresh(T entity) {
        em.refresh(entity);
    }

    public void remove(T entity) {
        em.remove(entity);
    }

    public T update(T entity) {
        T stored = em.merge(entity);
        em.flush();
        em.clear();
        return stored;
    }
}
