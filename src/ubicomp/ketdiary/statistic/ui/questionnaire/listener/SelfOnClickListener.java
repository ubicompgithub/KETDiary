package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.SelfHelpContent;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.view.View;

public class SelfOnClickListener extends QuestionnaireOnClickListener {

	public SelfOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_SELFHELP);
		seq.add(6);
		contentSeq.add(new SelfHelpContent(msgBox));
		contentSeq.get(contentSeq.size()-1).onPush();
	}

}
