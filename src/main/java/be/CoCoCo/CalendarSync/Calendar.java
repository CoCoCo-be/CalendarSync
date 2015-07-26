/**
 * be.CoCoCo.CalendarSync Copyright @2014 CoCoCo.be
 */
package be.CoCoCo.CalendarSync;

import org.apache.log4j.Logger;

/**
 * @author Kris Cox
 * 
 */
class CalendarException extends Exception {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -5718740219462162991L;

  public CalendarException (String msg) {
    super (msg);
  }
}

/**
 * @author Kris Cox
 * 
 */
interface Calendar {

  /**
   * Define log4j Logger for logging purposes
   */
  static Logger logger = Logger.getLogger (Calendar.class);

  /**
   * Get the <b>first</b> calendar item in the specified range
   * 
   * @return the first calendar item in the range or null of no item in
   *         range.
   * 
   */
  public CalendarItem getFirst ();

  /**
   * Get the next calendar item in the specified range
   * 
   * @return the next calendar item in the range or null if none exists.
   * 
   */
  public CalendarItem getNext ();

  /**
   * Get an specific calendar item from the calendar
   * 
   * @param ID
   *          Unique ID from a calendar item
   * @return The calendar item with the specific ID or null if no item with
   *         specific ID exists
   * 
   */
  public CalendarItem getById (String ID);

  /**
   * Open the calendar from the source
   * 
   * @throws CalendarException
   */
  public void open () throws CalendarException;

  /**
   * Close the calendar and write changes to the source
   * 
   * @throws CalendarException
   */
  public void close () throws CalendarException;

  /**
   * Modify item in calendar or add item to calendar if it not already exists in
   * calendar and returns the new ID for the calendar item.
   * 
   * @param item
   *          CalendarItem to change
   *          
   * @return The new ID for the calendar item
   */
  public String modify (CalendarItem item, MappingDatabase mapping);

}
