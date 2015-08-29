/**
 *
 * CalendarSync
 * Copyright @2015 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;

import junit.framework.TestCase;


/**
 * @author Kris Cox
 *
 */
public class UUIDGetterTest extends TestCase {

  private UUIDGetter getter = new UUIDGetter();
  
  /**
   * @param name
   */
  public UUIDGetterTest (String name) {
    super (name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () throws Exception {
    super.setUp ();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () throws Exception {
    super.tearDown ();
  }
  
  /*
   * Test method for {@link be.CoCoCo.CalendarSync.UUIDGetter#getNew()}.
   */
  public final void testGetNew () {
    getter.reset ();
    assertEquals (1, getter.getNew ());
  }
  
}
