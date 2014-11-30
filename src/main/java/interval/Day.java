package interval;

import java.util.Calendar;
import java.util.Date;

public class Day extends Minute {

	public Day() {}

	public Date next() {
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return get();
	}

	protected void adjust() {
		super.adjust();
	    calendar.set(Calendar.HOUR_OF_DAY, 0);
	}
}
