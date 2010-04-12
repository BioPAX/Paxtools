package org.biopax.paxtools.proxy;

import org.biopax.paxtools.proxy.level3.PhysicalEntityProxy;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class TestSchema
{
    static
    {
           Configuration config = new Configuration().
               setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect").
               setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver").
               setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:baseball").
               setProperty("hibernate.connection.username", "sa").
               setProperty("hibernate.connection.password", "").
               setProperty("hibernate.connection.pool_size", "1").
               setProperty("hibernate.connection.autocommit", "true").
               setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider").
               setProperty("hibernate.hbm2ddl.auto", "create-drop").
               setProperty("hibernate.show_sql", "true").
               addClass(PhysicalEntityProxy.class);

        SessionFactory sessionFactory = config.buildSessionFactory();

    }

}
