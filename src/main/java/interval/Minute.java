package interval;

import java.util.Calendar;
import java.util.Date;

public class Minute extends Interval {
    public static final int MINUTES = 1;

    public Date next() {
        calendar.add(Calendar.MINUTE, MINUTES);
        return get();
    }

    protected void adjust() {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
