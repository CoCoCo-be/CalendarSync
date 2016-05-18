/**
 *
 * CalendarSync
 * Copyright @2014 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import junit.framework.TestCase;

import org.xBaseJ.DBF;


/**
 * @author Kris Cox
 *
 */
public class JudaItemTest extends TestCase {

  private JudaItem judaItem1, judaItem2;

  /**
   * @param name
   */
  public JudaItemTest (String name) {
    super (name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () throws Exception {
    super.setUp ();
    org.xBaseJ.Util.setxBaseJProperty ("ignoreMissingMDX", "true");
    DBF judaDatabase = new DBF("src/test/resources/AGENDA.DBF");
    judaDatabase.startTop ();
    judaDatabase.read ();
    judaItem1 = new JudaItem(judaDatabase);
    judaDatabase.read ();
    judaItem2 = new JudaItem(judaDatabase);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () throws Exception {
    super.tearDown ();
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#valid()}.
   */
  public final void testValid () {
    assertFalse (judaItem1.valid ());
    assertTrue (judaItem2.valid ());
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#getID()}.
   */
  public final void testGetID () {
    assertNotNull (judaItem1.getID ());
    assertNotNull (judaItem2.getID ());
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#isNewer(be.CoCoCo.CalendarSync.CalendarItem)}.
   */
  public final void testIsNewer () {
    assertFalse (judaItem1.isNewer (judaItem2));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#lastModified()}.
   */
  public final void testLastModified () {
    java.util.Calendar checkDate = java.util.Calendar.getInstance ();
    checkDate.clear ();
    checkDate.set (1999, 8, 17);
    assertTrue (0 == judaItem1.lastModified ().compareTo (checkDate));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#getStartDate()}.
   */
  public final void testGetStartDate () {
    java.util.Calendar checkDate1 = java.util.Calendar.getInstance ();
    java.util.Calendar checkDate2 = java.util.Calendar.getInstance ();
    checkDate1.clear ();
    checkDate2.clear ();
    checkDate1.set (1999, 3, 27, 9, 0, 0);
    checkDate2.set (1999, 8, 21, 14, 0, 0);
    assertTrue ( 0 == judaItem1.getStartDate ().compareTo (checkDate1));
    assertTrue ( 0 == judaItem2.getStartDate ().compareTo (checkDate2));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#getEndDate()}.
   */
  public final void testGetEndDate () {
    java.util.Calendar checkDate1 = java.util.Calendar.getInstance ();
    java.util.Calendar checkDate2 = java.util.Calendar.getInstance ();
    checkDate1.clear ();
    checkDate2.clear ();
    checkDate1.set (1999, 3, 27, 10, 0, 0);
    checkDate2.set (1999, 8, 21, 15, 0, 0);
    assertTrue ( 0 == judaItem1.getEndDate ().compareTo (checkDate1));
    assertTrue ( 0 == judaItem2.getEndDate ().compareTo (checkDate2));
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#getSummary()}.
   */
  public final void testGetSummary () {
    assertEquals ("1999/0111-1 - Vredegerecht", judaItem1.getSummary ());
    assertEquals ("1999/0111-0 - AF - Bespreking (elders) :", judaItem2.getSummary ());
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#getUser()}.
   */
  public final void testGetUser () {
    assertEquals ("EVAJOOSTEN", judaItem1.getUser ());
    assertEquals ("ERICWOUTERS", judaItem2.getUser ());
  }

  /**
   * Test method for {@link be.CoCoCo.CalendarSync.JudaItem#equals(be.CoCoCo.CalendarSync.CalendarItem)}.
   */
  public final void testEqualsCalendarItem () {
    assertFalse(judaItem1.equals (judaItem2));
    judaItem1.equals (new ZarafaItem (judaItem1));
  }

}
