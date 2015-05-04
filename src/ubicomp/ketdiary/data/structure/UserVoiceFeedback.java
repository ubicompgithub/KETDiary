package ubicomp.ketdiary.data.structure;

public class UserVoiceFeedback {
	private TimeValue tv;
	private TimeValue detectionTv;
	private boolean testSuccess = false;
	private boolean hasData = false;

	public UserVoiceFeedback(long ts, long detectionTs, boolean testSuccess, boolean hasData) {
		tv = TimeValue.generate(ts);
		detectionTv = TimeValue.generate(detectionTs);
		this.testSuccess = testSuccess;
		this.hasData = hasData;
	}

	public TimeValue getTv() {
		return tv;
	}

	public TimeValue getDetectionTv() {
		return detectionTv;
	}

	public boolean isTestSuccess() {
		return testSuccess;
	}

	public boolean hasData() {
		return hasData;
	}

}
