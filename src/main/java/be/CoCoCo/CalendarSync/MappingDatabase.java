/**
 * 
 * be.CoCoCo.CalendarSync Copyright @2014 CoCoCo.be
 * 
 */
package be.CoCoCo.CalendarSync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Kris Cox
 * 
 */
class MappingDatabaseException extends Exception {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -5718740219462162991L;

  public MappingDatabaseException (String msg) {
    super (msg);
  }
}

/**
 * @author Kris Cox
 * 
 */
class MappingDatabase {

  /**
   * Define log4j Logger for logging purposes
   */
  private static Logger logger = Logger.getLogger (MappingDatabase.class);
  /**
   * Properties defined during construction
   */
  private static String driver;
  private static String connectionString;
  private static String userName;
  private static String password;

  private Connection    connection;
  private ResultSet     resultSet;

  /**
   * Default constructor
   * 
   * @param properties
   *          : properties containing db_driver, db_ip, db_port, db_name,
   *          db_user, db_password
   * @throws MappingDatabaseException
   */
  MappingDatabase (Properties properties) throws MappingDatabaseException {
    logger.trace ("Entering MappingDatabase");

    // Read the necessary properties from given properties
    String dbDriver = properties.getProperty ("db_driver", "HSQLDB");
    String dbIP = properties.getProperty ("db_ip", "127.0.0.1");
    String dbPort = properties.getProperty ("db_port", "9001");
    String dbName = properties.getProperty ("db_name", "CalendarSynchronizer");
    userName = properties.getProperty ("db_user", "sa");
    password = properties.getProperty ("db_password", "sa");

    // Create connection string and
    if (dbDriver.equals ("MSSQL")) {
      driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
      connectionString = "jdbc:sqlserver://" + dbIP + ":" + dbPort + ";databaseName="
          + dbName;
    } else if (dbDriver.equals ("MySQL")) {
      driver = "com.mysql.jdbc.Driver";
      connectionString = "jdbc:mysql://" + dbIP + ":" + dbPort + "/" + dbName;
    } else if (dbDriver.equals ("HSQLDB")) {
      driver = "org.hsqldb.jdbc.JDBCDriver";
      connectionString = "jdbc:hsqldb:hsql://" + dbIP + ":" + dbPort + "/" + dbName;
    }

  }

  /**
   * Open connection to database
   * 
   * return if successful opened 
   */
  public boolean open () {
    logger.trace ("Entering Open");
    try {
      Class.forName (driver);
      connection = DriverManager.getConnection (connectionString, userName, password);
    } catch (ClassNotFoundException e) {
      logger.error ("Error opening database connection: driver class " + driver
          + "not found");
      logger.info (e);
      logger.trace ("Exiting Open");
      return false;
    } catch (SQLException e) {
      logger.error ("Error opening database connection");
      logger.info (e);
      logger.trace ("Exiting Open");
      return false;
    }
    logger.trace ("Exiting Open");
    return true;
  }

  /**
   * Check if database is open
   * 
   * @return true if database is open, otherwise false
   */
  public boolean checkOpen () {
    logger.trace ("CheckOpen");
    
    if (null == connection) return false;
    
    try {
      return connection.isValid (60);
    } catch (SQLException e) {
      logger.error ("Database connection invalid");
      logger.info (e);
      return false;
    }
  }

  /**
   * Insert value to database
   * 
   * @param uid1
   *          , uid2 : both UID to be mapped together
   * @throws mappingDatabaseException
   */
  public void insert (String uid1, String uid2) throws MappingDatabaseException {
    logger.trace ("Entering insert");
    String query = "insert into MAPPING (uid1,uid2) values('" + uid1 + "', '" + uid2
        + "')";
    logger.trace (query);

    executeQuery (query);

    logger.trace ("Exiting insert");
  }

  /**
   * Retrieve mapping Uid in database
   * 
   * @param uid
   *          : uid to be mapped
   * 
   * @return mapping url
   * @throws MappingDatabaseException
   */
  public String find (String uid) throws MappingDatabaseException {
    logger.trace ("Entering find");
    String result = null;
    String query = "select uid2 from MAPPING where uid1 = '" + uid + "'";
    logger.trace (query);
 
    // if database not open, open database
    if (! checkOpen ()) this.open ();

    try {
      // check if uid match uid1
      if (executeQuery (query)) {
        if (resultSet.next ()) {
          result = resultSet.getString (1);
        } else {

          // if not check if uid match uid2
          query = "select uid1 from MAPPING where uid2 = '" + uid + "'";
          logger.trace (query);

          if (executeQuery (query)) {
            if (resultSet.next ()) result = resultSet.getString (1);
          }
        }
      }
    } catch (SQLException e) {
      logger.error ("Error finding uid in database: " + e.getMessage ());
      logger.info (e);
      throw new MappingDatabaseException (e.getMessage ());
    }

    logger.trace ("Exiting find");
    return result;
  }

  /**
   * Execute given query
   * 
   * @param query
   * @throws MappingDatabaseException
   */
  private boolean executeQuery (String query) throws MappingDatabaseException {
    logger.trace ("Entering executeQuery");
    boolean result = false;
    Statement statement;

    // If this database connection is not open yet, open it.
    if (!checkOpen ()) {
      open ();
    }

    try {
      statement = connection.createStatement ();
      if (result = statement.execute (query)) {
        resultSet = statement.getResultSet ();
      }

    } catch (SQLException e) {
      logger.error ("Error inserting value in mappingDatabase: " + e);
      logger.info (e);
      throw new MappingDatabaseException (e.getMessage ());
    }
    logger.trace ("Exiting executeQuery");
    return result;
  }

  /**
   * remove tupple from database
   * 
   * @param uid1
   * @param uid2
   * @throws MappingDatabaseException
   */
  public void remove (String uid1, String uid2) throws MappingDatabaseException {
    logger.trace ("Entering remove");
    String query1 = "DELETE FROM MAPPING WHERE UID1 = '" + uid1 + "' AND UID2 = '" + uid2
        + "';";
    String query2 = "DELETE FROM MAPPING WHERE UID2 = '" + uid1 + "' AND UID1 = '" + uid2
        + "';";

    logger.trace (query1);
    executeQuery (query1);

    logger.trace (query2);
    executeQuery (query2);

    logger.trace ("Exiting insert");
  }

}
