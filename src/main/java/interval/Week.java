package interval;

import java.util.Calendar;
import java.util.Date;

public class Week extends Day {
    public Week() {}

    public Date next() {
        calendar.add(Calendar.WEEK_OF_MONTH, 1);
        return get();
    }

    protected void adjust() {
        super.adjust();
        calendar.set(Calendar.DAY_OF_WEEK, 2);
    }
}
