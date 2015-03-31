/**
 * 
 * be.CoCoCo.CalendarSync Copyright @2013 CoCoCo.be
 * 
 */
package be.CoCoCo.CalendarSync;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.xBaseJ.DBF;
import org.xBaseJ.xBaseJException;
import org.xBaseJ.fields.CharField;
import org.xBaseJ.fields.DateField;
import org.xBaseJ.fields.LogicalField;

/**
 * @author Kris Cox
 * 
 */
public class JudaItem implements CalendarItem {

  // Fields
  static private DBF database;
  private String     agID;
  private String     agUser;
  private Calendar   agDate;
  private Calendar   agModified;
  private Calendar   agStartDate;
  private Calendar   agEndDate;
  private Calendar   agEndTime;
  private Calendar   agStartTime;
  private String     description;
  private String     summary;
  private String     agKind;
  private String     agSyncID;
  private boolean    agExport;
  private boolean    agTransparant;

  static String  idField         = "AGCODE";
  static String  dateField       = "AGDATUM";
  static String  endTimeField    = "AGETIJD";
  static String  startTimeField  = "AGBTIJD";
  static String  changeDateField = "AGCHADAT";    // datefield
  static String  summaryField    = "AGSRTNRN";
  static String  kindField       = "AGSRTNR";
  static String  descriptionField= "AGOMSCH";
  static String  dossierField    = "AGDOSSIER";
  static String  userField       = "AGUSERNS";
  static String  exportField     = "AGEXPORT";
  static String  syncField       = "AGOUTLID";
  static String  temporaryField  = "AGVOORL";

  static String  dateFormatYMdhm = "yyyyMMddhhmm";
  static String  dateFormatYMd   = "yyyyMMdd";
  static String  dateFormatMDY   = "MM/dd/yy";
  static String  dateFormatDMY   = "dd/MM/yy";
  static String  timeFormatHHMM  = "hh:mm";

  /**
   * constructor of an judaItem
   * 
   * @param judaDatabase
   *          database positioned on the record to read.s
   */
  public JudaItem (DBF judaDatabase) {
    database = judaDatabase;
    agID = readCharField (idField);
    agDate = readCalendarField (dateField, dateFormatYMd);
    agEndTime = readTimeField(endTimeField, timeFormatHHMM);
    agStartTime = readTimeField(startTimeField, timeFormatHHMM);
    agModified = readCalendarField (changeDateField, dateFormatYMd);
    agExport = readLogicalField (exportField);
    agTransparant = readLogicalField (temporaryField);
    agKind = readCharField (kindField);
    String agSummary = readCharField (summaryField);
    String agDescription = readCharField (descriptionField);
    String agDossier = readCharField (dossierField);
    agUser = readCharField (userField);
    agSyncID = readCharField (syncField);

    if (! (null == agDate)) {
      if ((null == agStartTime) || (null == agEndTime)) {
        agEndDate = null;
        agStartDate = agDate;
        agTransparant = true;
      } else {
        agStartDate = Calendar.getInstance ();
        agStartDate.setTimeInMillis (agDate.getTimeInMillis () + agStartTime.getTimeInMillis ());;
        agEndDate = Calendar.getInstance ();
        agEndDate.setTimeInMillis (agDate.getTimeInMillis () + agEndTime.getTimeInMillis ());
      }
    }
    
    if (agID.isEmpty ()) {
      String agIDString = agDossier + judaDatabase.getCurrentRecordNumber ();
      agID = String.format("%x", new BigInteger(agIDString.getBytes()));
    }
    
    if (0 == agSyncID.length()) agSyncID=null;
    
    description = agDescription;
    summary = agDossier + " - " + agSummary;
  }

  /**
   * @return if this element contains valid information
   */
  public boolean valid() {
    logger.trace("valid");
    Integer kind;
    try {
      kind = Integer.decode (agKind);
    } catch (NumberFormatException e) {
      return false;
    }
    switch (kind) {
      case   0:
      case   1:
      case  -3:
      case  10:
      case  11:
      case  12:
      case  13:
      case  14:
      case  26:
      case  31:
      case  32:
      case  56:
      case  58:
      case  60:
      case  64:
      case  29:
      case  78:
      case  82:
      case  86:
      case 113:
      case 114:
      case 117:
        break;
      default: return false;
    }
    // Not all values are null
    return (!((null == agID) && (null == summary) && (null == agUser) &&
        (null == agModified ) && (null == agStartDate) && // (null == agEndCalendar) &&
        (null == description) && (false == agExport)));
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getID()
   */
  public String getID () {
    logger.trace ("getID");
    if (null == agSyncID) {
       logger.trace("Create new ID");
       agSyncID = agID + "@CoCoCo.be";
       writeCharField(syncField, agSyncID);
    } 
    return agSyncID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#isNewer(be.CoCoCo.
   * CalendarSync.CalendarItem)
   */
  public boolean isNewer (CalendarItem calendarItem) {
    logger.trace ("Entering isNewer");
    Calendar lastModified1 = lastModified (), lastModified2 = calendarItem.lastModified ();
    boolean result;
    if (null == lastModified1) result = false;
    else if (null == lastModified2) result = true;
    else result = lastModified ().before (calendarItem.lastModified ());
    logger.trace ("Exiting isNewer");
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#lastModified()
   */
  public Calendar lastModified () {
    logger.trace ("Entering lastModified");
    return agModified;

  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getStartCalendar()
   */
  public Calendar getStartDate () {
    logger.trace ("getStartDate");

    return agStartDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getEndCalendar()
   */
  public Calendar getEndDate () {
    logger.trace ("getEndDate");
    if (null==agEndDate) 
      return agStartDate;
    else 
      return agEndDate;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getSummary()
   */
  public String getSummary () {
    logger.trace ("getSummary");

    return summary;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getDescription()
   */
  public String getDescription () {
    logger.trace ("getDescription");

    return description;
  }

  /**
   * 
   * Return the user of the agenda Item
   * 
   * @return name of user of agenda Item
   */
  public String getUser () {
    logger.trace ("getUser");

    return agUser;
  }
  
  /**
   * Reads a data from a juda Database into Calendar value
   * 
   * @param fieldName Name of field to be read
   * @param format Format of datefield
   * @return {@link Calendar} value from field with name fieldName
   */
  private Calendar readCalendarField (String fieldName, String format) {
    logger.trace ("Entering readDataField");

    DateField agCalendar = null;
    try {
      agCalendar = (DateField) database.getField (fieldName);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.error ("Array index out of bound");
      logger.info (e);
      agCalendar = null;
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      agCalendar = null;
    }

    Calendar result = null;
    if (null != agCalendar) {
      String dateString = null;
      dateString = new String (agCalendar.get ()).trim ();
      if (0 != dateString.length ()) {
        // Parse Calendar
        DateFormat formater = new SimpleDateFormat (format);
        try {
          result = Calendar.getInstance ();
          result.setTimeInMillis (formater.parse (dateString).getTime());
        } catch (ParseException e) {
          logger.error ("Error parsing date", e);
          logger.info (e);
          result = null;
        }
      }
    }

    logger.trace ("Exiting readDataField");
    return result;
  }

  /**
   * @param endTimeField2
   * @param timeFormatHHMM2
   * @return
   */
  private Calendar readTimeField (String fieldName, String format) {
    logger.trace ("Entering readTimeField");
    
    CharField agTime = null;
    try {
      agTime = (CharField) database.getField (fieldName);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.error ("Array index out of bound");
      logger.info (e);
      agTime = null;
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      agTime = null;
    }

    Calendar result = null;
    if (null != agTime) {
      String timeString = null;
      timeString = new String (agTime.get ()).trim ();
      // if timeString is "99:99" it is a full day event
      if (! timeString.equals ("99:99") &&
          (0 != timeString.length ())) {
        // Parse Calendar
        DateFormat formater = new SimpleDateFormat(format);
        formater.setTimeZone (TimeZone.getTimeZone ("CEST"));
        try {
          result = Calendar.getInstance ();
          result.setTimeInMillis(formater.parse (timeString).getTime ());
        } catch (ParseException e) {
          logger.error ("Error parsing date", e);
          logger.info (e);
          result = null;
        }
      }
    }

    logger.trace ("Exiting readDataField");
    return result;
  }
  
  /**
   * read character field from database
   * 
   * @param fieldName
   *          the name of the field
   * @return Value of the field
   */
  private String readCharField (String fieldName) {

    logger.trace ("Entering readCharField");

    CharField agField = null;
    try {
      agField = (CharField) database.getField (fieldName);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.error ("Array index out of bound");
      logger.info (e);
      agField = null;
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      agField = null;
    }

    String result = null;
    if (null != agField) result = new String (agField.get ()).trim ();
    logger.trace ("Exiting readField");
    return result;
  }

  /**
   * Write character field to Database
   * 
   * @param sting
   */
  private void writeCharField (String fieldName, String value) {
    logger.trace ("Entering writeSyncID");
    
    CharField agField = null;

    try {
      agField = (CharField) database.getField (fieldName);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.error ("Array index out of bound");
      logger.info (e);
      return;
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      return;
    }

    if (null == agField) return;
    try {
      agField.put (value);
      database.update ();
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      return;
    } catch (IOException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      return;
    }
  }
  
  /**
   * read boolean field from database
   * 
   * @param fieldName
   *          the name of the field
   * @return Value of the field
   */
  private boolean readLogicalField (String fieldName) {

    logger.trace ("Entering readLogicalField");

    LogicalField agField = null;
    try {
      agField = (LogicalField) database.getField (fieldName);
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.error ("Array index out of bound");
      logger.info (e);
      agField = null;
    } catch (xBaseJException e) {
      logger.error ("Error reading field summary");
      logger.info (e);
      agField = null;
    }

    Boolean result = null;
    if (null != agField) result = agField.getBoolean ();
    logger.trace ("Exiting readField");
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#equals(be.CoCoCo.
   * CalendarSync.CalendarItem)
   */
  public boolean equals (CalendarItem calendarItem) {
    logger.trace ("Entering Equals");
    if (!(calendarItem instanceof JudaItem)) {
      logger.debug ("Wrong type");
      return false;
    }
    JudaItem item = (JudaItem) calendarItem;
    if (!item.getID ().equals (getID())) {
      logger.debug ("Wrong ID");
      return false;
    }
    if (!item.getSummary ().equals (summary)) {
      logger.debug ("Wrong summary");
      return false;
    }
    if (!item.getDescription().equals (description)) {
      logger.debug ("Wrong description");
      return false;
    }
    if (!item.getUser ().equals (agUser)) {
      logger.debug ("Wrong user");
      return false;
    }
    if (!item.getStartDate ().equals (agStartDate)) {
      logger.debug ("Wrong startdate");
      return false;
    }
    if (!item.getEndDate ().equals (agEndDate)) {
      logger.debug ("Wrong enddate");
      return false;
    }
    if (!item.lastModified ().equals (agModified)) {
      logger.debug ("Wrong modification date");
      return false;
    }
    logger.trace ("Exiting equals with true");
    return true;
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.CalendarItem#allDayEvent()
   */
  public boolean isTransparent () {
    return agTransparant;
  }

}
