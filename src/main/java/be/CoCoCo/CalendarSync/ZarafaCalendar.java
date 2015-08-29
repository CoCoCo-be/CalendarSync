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
import java.io.UnsupportedEncodingException;
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
class ZarafaCalendar implements be.CoCoCo.CalendarSync.Calendar {

  // Define log4j Logger
  static Logger logger = Logger.getLogger (ZarafaCalendar.class);

  // Fields
  private Integer     lookForward;
  private Integer     lookBack;
  private String      urlString;
  private String      password;
  private String      username;
  private Calendar    calendar = null;
  private Filter      filter;
  private String      outDir   = null;
  private Iterator<Component> calendarIterator;

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
  @SuppressWarnings ("unchecked")
  public CalendarItem getFirst () {
    logger.trace ("Entering reset");

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
      Component component = calendarIterator.next ();
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
      throw new RuntimeException ("Zarafa calendar error see logfiles");
    }

    // Create authentication
    String userPassword = username + ":" + password;
    new Base64 ();
    String encoding;
    try {
      encoding = Base64.encodeBase64String (userPassword.getBytes ("ISO8859_15")).replaceAll (
          "\r\n", "");
    } catch (UnsupportedEncodingException e) {
      logger.error ("Error encoding password");
      logger.info (e);
      throw new RuntimeException ("Error encoding password while opening zarafa calendar"); 
    }

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
      throw new RuntimeException ("Can't read calendar " + url); 
    } catch (ParserException e) {
      logger.error ("Error parsing calendar items from url : " + url);
      logger.info (e);
      throw new RuntimeException ("Can't read calendar " + url);
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
  @SuppressWarnings ("unchecked")
  public void close () throws CalendarException {
    logger.trace ("Entering close");

    String fileName = outDir + "/" + username + ".ics";

    if (null != calendarIterator) calendarIterator = filter.filter (calendar.getComponents ()).iterator (); 
    if (null != calendar && 1 <= calendar.getComponents ().size ()) {
      // Local variables
      CalendarOutputter outputter = new CalendarOutputter ();
      CompatibilityHints.setHintEnabled ("ical4j.parsing.relaxed", true);

      // Write the calendar to file
      OutputStream out = null;
      try {
        out = new FileOutputStream (fileName);
        outputter.output (calendar, out);
      } catch (IOException e) {
        logger.error ("Error writing to file : " + fileName);
        logger.info (e);
        throw new RuntimeException ("Not synced to zarafa");
      } catch (ValidationException e) {
        logger.error ("Error writing calendar items to file : " + fileName);
        logger.info (e);
        throw new RuntimeException ("Not synced to zarafa");
      } finally {
        try {
          if ( null != out) out.close();
        } catch ( IOException e) {
          logger.error ("Error writing calendar items to file : " + fileName);
          logger.info (e);
        }
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
  public String modify (CalendarItem item, MappingDatabase mapping) {
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
    Iterator<Component> keepIterator = calendarIterator;
    calendarIterator = null;
    CalendarItem existingItem = getById (uID);
    if (null != existingItem) {
      calendar.getComponents ().remove (((ZarafaItem) existingItem).getComponent ());
    } else {
      String newUID = mapping.getMapping (uID);
      if (null != newUID)
        existingItem = getById (newUID);
      else existingItem = null;
      if (null != existingItem)
        calendar.getComponents ().remove (((ZarafaItem) existingItem).getComponent ());
    }
    calendarIterator = keepIterator;

    // Create new event
    ZarafaItem zarafaItem = new ZarafaItem (item);
    calendar.getComponents ().add (zarafaItem.getComponent ());

    logger.trace ("Exiting modify");
    return uID;
  }

}
