package ubicomp.ketdiary.data.structure;

/** Structure of AdditionalQuesionnaire appeared at night */
public class AdditionalQuestionnaire {

	/** @see TimeValue */
	private TimeValue tv;
	/** if the user can get a credit after finishing the questionnaire */
	private boolean addedScore;
	/** Emotion (0~4) */
	private int emotion;
	/** Craving index (0~9) */
	private int craving;
	/** Total credits got by the user */
	private int score;

	public AdditionalQuestionnaire(long timestamp, boolean addedScore, int emotion, int craving, int score) {
		this.tv = TimeValue.generate(timestamp);
		this.addedScore = addedScore;
		this.emotion = emotion;
		this.craving = craving;
		this.score = score;
	}

	/**
	 * Get TimeValue of the AdditionalQuestionnaire
	 * 
	 * @return TimeValue
	 * @see TimeValue
	 */
	public TimeValue getTv() {
		return tv;
	}

	/**
	 * If the user can get a credit after finishing the questionnaire
	 * 
	 * @return true if the user can get credits
	 */
	public boolean isAddedScore() {
		return addedScore;
	}

	/**
	 * Get the emotion of the user
	 * 
	 * @return emotion (0~4)
	 */
	public int getEmotion() {
		return emotion;
	}

	/**
	 * Get the craving index of the user
	 * 
	 * @return craving index (0~9)
	 */
	public int getCraving() {
		return craving;
	}

	/**
	 * Get total credits got by the user
	 * 
	 * @return total credits
	 */
	public int getScore() {
		return score;
	}
}
