package ubicomp.ketdiary.data.structure;

public class StorytellingTest {

	private TimeValue tv;
	private int questionPage;
	private boolean isCorrect;
	private String selection;
	private int agreement;
	private int score;

	public StorytellingTest(long ts, int questionPage, boolean isCorrect, String selection, int agreement, int score) {
		this.tv = TimeValue.generate(ts);
		this.questionPage = questionPage;
		this.isCorrect = isCorrect;
		this.selection = selection == null ? "" : selection;
		this.agreement = agreement;
		this.score = score;
	}

	public TimeValue getTv() {
		return tv;
	}

	public int getQuestionPage() {
		return questionPage;
	}

	public boolean isCorrect() {
		return isCorrect;
	}

	public String getSelection() {
		return selection;
	}

	public int getAgreement() {
		return agreement;
	}

	public int getScore() {
		return score;
	}

}
