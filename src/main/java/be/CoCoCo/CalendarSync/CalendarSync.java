/**
 *
 * CalendarSync Copyright @2014 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import be.CoCoCo.CalendarSync.CalendarException;
import be.CoCoCo.CalendarSync.ZarafaCalendar;

/**
 * @author Kris Cox
 *
 */
public class CalendarSync {

  // Define log4j Logger
  static Logger logger = Logger.getLogger (ZarafaCalendar.class);
  private static File propertiesFile = null;
  private static Properties properties;
  private static Calendar sourceCalendar;
  private static ZarafaCalendar targetCalendar;

  /**
   * @param args
   */
  public static void main (String[] args) {
    logger.trace ("Start MAIN");
    
    logger.trace ("parsing arguments");
    if ((2 != args.length) || (!args[0].equals ("-f")))  {
      logger.fatal ("Usage: JudaToZarafa -f properties-files");
      System.exit (1);
    } else {
      propertiesFile = new File (args[1]);
      if  (!(propertiesFile.isFile ())) {
      logger.fatal ("Properties file non existing");
      logger.info ("Properties file :" + args[1]);
      System.exit (2);
      }
    }
    logger.trace ("Exiting parsing arguments");
  
    parseOptions (args[1]);
    
    initalize ();
    
    synchronize ();
    
    cleanUp ();

    logger.trace ("end MAIN");
  }

  /**
   * @param string
   */
  private static void parseOptions (String optionFiles) {
    logger.trace ("Entering parseOptions");
    properties = new Properties ();
    try {
      FileInputStream propertiesStream = new FileInputStream (optionFiles);
      properties.load (propertiesStream);
      propertiesStream.close ();
    } catch (IOException e) {
      logger.fatal ("Error in properties file");
      logger.info (e);
      System.exit (1);
    }
    logger.trace("Exiting initialisation");
  }

  /**
   * 
   */
  private static void initalize () {
    logger.trace ("Entering initialisation");
    
    String propertyValue = properties.getProperty ("lookForwardWindow", "90");
    Integer lookForwardWindow = new Integer(propertyValue);
    propertyValue = properties.getProperty ("lookBackWindow", "90");
    Integer lookBackWindow = new Integer(propertyValue);

    sourceCalendar = new JudaCalendar (lookForwardWindow, lookBackWindow, properties);
    targetCalendar = new ZarafaCalendar (lookForwardWindow, lookBackWindow, properties);
    try {
      sourceCalendar.open ();
      targetCalendar.open ();
    } catch (CalendarException e) {
      logger.fatal ("Error opening calendars");
      logger.info (e);
      System.exit (1);
    }

    logger.trace("Exiting initialisation");
  }

  /**
   * 
   */
  private static void synchronize () {
    logger.trace ("Entering synchronize");
    
    CalendarItem targetItem, sourceItem = sourceCalendar.getFirst ();
    
    while (null != sourceItem) {
      String sourceID = sourceItem.getID ();
      targetItem = targetCalendar.getById (sourceID);
      if (null == targetItem || sourceItem.isNewer (targetItem)) {
        logger.trace ("CalendarItem " + sourceItem.getID () + " added te other Calendar");
        targetCalendar.modify (sourceItem);
      }
      sourceItem = sourceCalendar.getNext ();
    }
    
    logger.trace ("Exiting synchronize");
  }

  /**
   * 
   */
  private static void cleanUp () {
    logger.trace ("Start cleanUp");
    try {
      sourceCalendar.close ();
      targetCalendar.close ();
    } catch (CalendarException e) {
      logger.error ("Error Closing calendars. Synchronisation may be failed");
      logger.info (e);
    } 
    logger.trace ("End CleanUp");

  }

}
