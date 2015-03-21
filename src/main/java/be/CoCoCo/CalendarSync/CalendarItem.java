/**
 * 
 * be.CoCoCo.CalendarSync- Copyright @2013 CoCoCo.be
 * 
 */
package be.CoCoCo.CalendarSync;

import java.util.Calendar;

import org.apache.log4j.Logger;

/**
 * @author Kris Cox
 * 
 */
class CalendarItemException extends Exception {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -5718740219462162991L;

  public CalendarItemException (String msg) {
    super (msg);
  }
}

/**
 * @author Kris Cox
 * 
 */
interface CalendarItem {

  /**
   * Define log4j Logger for logging purposes
   */
  static Logger logger = Logger.getLogger (CalendarItem.class);

  /**
   * Return the ID of the calendarItem
   * 
   * @return return the ID
   */
  public String getID ();

  /**
   * Check if the item is newer or older
   * 
   * @return true if this item is newer, else false
   * 
   */
  public boolean isNewer (CalendarItem calendarItem);
  
  /**
   * Return the modification date of the item
   * 
   * @return date of modification
   * 
   */
  public Calendar lastModified ();

  /**
   * Return start date of the item
   * 
   * @return start date of the item
   */
  public Calendar getStartDate ();

  /**
   * Return end date of the item
   * 
   * @return end date of the item
   */
  public Calendar getEndDate ();

  /**
   * Return if event takes all day
   * 
   * @return transparant value
   */
  public boolean isTransparent ();

  /**
  /**
   * Return short description of item
   * 
   * @return short description of item
   */
  public String getSummary ();
  
  /**
   * Return the full description of item
   * 
   * @return full description of item
   */
  public String getDescription ();
  
  /**
   * check if calendar items are the same
   * 
   * @return true if equal, otherwise false
   */
  public boolean equals (CalendarItem calendarItem);

}
