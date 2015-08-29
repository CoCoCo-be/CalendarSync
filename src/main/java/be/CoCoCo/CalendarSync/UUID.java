/**
 *
 * CalendarSync
 * Copyright @2015 CoCoCo.be
 *
 */
package be.CoCoCo.CalendarSync;


/**
 * @author Kris Cox
 *
 */
class UUID {

  //values
  private int id;
  
  protected UUID() {
    
  }
  
  public UUID(int id) {
    this.id = id;
  }
  
  /**
   * @return the id
   */
  public int getId () {
    return id;
  }

  
  /**
   * @param id the id to set
   */
  public void setId (int id) {
    this.id = id;
  }

}
