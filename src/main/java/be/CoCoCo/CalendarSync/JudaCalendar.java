package be.CoCoCo.CalendarSync;
/**
 * 
 * be.CoCoCo.CalendarSync Copyright @2014 CoCoCo.be
 * 
 */


import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

/**
 * @author Kris Cox
 * 
 */
class JudaCalendar implements Calendar {

  static Logger logger = Logger.getLogger (JudaCalendar.class);

  // Fields
  private java.util.Calendar  startLookWindow = java.util.Calendar.getInstance ();
  private java.util.Calendar  endLookWindow   = java.util.Calendar.getInstance ();
  private ArrayList<JudaItem> calendar = new ArrayList<JudaItem>();
  private int                 index = 0;
  private boolean             open = false;
  private String              databaseLocation;
  private DBF                 judaDatabase;
  private String              username;

  /**
   * Constructor with lookBackwindow, lookForwardWindow and properties
   * containing urlStrin, password, username, outdir
   * 
   * @param lookForwardWindow
   * @param lookBackWindow
   * @param properties
   */
  public JudaCalendar (Integer lookForwardWindow, Integer lookBackWindow,
      Properties properties) {
    startLookWindow.add (java.util.Calendar.DAY_OF_YEAR, -lookBackWindow);
    endLookWindow.add (java.util.Calendar.DAY_OF_YEAR, lookForwardWindow);
    databaseLocation = properties.getProperty ("juda.calendar.database");
    username = properties.getProperty ("juda.calendar.username");
  }

  /**
   * reads the JudaCalendar and caches the items in the calendar field;
   */
  private void readCalendar () {
    logger.trace ("Entering readCalendar");
    
    JudaItem calendarItem = getFirstItem();
    while (null != calendarItem) {
      calendar.add (calendarItem);
      calendarItem= getNextItem();
    }

    logger.trace("Leaving readCalendar");
  }

  /**
   * Constructor with lookBackwindow and properties containing urlStrin,
   * password, username
   * 
   * @param lookBackWindow
   * @param properties
   */
  public JudaCalendar (Integer lookBackWindow, Properties properties) {
    this (90, lookBackWindow, properties);
  }

  /**
   * Constructor with only properties containing urlStrin, password, username
   * 
   * @param properties
   */
  public JudaCalendar (Properties properties) {
    this (90, properties);
  }

  /*
   * Get the first valid judaItem from the database file
   */
  private JudaItem getFirstItem () {
    logger.trace ("Entering getFirst");

    try {
      judaDatabase.startTop ();
    } catch (IOException e) {
      logger.warn ("IO error reading database", e);
      logger.trace ("Exiting Reset");
      return null;
    } catch (xBaseJException e) {
      logger.warn ("xBaseJExcetption while reading record", e);
      logger.trace ("Exiting Reset");
      return null;
    }
    logger.trace ("Exiting Reset");
    return getNextItem ();
  }

  /*
   * Get the next valid judaItem from the database file
   */
  private JudaItem getNextItem () {
    logger.trace ("Entering getNext");

    /**
     * Find the next agenda item within the asked window
     */
    JudaItem judaItem = null;

    while ((judaDatabase.getCurrentRecordNumber () < judaDatabase.getRecordCount ())) {
      try {
        judaDatabase.read ();
      } catch (xBaseJException e) {
        logger.warn ("End of file?", e);
        judaItem = null;
        continue;
      } catch (IOException e) {
        logger.warn ("IO exception during read of next record", e);
        judaItem = null;
        continue;
      }

      // Create JudaItem from record
      judaItem = new JudaItem (judaDatabase);

      // If judaItem is a valid item, the user matches and the record between startLookWindow 
      // and endLookWindow return item
      if ( (judaItem.valid()) && 
           (judaItem.getUser ().equalsIgnoreCase (username)) &&
           (judaItem.getStartDate().before (endLookWindow)) &&
           (judaItem.getEndDate ().after (startLookWindow)) ) {
        break;
      } else
        judaItem = null;
    }

    logger.trace ("Exiting getNext");
    return judaItem;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getById(java.lang.String)
   */
  public CalendarItem getById (String ID) {
    // read each entry until found
  JudaItem judaItem = (JudaItem) getFirst ();
    while (!(null == judaItem) && !judaItem.getID ().equals (ID))
      judaItem = (JudaItem) getNext ();
    return judaItem;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#open()
   */
  public void open () throws CalendarException {
    logger.trace ("Entering open");
    if (open) {
      logger.trace ("Exitin open because already open");
      return;
    }
    try {
      org.xBaseJ.Util.setxBaseJProperty ("ignoreMissingMDX", "true");
      judaDatabase = new DBF (databaseLocation);
      judaDatabase.startTop ();
    } catch (xBaseJException e) {
      logger.error ("Error opening Juda Agenda Database ");
      logger.info (e);
      throw new CalendarException ("Error opening Juda Agenda Database");
    } catch (IOException e) {
      logger.error ("Error opening Juda Agenda Database: ");
      logger.info (e);
      throw new CalendarException ("Error opening Juda Agenda Database");
    }
    readCalendar();
    logger.trace ("Exiting open");
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#close()
   */
  public void close () throws CalendarException {
    logger.trace ("Entering close()");
    if (null != judaDatabase)
        try {
          judaDatabase.close ();
        } catch (IOException e) {
          logger.error ("Error closing judaDatabase", e);
          throw new CalendarException (e.getMessage ());
        }
    logger.trace ("Exiting close()");
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#modify(be.CoCoCo.
   * CalendarSync.CalendarItem)
   */
  public String modify (CalendarItem item, MappingDatabase mapping) {
    logger.trace("Entering modify");    
    // store recordnumber to avoid side effects
    int recordNumber = judaDatabase.getCurrentRecordNumber ();

    // GetID
    String ID = item.getID();
    // Search for item with same ID
    JudaItem judaItem = (JudaItem) getById (ID);
    if (null != judaItem) {
      // change judaItem with item
      judaItem.modify(judaDatabase, item);
    } else {
      judaItem = (JudaItem) getById (mapping.getMapping (ID));
      if ( null != judaItem) {
        judaItem.modify(judaDatabase, item);
      } else {
        // clear the current record
        try {
          for (int i=1; i <= judaDatabase.getFieldCount (); i++)
            judaDatabase.getField (i).put ("");
          judaDatabase.write(true);
        } catch (xBaseJException e) {
          logger.error ("Error writing record to database");
          logger.info (e);
        } catch (IOException e) {
          logger.error ("Error writing record to database");
          logger.info (e); 
        }
        // add item to database
        judaItem = new JudaItem(judaDatabase, item, mapping);
      }
    }

    // reset database to recordnumber to avoid side effects
    try {
      judaDatabase.gotoRecord (recordNumber);
    } catch (xBaseJException e) {
      logger.error ("Error resetting database to recordnumber : " + recordNumber);
      logger.info (e);
    } catch (IOException e) {
      logger.error ("Error resetting database do recordnumber : " + recordNumber);
      logger.info (e);
    }    
    logger.trace("Exiting modify");
    return judaItem.getID ();
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.Calendar#getFirst()
   */
  public CalendarItem getFirst () {
    index = 0;
    return getNext();
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.Calendar#getNext()
   */
  public CalendarItem getNext () {
    if (index < calendar.size ()) return calendar.get(index++) ;
    return null;
  }
 
}
