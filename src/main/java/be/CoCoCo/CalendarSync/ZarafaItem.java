/**
 * 
 * be.CoCoCo.CalendarSync- Copyright @2014 CoCoCo.be
 * 
 */
package be.CoCoCo.CalendarSync;

import java.util.Calendar;

import org.apache.log4j.Logger;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;

/**
 * @author Kris Cox
 * 
 */
public class ZarafaItem implements CalendarItem {

  static Logger logger = Logger.getLogger (ZarafaItem.class);

  private Component component = null;

  /**
   * Constructor
   * 
   * @param component
   */
  public ZarafaItem (Component component) {
    this.component = component;
    if (this.component instanceof VEvent) {
      java.util.Calendar now = java.util.Calendar.getInstance ();

      VEvent event = (VEvent) this.component;

      if (null == event.getStartDate ()) {
        logger.info ("Event without a start date");
      }

      if (null == event.getEndDate ()) {
        logger.info ("Event without an end date");
      }

      if (null == event.getSummary ()) {
        logger.info ("Event without a summary");
      }
      
      if (null == event.getDescription ()) {
        logger.info ("Event without a Description");
      }

      if (null == event.getUid ()) {
        String year = String.valueOf (now.get (java.util.Calendar.YEAR));
        String uidString = year + "CoCoCo.be-" + java.util.UUID.randomUUID ();
        Uid uid = new Uid (uidString);
        event.getProperties ().add (uid);
      }

      if (null == event.getLastModified ()) {
        event.getProperties ().add (new LastModified (new DateTime (now.getTime ())));
      }
      
      if (null == event.getTransparency ()) {
        logger.info ("Event without Transparency setting");
      }
    }
  }

  /**
   * 
   * Constructor from other CalendarItem 
   * 
   * @param item
   */
  public ZarafaItem (CalendarItem item) {
    this(item, item.getID ());
  }

  /**
   * 
   * Constructor from other CalendarItem 
   * 
   * @param CalendarItem, String
   */
  public ZarafaItem (CalendarItem item, String uid) {
    
    if (item.isTransparent ()) {
      java.util.Calendar startDate = java.util.Calendar.getInstance();
      startDate.clear ();
      startDate.set (Calendar.YEAR, item.getStartDate ().get(Calendar.YEAR));
      startDate.set (Calendar.MONTH, item.getStartDate ().get(Calendar.MONTH));
      startDate.set (Calendar.DAY_OF_MONTH, item.getStartDate ().get (Calendar.DAY_OF_MONTH));
      ParameterList params = new ParameterList();
      params.add(Value.DATE);
      Date startTime = new Date(startDate.getTime ());
      DtStart start = new DtStart(params, startTime);
      Summary summary = new Summary (item.getSummary());
      PropertyList props = new PropertyList ();
      props.add (new DtStamp ());
      props.add (start);
      props.add (summary);
      component = new VEvent(props);
    } else {
      DateTime startDate = new DateTime(item.getStartDate().getTime ());
      DateTime endDate = new DateTime(item.getEndDate ().getTime ());
      component = new VEvent(startDate, endDate, item.getSummary ());  
    }
    
    PropertyList property = component.getProperties ();
    property.add ( new Uid(uid));
    property.add ( new Description (item.getDescription ()));
    property.add ( new Transp (item.isTransparent ()? "TRANSPARENT": "OPAQUE"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#GetID()
   */
  public String getID () {
    if (component instanceof VEvent) {
      Uid uid = ((VEvent) component).getUid ();
      if (null != uid) return uid.toString ().replaceFirst ("UID:", "")
          .replaceAll ("[\r\n]*", "");
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#isNewer(be.CoCoCo.
   * CalendarSync.CalendarItem)
   */
  public boolean isNewer (CalendarItem calendarItem) {
    Calendar lastModified1 = calendarItem.lastModified ();
    if (null != lastModified1) return calendarItem.lastModified ().before (
        lastModified ());
    return false;
  }

  /**
   * Return the {@link Component}
   * 
   * @return {@link Component}
   */
  public Component getComponent () {
    return component;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#lastModified()
   */
  public Calendar lastModified () {
    if (component instanceof VEvent) {
      LastModified lastModified;
      if (null != (lastModified = ((VEvent) component).getLastModified ())) {
        Calendar result = Calendar.getInstance ();
        result.setTime (lastModified.getDate());
        return result;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getStartDate()
   */
  public Calendar getStartDate () {
    if (component instanceof VEvent) {
      DtStart startDate = ((VEvent) component).getStartDate ();
      if (null != startDate) {
        Calendar result = Calendar.getInstance ();
        result.setTime (startDate.getDate());
        return result;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getEndDate()
   */
  public Calendar getEndDate () {
    if (component instanceof VEvent) {
      DtEnd endDate = ((VEvent) component).getEndDate ();
      if (null != endDate) {
        Calendar result = Calendar.getInstance ();
        result.setTime (endDate.getDate ());
        return result;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see be.CoCoCo.CalendarSync.CalendarItem#getSummary()
   */
  public String getSummary () {
    if (component instanceof VEvent) return ((VEvent) component).getSummary ()
        .toString ().replaceFirst ("SUMMARY:", "").replaceAll ("[\r\n]*", "");
    return null;
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.CalendarItem#equals(be.CoCoCo.CalendarSync.CalendarItem)
   */
  public boolean equals (CalendarItem calendarItem) {
    logger.trace ("Entering Equals");
    if (!(calendarItem instanceof ZarafaItem)) {
      logger.debug ("Wrong type");
      return false;
    }
    ZarafaItem item = (ZarafaItem) calendarItem;
    if (! item.getComponent ().equals (component)) { logger.debug ("Different component"); return false; }
    logger.trace ("Exiting equals with true");
    return true;
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.CalendarItem#getDescription()
   */
  public String getDescription () {
    logger.trace("Entering getDescription");
    if (component instanceof VEvent) 
      if (null != ((VEvent) component).getDescription ())
        return ((VEvent) component).getDescription ().getValue ();
    return null;
  }

  /* (non-Javadoc)
   * @see be.CoCoCo.CalendarSync.CalendarItem#allDayEvent()
   */
  public boolean isTransparent () {
    if (component instanceof VEvent) return ((VEvent) component).getTransparency ().getValue ()
        .equalsIgnoreCase ("TRANSPARENT");
    return false;
  }

}
