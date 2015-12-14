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
class CalendarSync {
  
  // mapping database
  private static final MappingDatabase mapping = new MappingDatabase();
  
  // Define log4j Logger
  static Logger logger = Logger.getLogger (CalendarSync.class);
  private static File propertiesFile = null;
  private static Properties properties;
  private static Calendar calendar1;
  private static Calendar calendar2;

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
    FileInputStream propertiesStream = null;
    try {
      propertiesStream = new FileInputStream (optionFiles);
      properties.load (propertiesStream);
    } catch (IOException e) {
      logger.fatal ("Error in properties file");
      logger.info (e);
      throw new RuntimeException ("Error in properties file");
    } finally {
      try {
        if (null != propertiesStream ) propertiesStream.close();
      } catch (IOException e) {
        logger.fatal ("Error in properties file");
        logger.info (e);
      }
    }
    logger.trace("Exiting initialisation");
  }

  /**
   * 
   */
  private static void initalize () {
    logger.trace ("Entering initialisation");
    
    String propertyValue = properties.getProperty ("lookForwardWindow", "90");
    Integer lookForwardWindow = Integer.valueOf (propertyValue);
    propertyValue = properties.getProperty ("lookBackWindow", "90");
    Integer lookBackWindow = Integer.valueOf(propertyValue);

    calendar1 = new JudaCalendar (lookForwardWindow, lookBackWindow, properties);
    calendar2 = new ZarafaCalendar (lookForwardWindow, lookBackWindow, properties);
    try {
      calendar1.open ();
      calendar2.open ();
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
    
    CalendarItem targetItem = null, sourceItem = calendar1.getFirst ();
    
    while (null != sourceItem) {
      String targetID, sourceID = sourceItem.getID ();
      targetID = mapping.getMapping (sourceID);
      if ( null != targetID ) 
        targetItem = calendar2.getById (targetID);

      if (null == targetID || null == targetItem || sourceItem.isNewer (targetItem)) {
        logger.trace ("CalendarItem " + sourceItem.getID () + " added te other Calendar");
        targetID = calendar2.modify (sourceItem, targetID);
        mapping.addMapping (sourceID, targetID);
      }
      sourceItem = calendar1.getNext ();
    }
    
    sourceItem = calendar2.getFirst ();
    
    while (null != sourceItem) {
      String targetID, sourceID = sourceItem.getID ();
      targetID = mapping.getMapping (sourceID);
      if ( null != targetID)
        targetItem = calendar1.getById (targetID);

      if (null == targetID || null == targetItem || sourceItem.isNewer (targetItem)) {
        logger.trace ("CalendarItem " + sourceItem.getID () + " added te other Calendar");
        targetID = calendar1.modify (sourceItem, targetID);
        mapping.addMapping (sourceID, targetID);
      }
      sourceItem = calendar2.getNext ();
    }
    
    logger.trace ("Exiting synchronize");
  }

  /**
   * 
   */
  private static void cleanUp () {
    logger.trace ("Start cleanUp");
    try {
      calendar1.close ();
      calendar2.close ();
      mapping.close ();
    } catch (CalendarException e) {
      logger.error ("Error Closing calendars. Synchronisation may be failed");
      logger.info (e);
    } 
    logger.trace ("End CleanUp");

  }

}
