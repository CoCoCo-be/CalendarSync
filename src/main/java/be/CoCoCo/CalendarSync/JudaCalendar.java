package be.CoCoCo.CalendarSync;
/**
 * 
 * be.CoCoCo.CalendarSync Copyright @2014 CoCoCo.be
 * 
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author Kris Cox
 * 
 */
public class JudaCalendar implements Calendar {

  // Fields
  private java.util.Calendar startLookWindow = java.util.Calendar.getInstance ();
  private java.util.Calendar endLookWindow   = java.util.Calendar.getInstance ();
  private String             databaseLocation;
  private DBF                judaDatabase;
  private boolean            open;
  private String             username;
  private String             updateFileName;
  // Maxid: next id for Calendar is maxid + 1
  private Integer            maxID = 0;

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
    updateFileName = properties.getProperty ("juda.calendar.updateFileName");
//    maxID = GetMaxID();
  }

  /**
   * @return
   */
//  private Integer GetMaxID () {
//    JudaItem judaItem = (JudaItem) getFirst ();
//    while (!(null == judaItem))
//      maxID =  Math.max(maxID, Integer.valueOf (judaItem.getID ()));
//    return maxID;
//  }

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
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getFirst()
   */
  public CalendarItem getFirst () {
    logger.trace ("Entering getFirst");

    if (!open) try {
      open ();
    } catch (CalendarException e) {
      logger.warn ("CalendarException thrown", e);
      logger.trace ("Exiting Reset");
      return null;
    }

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
    return getNext ();
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getNext()
   */
  public CalendarItem getNext () {
    logger.trace ("Entering getNext");
    if (!open) try {
      open ();
    } catch (CalendarException e) {
      logger.warn ("Can't open database", e);
      logger.trace ("Exiting getNext");
      return null;
    }

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
    this.open = true;
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
  public String modify (CalendarItem item) {
    logger.trace("Entering modify");

    boolean alreadyExists = new File(updateFileName).exists();
    String calendarItemID= String.format ("%d", ++maxID);

    try {
      CSVWriter csvUpdateFileWriter = new CSVWriter (new FileWriter(updateFileName, true), ';');

      if (! alreadyExists)
        csvUpdateFileWriter.writeNext (createHeader());

      csvUpdateFileWriter.writeNext (createRecord(item, calendarItemID));
      csvUpdateFileWriter.close();
    } catch (IOException e) {
      logger.error ("Error writing judaDatabase", e);
      System.exit (1);
    }

    logger.trace("Exiting modify");
    return calendarItemID;
  }


  /**
   * 
   * Create from and {@link CalendarItem} an array of strings with the most critical values
   * 
   * @return array of strings of crucial fields
   * 
   */
  private String[] createRecord (CalendarItem item, String itemID) {
    logger.trace ("Entering createHeader");
    String[] record = new String[6];

    // Record consist of AGUSERNS AGOMSCH AGDATUM AGBCODC AGECODC AGUNIQID
    record[0]=username;
    record[1]=item.getSummary ();

    java.util.Calendar startDate = item.getStartDate ();
    java.util.Calendar endDate = item.getEndDate ();

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");   
    record[2]=dateFormat.format(startDate);
    dateFormat = new SimpleDateFormat("yyyyMMddhhmm");
    record[3]=dateFormat.format (startDate);
    record[4]=dateFormat.format (endDate);
    record[5]=itemID;

    logger.trace("Exiting createHeader");
    return record;
  }

  /**
   * Create an array of strings with the names of the most critical values of an {@link CalendarItem}
   */
  private String[] createHeader () {
    logger.trace ("Entering createHeader");
    String[] header = new String[6];

    header[0]="AGUSERNS";
    header[1]="AGOMSCH";
    header[2]="AGDATUM";
    header[3]="AGBCODC";
    header[4]="AGECODC";
    header[5]="AGCODE";

    logger.trace("Exiting createHeader");
    return header;
  }
 
}
