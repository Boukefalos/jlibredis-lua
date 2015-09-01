package interval;

import java.util.Calendar;
import java.util.Date;

public class Hour extends Minute {
    public static final int HOURS = 1;

    public Hour() {}

    public Date next() {
        calendar.add(Calendar.HOUR, HOURS);
        return get();
    }

    protected void adjust() {
        super.adjust();
        calendar.set(Calendar.MINUTE, 0);
    }
}
