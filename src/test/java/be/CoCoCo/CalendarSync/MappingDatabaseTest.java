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
public class MappingDatabaseTest extends TestCase {

  /**
   * @param name
   */
  public MappingDatabaseTest (String name) {
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

  public final void testMappingDatabase () {
    MappingDatabase mappingDB = new MappingDatabase ();
    assertNull (mappingDB.getMapping ("1"));
    assertNull (mappingDB.getMapping ("2"));
    mappingDB.addMapping ("1", "2");
    assertEquals ("2", mappingDB.getMapping ("1"));
    assertEquals ("1", mappingDB.getMapping ("2"));
    mappingDB.deleteMapping ("2", "1");
    assertNull (mappingDB.getMapping ("1"));
    assertNull (mappingDB.getMapping ("2"));
  }
}
