package ubicomp.ketdiary.statistic.ui.questionnaire.content;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.EndOnClickListener;

public class BreathContent extends QuestionnaireContent {

	public BreathContent(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	protected void setContent() {
		msgBox.showCloseButton(false);
		msgBox.setNextButton("", null);
		setHelp(R.string.breath_check_help);
		msgBox.showQuestionnaireLayout(false);
		msgBox.setNextButton(R.string.ok,new EndOnClickListener(msgBox));
	}

}
