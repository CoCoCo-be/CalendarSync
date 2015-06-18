/**
 *
 * CalendarSync
 * Copyright @2014 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import java.text.ParseException;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.LastModified;


/**
 * @author Kris Cox
 *
 */
public class ZarafaItemTest extends TestCase {
  
  VEvent testEvent1, testEvent2;
  ZarafaItem testItem1, testItem2;

  /**
   * @param name
   */
  public ZarafaItemTest (String name) {
    super (name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () throws Exception {
    super.setUp ();
    java.util.Calendar begin = java.util.Calendar.getInstance ();
    java.util.Calendar end = java.util.Calendar.getInstance ();
    begin.clear ();
    end.clear ();
    begin.set(2014, 9, 9, 10, 0);
    end.set(2014, 9, 9, 11, 0);
    testEvent1 = new VEvent (new DateTime (begin.getTime ()), new DateTime(end.getTime ()),"Test Event1");
    testEvent1.getProperties ().add (new LastModified (new DateTime (begin.getTime ())));
    testEvent2 = new VEvent (new DateTime(begin.getTime ()), new DateTime(end.getTime ()), "Test Event2");
    testEvent2.getProperties ().add (new LastModified (new DateTime (end.getTime ())));
    testItem1 = new ZarafaItem (testEvent1);
    testItem2 = new ZarafaItem (testEvent2);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () throws Exception {
    super.tearDown ();
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#getID()}.
   */
  public final void testGetID () {
    assertTrue (0==testItem1.getID ().compareTo (testEvent1.getUid ().getValue ().toString ()));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#isNewer(be.CoCoCo.CalendarSync.CalendarItem)}.
   */
  public final void testIsNewer () {
    assertTrue(testItem2.isNewer(testItem1));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#getComponent()}.
   */
  public final void testGetComponent () {
    assertTrue (testEvent1.equals (testItem1.getComponent ())) ;
    assertFalse(testEvent2.equals (testItem1.getComponent ())) ;
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#lastModified()}.
   * @throws ParseException 
   */
  public final void testLastModified () throws ParseException {
    java.util.Calendar checkDate = java.util.Calendar.getInstance ();
    checkDate.clear ();
    checkDate.set (2014, 9, 9, 10, 0);
    assertTrue(0==testItem1.lastModified ().compareTo (checkDate));    
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#getStartDate()}.
   * @throws ParseException 
   */
  public final void testGetStartDate () throws ParseException {
    java.util.Calendar checkDate = java.util.Calendar.getInstance ();
    checkDate.clear ();
    checkDate.set (2014, 9, 9, 10, 0);
    assertTrue (testItem1.getStartDate ().equals (checkDate));
  }
  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#getEndDate()}.
   * @throws ParseException 
   */
  public final void testGetEndDate () throws ParseException {
    java.util.Calendar checkDate = java.util.Calendar.getInstance ();
    checkDate.clear ();
    checkDate.set (2014, 9, 9, 11, 0);
    assertTrue (testItem1.getEndDate ().equals (checkDate));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#getSummary()}.
   */
  public final void testGetSummary () {
    assertTrue (0==testItem1.getSummary ().compareTo ("Test Event1"));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.ZarafaItem#equals(be.CoCoCo.CalendarSync.CalendarItem)}.
   */
  public final void testEqualsCalendarItem () {
    assertTrue(testItem1.equals (testItem1));
    assertFalse(testItem2.equals (testItem1));
  }

}
