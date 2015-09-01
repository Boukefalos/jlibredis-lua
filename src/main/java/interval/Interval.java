package interval;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

public abstract class Interval implements Iterator<Date> {
    public static final String TIMEZONE = "Europe/Amsterdam";

    protected Date endDate;
    protected TimeZone timeZone;
    protected Calendar calendar;

    public Interval() {}

    public Interval(Date startDate, Date endDate, String timeZoneID) {
        setTimeZone(timeZoneID);
        setDate(startDate, endDate);
    }

    public void setTimeZone(String timeZoneID) {
        timeZone = TimeZone.getTimeZone(timeZoneID);
        calendar = new GregorianCalendar(TimeZone.getTimeZone(TIMEZONE));        
    }

    public Interval(Date startDate, Date endDate) {
        this(startDate, endDate, TIMEZONE);
    }
    
    public void setDate(Date startDate, Date endDate) {
        if (timeZone == null) {
            setTimeZone(TIMEZONE);
        }
        calendar.setTime(startDate);
        adjust();
        this.endDate = endDate;
    }

    public void remove() {}

    public Date get() {
        return calendar.getTime();
    }

    public abstract Date next();

    public boolean hasNext() {
        return next().compareTo(endDate) < 0;
    }

    protected abstract void adjust();
}
