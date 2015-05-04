package ubicomp.ketdiary.statistic.ui.questionnaire.content;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.EndOnClickListener;

public class SolutionContent extends QuestionnaireContent {

	private static String[] TEXT;
	private static final int AID_START_IDX = 10;
	private int aid;

	public SolutionContent(QuestionnaireDialog msgBox, int aid) {
		super(msgBox);
		this.aid = aid;
		TEXT = msgBox.getContext().getResources().getStringArray(R.array.question_solutions);
	}

	@Override
	protected void setContent() {
		msgBox.showCloseButton(false);
		msgBox.setNextButton("", null);
		setHelp(R.string.follow_the_guide);
		msgBox.setNextButton(TEXT[aid - AID_START_IDX], new EndOnClickListener(msgBox));
		msgBox.showQuestionnaireLayout(false);
	}

}
