package ubicomp.ketdiary.data.structure;

import java.util.Calendar;

import ubicomp.ketdiary.system.check.WeekNumCheck;

public class TimeValueVer1 extends TimeValue {

	public static TimeValue generate(long timestamp){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int week = WeekNumCheck.getWeek(timestamp);
		int timeslot;
		if (hour < 12)
			timeslot = TimeValue.TIME_MORNING;
		else if (hour < 18)
			timeslot = TimeValue.TIME_NOON;
		else
			timeslot = TimeValue.TIME_NIGHT;
		
		return new TimeValue(year,month,day,hour,timeslot,timestamp,week);
	}
	
	protected TimeValueVer1(int year, int month, int day, int hour, int timeslot, long timestamp, int week) {
		super(year, month, day, hour, timeslot, timestamp, week);
	}

}
