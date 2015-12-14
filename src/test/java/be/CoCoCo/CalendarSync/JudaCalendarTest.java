/**
 *
 * CalendarSync
 * Copyright @2014 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.util.Properties;

import junit.framework.TestCase;


/**
 * @author Kris Cox
 *
 */
public class JudaCalendarTest extends TestCase {

  private JudaCalendar judaCalendar;
  private Properties properties;

  /**
   * @param name
   */
  public JudaCalendarTest (String name) {
    super (name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () throws Exception {
    super.setUp ();
    properties = new Properties();
    properties.setProperty ("juda.calendar.database", "src/test/resources/AGENDA.DBF");
    properties.setProperty ("juda.calendar.updateFileName", "/tmp/test.csv");
    properties.setProperty ("juda.maxID", "10000");    
    properties.setProperty ("juda.calendar.username", "ERICWOUTERS");
    judaCalendar = new JudaCalendar(360,360,properties);
    try {
      judaCalendar.open ();
    } catch (CalendarException e) {
      fail ("Open failed");
    }
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () throws Exception {
    super.tearDown ();
    if (null != judaCalendar) 
      try {
        judaCalendar.close ();
      } catch (CalendarException e) {
        fail ("Close failed");
      }
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaCalendar#getFirst()}.
   */
  public final void testGetFirstNext () {
    // Test getFirst()
    CalendarItem item = judaCalendar.getFirst ();
    assertNotNull (item);
    assertEquals ("2015/0023-0 - TDD - Vervaltermijn - laatste dag nadert", item.getSummary ());
    // Test getNext()
    item=judaCalendar.getNext ();
    assertNotNull (item);
    assertEquals ("2008/0186-0 - AF - Bespreking (elders) :", item.getSummary ());
}

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaCalendar#getById(java.lang.String)}.
   */
  public final void testGetById () {
    CalendarItem item = judaCalendar.getFirst ();
    item = judaCalendar.getNext ();
    String ID = item.getID ();
    String summary = item.getSummary ();
    item = judaCalendar.getNext ();
    item = judaCalendar.getById (ID);
    assertEquals (summary, item.getSummary ());;
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaCalendar#modify(be.CoCoCo.CalendarSync.CalendarItem)}.
   */
  public final void testModify () {
//    fail ("Not yet implemented");
  }

}
