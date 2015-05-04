package ubicomp.ketdiary.data.structure;

public class FacebookInfo {

	private TimeValue tv;
	private int pageWeek;
	private int pageLevel;
	private String text;
	private boolean addedScore;
	private boolean uploadSuccess;
	private int privacy;
	private int score;

	public static final int FRIEND = 0;
	public static final int SELF = 1;

	public FacebookInfo(long ts, int pageWeek, int pageLevel, String text, boolean addedScore, boolean uploadSuccess,
			int privacy, int score) {
		this.tv = TimeValue.generate(ts);
		this.pageWeek = pageWeek;
		this.pageLevel = pageLevel;
		this.text = text == null ? "" : text;
		this.addedScore = addedScore;
		this.uploadSuccess = uploadSuccess;
		this.privacy = privacy;
		this.score = score;

	}

	public TimeValue getTv() {
		return tv;
	}

	public int getPageWeek() {
		return pageWeek;
	}

	public int getPageLevel() {
		return pageLevel;
	}

	public String getText() {
		return text;
	}

	public boolean isAddedScore() {
		return addedScore;
	}

	public boolean isUploadSuccess() {
		return uploadSuccess;
	}

	public int getPrivacy() {
		return privacy;
	}

	public int getScore() {
		return score;
	}
}
