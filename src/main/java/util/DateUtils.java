package util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtils {
    public static final String TIMEZONE = "Europe/Amsterdam";
    protected static TimeZone timeZone;
    protected static Calendar calendar;

    public static void setTimeZone(String timeZone) {
        DateUtils.timeZone = TimeZone.getTimeZone(timeZone);
        calendar = new GregorianCalendar(DateUtils.timeZone);
    }

    public static long makeTimestamp(int year, int month, int day, int hour, int minute,  int second, int millisecond) {
        checkCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTimeInMillis();
    }

    public static TimeZone getTimeZone() {
        checkCalendar();
        return timeZone;
    }

    public static Calendar getCalendar() {
        checkCalendar();
        return calendar;
    }

    protected static void checkCalendar() {
        if (calendar == null ) {
            setTimeZone(TIMEZONE);
        }
    }
}
