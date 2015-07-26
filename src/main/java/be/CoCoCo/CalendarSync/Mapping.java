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
class Mapping {

  private int id;
  private String UID1; 
  private String UID2;   
  
  public Mapping(String UID1, String UID2) {
      this.UID1 = UID1;
      this.UID2 = UID2;
  }
  
  protected Mapping () {
    
  }
  
  public int getid() {
     return id;
  }
  
  public void setid(int id){
    this.id=id;
  }
  
  public String getUID1() {
    return UID1;
  }

  public void setUID1(String uid) {
    this.UID1=uid;
  }
  
  public String getUID2() {
    return UID2;
  }

  public void setUID2(String uid) {
    UID2=uid;
  }
}
