package ubicomp.ketdiary.system.check;

import java.util.Calendar;

import ubicomp.ketdiary.system.config.PreferenceControl;

import android.app.AlarmManager;

public class WeekNumCheck {

	private static final long WEEK = AlarmManager.INTERVAL_DAY * 7L;

	public static int getWeek(long ts) {
		Calendar c = PreferenceControl.getStartDate();
		long time = ts - c.getTimeInMillis();
		if (time < 0)
			return 0;
		else
			return (int) (time / WEEK);
	}

}
