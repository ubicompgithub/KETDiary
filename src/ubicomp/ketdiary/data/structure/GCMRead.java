package ubicomp.ketdiary.data.structure;

public class GCMRead {
	private TimeValue tv;
	private TimeValue readTv;
	private String message;
	private boolean read;

	public GCMRead(long ts, long readTs, String message, boolean read) {
		this.tv = TimeValue.generate(ts);
		this.readTv = TimeValue.generate(readTs);
		this.message = message == null ? "" : message;
		this.read = read;
	}

	public TimeValue getTv() {
		return tv;
	}

	public TimeValue getReadTv() {
		return readTv;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return read;
	}

}
