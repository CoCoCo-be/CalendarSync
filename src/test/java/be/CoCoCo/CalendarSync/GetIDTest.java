/**
 *
 * CalendarSync
 * Copyright @2015 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.util.Properties;

import junit.framework.TestCase;


/**
 * @author Kris Cox
 *
 */
public class GetIDTest extends TestCase {

  private ZarafaCalendar calendar;

  /**
   * @param name
   */
  public GetIDTest (String name) {
    super (name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () throws Exception {
    super.setUp ();
    Properties properties = new Properties ();
    properties.setProperty ("zarafa.calendar.url", "http://zarafa.cococo.be:8080/ical/" );
    properties.setProperty ("zarafa.calendar.username", "test");
    properties.setProperty ("zarafa.calendar.password", "YMuOPPxb");
    properties.setProperty ("zarafa.calendar.outputDirectory", "/Users/krisc");
    calendar = new ZarafaCalendar (90, 10, properties);
    calendar.open();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () throws Exception {
    super.tearDown ();
  }

  public final void testGetID () {
    //assertNotNull (calendar.getById ("CoCoCo-8f97d6a2-2616-4ff4-a04e-4bef91817766"));;
  }
}
