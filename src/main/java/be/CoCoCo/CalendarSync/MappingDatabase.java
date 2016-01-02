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
class MappingDatabase {

  static Logger logger = Logger.getLogger (MappingDatabase.class);
  private static SessionFactory factory = createSessionFactory (); 
  private static ServiceRegistry serviceRegistry;

  public static SessionFactory createSessionFactory() {
      Configuration configuration = new Configuration();
      configuration.configure();
      serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
          configuration.getProperties()).build();
      return configuration.buildSessionFactory(serviceRegistry);
  }

  public MappingDatabase () {
  }

  /**
   * @param UID1
   * @param UID2
   */
  public void addMapping(String UID1, String UID2) {
    // Only add mapping if not already exists
    if (existMapping(UID1, UID2)) return;
    
    Session session = factory.openSession();
    Transaction tx = null;
    try{
      tx = session.beginTransaction();
      Mapping mapping = new Mapping(UID1, UID2);
      session.save(mapping); 
      tx.commit();
    }catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      logger.error ("Error adding mapping-record (" + UID1 + "," + UID2 + ") to mapping-database");
      logger.info (e);
    }finally {
      session.close(); 
    }
    return;
  }

  private boolean existMapping(String UID1, String UID2) {
    String result = getMapping(UID1);
    if (null != result)
      return result.equals (UID2);
    else
      return false;
  }
  
  @SuppressWarnings ("unchecked")
  public String getMapping(String UID) {
    Session session = factory.openSession ();
    Transaction tx = null;
    try {
      tx = session.beginTransaction ();
      String queryString = "FROM Mapping where UID1 = :UID OR UID2 = :UID";
      Query query = session.createQuery (queryString).setParameter ("UID", UID);
      List<Mapping> results = (List<Mapping>) query.list ();
      if (results.isEmpty ()) {
        return null;
      } else if ( 1 < results.size())
        throw new RuntimeException ("Double mapping found"); 
      else {
        Mapping mapping = results.get (0);
        if ( UID.equals (mapping.getUID1 ()) ) return mapping.getUID2 ();
        else return mapping.getUID1 ();
      }
    } catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      logger.error ("Error reading record from mappingDatabase");
      logger.info (e);
    }finally {
      session.close(); 
    }
    return null;
  }

  public void deleteMapping(String UID1, String UID2) {
    Session session = factory.openSession ();
    Transaction tx = null;
    try {
      tx = session.beginTransaction ();
      String queryString = "DELETE Mapping WHERE ( UID1 = :UID1 AND UID2 = :UID2 )";
      Query  query = session.createQuery (queryString);
      query.setParameter ("UID1", UID1).setParameter ("UID2", UID2);
      query.executeUpdate ();
      query.setParameter ("UID1", UID2).setParameter ("UID2", UID1);
      query.executeUpdate ();
      tx.commit ();
    } catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      logger.error ("Error deleting record from mappingdatabase");
      logger.info (e);
    }finally {
      session.close(); 
    }
  }
  
  public void close() {
    factory.close ();
  }
}
