package ubicomp.ketdiary.statistic.ui.questionnaire.content;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.MusicOnClickListener;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.SelectedListener;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.SituationOnClickListener;

public class SelfHelpContent extends QuestionnaireContent {

	public SelfHelpContent(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	protected void setContent() {
		msgBox.setNextButton("", null);
		setHelp(R.string.self_help_help);
		setSelectItem(R.string.self_help_selection0, new SelectedListener(msgBox, new SituationOnClickListener(msgBox,
				10), R.string.next));
		setSelectItem(R.string.self_help_selection1, new SelectedListener(msgBox, new SituationOnClickListener(msgBox,
				11), R.string.next));
		setSelectItem(R.string.self_help_selection2, new SelectedListener(msgBox, new SituationOnClickListener(msgBox,
				12), R.string.next));
		setSelectItem(R.string.self_help_selection3, new SelectedListener(msgBox, new SituationOnClickListener(msgBox,
				13), R.string.next));
		setSelectItem(R.string.self_help_selection4, new SelectedListener(msgBox, new SituationOnClickListener(msgBox,
				14), R.string.next));
		setSelectItem(R.string.self_help_selection5, new SelectedListener(msgBox, new MusicOnClickListener(msgBox, 15),
				R.string.next));
		msgBox.showQuestionnaireLayout(true);

	}

}
