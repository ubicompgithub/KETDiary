package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import android.view.View;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;

public class EndOnClickListener extends QuestionnaireOnClickListener {

	public EndOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_END);
		msgBox.closeDialog();
	}

}
