/**
 *
 * CalendarSync
 * Copyright @2015 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.util.List;

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

  public void addMapping(String UID1, String UID2) {
    Session session = factory.openSession();
    Transaction tx = null;
    try{
      tx = session.beginTransaction();
      Mapping mapping = new Mapping(UID1, UID2);
      session.save(mapping); 
      tx.commit();
    }catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      //TODO catch error
      e.printStackTrace(); 
    }finally {
      session.close(); 
    }
    return;
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
      } else if ( 1 > results.size())
        throw new RuntimeException ("Double mapping found"); 
      else {
        Mapping mapping = results.get (0);
        if ( UID.equals (mapping.getUID1 ()) ) return mapping.getUID2 ();
        else return mapping.getUID1 ();
      }
    } catch (HibernateException e) {
      if (tx!=null) tx.rollback();
      //TODO catch error
      e.printStackTrace(); 
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
      //TODO catch error
      e.printStackTrace(); 
    }finally {
      session.close(); 
    }
  }
  
  public void close() {
    factory.close ();
  }
}
