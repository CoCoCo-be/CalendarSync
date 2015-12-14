/**
 *
 * CalendarSync
 * Copyright @2015 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;


/**
 * @author Kris Cox
 *
 */
class UUIDGetter {
  
  static Logger logger = Logger.getLogger (UUIDGetter.class);
  private static SessionFactory factory = createSessionFactory (); 
  private static ServiceRegistry serviceRegistry;
  
  protected static SessionFactory createSessionFactory() {
    Configuration configuration = new Configuration();
    configuration.configure();
    serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
        configuration.getProperties()).build();
    return configuration.buildSessionFactory(serviceRegistry);
  }

  public UUIDGetter() {
    
  }
  
  public int getNew() {
    Session session = factory.openSession ();
    Transaction tx = null;
    int id;
    try {
      tx = session.beginTransaction ();
      Query query = session.getNamedQuery ("getID");
      @SuppressWarnings ("rawtypes")
      List results = query.list ();
      if ( null == results.get(0)) 
        id = 1;
      else
        id = ((Integer) results.get (0)) + 1;
      UUID newID = new UUID(id);
      session.save(newID);
      tx.commit ();
    } catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      logger.error ("Error writing UUID");
      logger.info (e);
      throw new RuntimeException(e.toString ());
    }finally {
      session.close(); 
    }
    return id;
  }
  
  public void reset() {
    Session session = factory.openSession ();
    Transaction tx = null;
    tx = session.beginTransaction();
    Query query = session.getNamedQuery("reset") ;
    query.executeUpdate ();
    tx.commit (); 
  }
  
}
