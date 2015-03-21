/**
 * 
 * be.CoCoCo.CalendarSync Copyright @2013 CoCoCo.be
 * 
 */
package be.CoCoCo.CalendarSync;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.filter.Rule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * @author Kris Cox
 * 
 */
public class ZarafaCalendar implements be.CoCoCo.CalendarSync.Calendar {

  // Fields
  private Integer     lookForward;
  private Integer     lookBack;
  private String      urlString;
  private String      password;
  private String      username;
  private Calendar    calendar = null;
  private Filter      filter;
  private String      outDir   = null;
  private Iterator<?> calendarIterator;

  // Define log4j Logger
  static Logger       logger   = Logger.getLogger (ZarafaCalendar.class);

  /**
   * Constructor with only properties containing urlStrin, password, username
   * 
   * @param properties
   */
  ZarafaCalendar (Properties properties) {
    this (90, properties);
  }

  /**
   * Constructor with lookBackwindow and properties containing urlStrin,
   * password, username
   * 
   * @param lookBackWindow
   * @param properties
   */
  ZarafaCalendar (Integer lookBackWindow, Properties properties) {
    this (90, lookBackWindow, properties);
  }

  /**
   * Constructor with lookBackwindow, lookForwardWindow and properties
   * containing urlStrin, password, username, outdir
   * 
   * @param lookForwardWindow
   * @param lookBackWindow
   * @param properties
   */
  ZarafaCalendar (Integer lookForwardWindow, Integer lookBackWindow, Properties properties) {
    lookForward = lookForwardWindow;
    lookBack = lookBackWindow;
    urlString = properties.getProperty ("zarafa.calendar.url");
    password = properties.getProperty ("zarafa.calendar.password");
    username = properties.getProperty ("zarafa.calendar.username");
    outDir = properties.getProperty ("zarafa.calendar.outputDirectory", "");
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getFirst()
   */
  public CalendarItem getFirst () {
    logger.trace ("Entering reset");

    if (null != calendarIterator) calendarIterator.remove ();
    if (null == calendar) {
      calendarIterator = null;
      return null;
    } else calendarIterator = filter.filter (calendar.getComponents ()).iterator ();

    logger.trace ("Exiting reset");
    return getNext ();
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getNext()
   */
  public CalendarItem getNext () {
    logger.trace ("Entering getNext");

    if (null == calendarIterator) {
      getFirst ();
    }

    if (calendarIterator.hasNext ()) {
      Component component = (Component) calendarIterator.next ();
      if (component instanceof VEvent) {
        logger.trace ("Exiting getNext");
        return new ZarafaItem (component);
      } else {
        logger.info ("Not an VEvent");
        logger.trace ("Exiting getNext");
        return getNext ();
      }
    } else {
      // calendarIterator.remove();
      calendarIterator = null;
      logger.info ("No more items, returning null");
      logger.trace ("Exiting getNext");
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#getById(java.lang.String)
   */
  public CalendarItem getById (String ID) {
    logger.trace ("Entering getById");
    CalendarItem item = getFirst ();
    while (null != item && !ID.equals (item.getID ())) {
      item = getNext ();
    }
    logger.trace ("Exiting getById");
    return item;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#open()
   */
  public void open () throws CalendarException {
    logger.trace ("Entering open");
    URL url = null;
    // Open the URL
    try {
      url = new URL (urlString);
    } catch (MalformedURLException e) {
      logger.error ("Error occured during opening of calendar : " + url);
      logger.info (e);
      logger.error ("Exit program");
      System.exit (3);
    }

    // Create authentication
    String userPassword = username + ":" + password;
    new Base64 ();
    String encoding = Base64.encodeBase64String (userPassword.getBytes ()).replaceAll (
        "\r\n", "");

    // Read the calendar
    try {
      URLConnection uCon = url.openConnection ();
      uCon.setRequestProperty ("Authorization", "Basic " + encoding);
      InputStream in = uCon.getInputStream ();
      CalendarBuilder builder = new CalendarBuilder ();
      calendar = builder.build (in);
    } catch (IOException e) {
      logger.error ("Error reading url : " + url);
      logger.info (e);
      System.exit (2);
    } catch (ParserException e) {
      logger.error ("Error parsing calendar items from url : " + url);
      logger.info (e);
      System.exit (2);
    }

    // Set the period filter
    java.util.Calendar beginWindow = java.util.Calendar.getInstance ();
    beginWindow.add (java.util.Calendar.DATE, -lookBack);
    java.util.Calendar endWindow = java.util.Calendar.getInstance ();
    endWindow.add (java.util.Calendar.DATE, lookForward);

    // create filter
    Period period = new Period (new DateTime (beginWindow.getTime ()), new DateTime (
        endWindow.getTime ()));
    Rule[] rules = new Rule[1];
    rules[0] = new PeriodRule (period);
    filter = new Filter (rules, Filter.MATCH_ALL);

    logger.trace ("Exiting open");

  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#close()
   */
  public void close () throws CalendarException {
    logger.trace ("Entering close");

    String fileName = outDir + "/" + username + ".ics";

    if (null != calendarIterator) calendarIterator.remove ();
    if (null != calendar && 1 <= calendar.getComponents ().size ()) {
      // Local variables
      OutputStream out = null;
      CalendarOutputter outputter = new CalendarOutputter ();
      CompatibilityHints.setHintEnabled ("ical4j.parsing.relaxed", true);

      // Write the calendar to file
      try {
        out = new FileOutputStream (fileName);
        outputter.output (calendar, out);
        out.close ();
      } catch (IOException e) {
        logger.error ("Error writing to file : " + fileName);
        logger.info (e);
        System.exit (2);
      } catch (ValidationException e) {
        logger.error ("Error writing calendar items to file : " + fileName);
        logger.info (e);
        System.exit (2);
      }
    }
    logger.trace ("Exiting Close");

  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.Calendar#modify(be.CoCoCo.
   * CalendarSync.CalendarItem)
   */
  public String modify (CalendarItem item) {
    logger.trace ("Entering modify");
//    Boolean lastModified = false;
    String uID = item.getID ();
    if (null == uID) {
      Uid newID = new Uid ();
      newID.setValue ("CoCoCo-" + UUID.randomUUID ());
      uID = newID.getValue ();
    } else {
      logger.debug ("Uid already existed, updating existing");
    }

    // Remove item if already in Calendar
    CalendarItem existingItem = getById (uID);
    if (null != existingItem) {
      calendar.getComponents ().remove (((ZarafaItem) existingItem).GetComponent ());
    }

    // Create new event
    ZarafaItem zarafaItem = new ZarafaItem (item);
//    VEvent newEvent = null;
//    java.util.Calendar startDate = item.getStartDate ();
//    java.util.Calendar endDate = item.getEndDate ();
//
//    String summary = item.getSummary ();
//    
//    if (null != startDate) {
//      if (null != endDate) {
//        DateTime start = new DateTime(startDate.getTime ());
//        DateTime end = new DateTime(endDate.getTime ());
//        newEvent = new VEvent(start, end, summary);
//      } else {
//        Date start = new Date (startDate.getTime());
//        newEvent = new VEvent (start, summary);
//      }
//    }
//    
//    PropertyList properties = newEvent.getProperties ();
//
//    java.util.Calendar modified = item.lastModified ();
//    if (null != modified) {
//      DateTime modifiedDT = new DateTime (modified.getTime ());
//      LastModified lastModifiedDate = new LastModified (modifiedDT);
//      properties.add (lastModifiedDate);
//      lastModified = true;
//    }
//
//    String uidString = item.getID ();
//    if (null != uidString) {
//      properties.add (new Uid (uidString));
//    }
//
//     add modified date
//    java.util.Calendar now = java.util.Calendar.getInstance ();
//    DateTime nowDate = new DateTime (now.getTime ());
//    if (!lastModified) {
//      properties.add (new LastModified (nowDate));
//    }
//    properties.add (new DtStamp ());

//    calendar.getComponents ().add (newEvent);
    calendar.getComponents ().add (zarafaItem.GetComponent ());

    logger.trace ("Exiting modify");
    return uID;
  }

}
