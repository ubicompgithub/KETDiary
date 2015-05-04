package ubicomp.ketdiary.data.structure;

public class StorytellingRead {

	private TimeValue tv;
	private boolean addedScore;
	private int page;
	private int score;

	public StorytellingRead(long ts, boolean addedScore, int page, int score) {
		this.tv = TimeValue.generate(ts);
		this.addedScore = addedScore;
		this.page = page;
		this.score = score;

	}

	public TimeValue getTv() {
		return tv;
	}

	public boolean isAddedScore() {
		return addedScore;
	}

	public int getPage() {
		return page;
	}

	public int getScore() {
		return score;
	}

}
