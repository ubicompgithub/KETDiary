package ubicomp.ketdiary.data.structure;

import java.util.Calendar;

public class UserVoiceRecord {

	private TimeValue tv;
	private TimeValue recordTv;
	private int score;

	public UserVoiceRecord(long ts, int rYear, int rMonth, int rDay, int score) {
		this.tv = TimeValue.generate(ts);
		Calendar cal = Calendar.getInstance();
		cal.set(rYear, rMonth, rDay, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		this.recordTv = TimeValue.generate(cal.getTimeInMillis());
		this.score = score;
	}

	public String toFileString() {
		return recordTv.toFileString();
	}

	public TimeValue getTv() {
		return tv;
	}

	public TimeValue getRecordTv() {
		return recordTv;
	}

	public int getScore() {
		return score;
	}

}
