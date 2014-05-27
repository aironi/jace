package org.silverduck.jace.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.enterprise.context.Conversation;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.Properties;
import org.apache.openejb.api.LocalClient;
import org.silverduck.jace.dao.analysis.impl.AnalysisDaoImplTest;

/**
 * Created by ihietala on 25.5.2014.
 */

public abstract class DaoTestCase {
    private static EJBContainer ejbContainer;

    @BeforeClass
    public static void setupBeforeClass() throws NamingException {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        ejbContainer = EJBContainer.createEJBContainer(p);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    @After
    public void afterTest() throws Exception {
        userTransaction.rollback(); // Don't actually persist anything.
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void inject(DaoTestCase testCase) throws NamingException {
        Context context = ejbContainer.getContext();
        context.bind("inject", testCase);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before
    public void setup() throws Exception {
        Context context = ejbContainer.getContext();
        context.bind("inject", this); // inject LocalClient fields
        userTransaction.begin();
    }
}
