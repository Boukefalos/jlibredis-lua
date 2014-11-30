package interval;

import java.util.Date;
import java.util.Calendar;

public class Month extends Day {
	public Month() {}

	public Date next() {
		calendar.add(Calendar.MONTH, 1);
		return get();
	}

	protected void adjust() {
		super.adjust();
	    calendar.set(Calendar.DAY_OF_MONTH, 1);	
	}
}
