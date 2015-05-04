package ubicomp.ketdiary.data.structure;

import java.util.Calendar;

public class EmotionManagement {

	private TimeValue tv;
	private TimeValue recordTv;
	private int emotion;
	private int type;
	private String reason;
	private int score;

	public EmotionManagement(long ts, int rYear, int rMonth, int rDay, int emotion, int type, String reason, int score) {
		this.tv = TimeValue.generate(ts);
		Calendar cal = Calendar.getInstance();
		cal.set(rYear, rMonth, rDay, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		this.recordTv = TimeValue.generate(cal.getTimeInMillis());
		this.emotion = emotion;
		this.type = type;
		this.reason = reason == null ? "" : reason;
		this.score = score;
	}

	public EmotionManagement(long ts, int rYear, int rMonth, int rDay, int emotion, int type, String reason, int score,
			boolean ver1) {
		this.tv = TimeValue.generate(ts);
		Calendar cal = Calendar.getInstance();
		cal.set(rYear, rMonth, rDay, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		this.recordTv = TimeValueVer1.generate(cal.getTimeInMillis());
		this.emotion = emotion;
		this.type = type;
		this.reason = reason == null ? "" : reason;
		this.score = score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(tv.toString());
		sb.append(' ');
		sb.append(recordTv.toString());
		sb.append(' ');
		sb.append(emotion);
		sb.append(' ');
		sb.append(type);
		sb.append(' ');
		sb.append(reason);
		sb.append(' ');
		sb.append(score);
		return sb.toString();
	}

	public TimeValue getTv() {
		return tv;
	}

	public TimeValue getRecordTv() {
		return recordTv;
	}

	public int getEmotion() {
		return emotion;
	}

	public int getType() {
		return type;
	}

	public String getReason() {
		return reason;
	}

	public int getScore() {
		return score;
	}

}
