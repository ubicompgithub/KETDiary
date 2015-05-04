package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.ConnectContent;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.view.View;

public class FamilyCallOnClickListener extends QuestionnaireOnClickListener {

	public FamilyCallOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View arg0) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_FAMILY);
		seq.add(4);
		contentSeq.add(new ConnectContent(msgBox,ConnectContent.TYPE_FAMILY));
		contentSeq.get(contentSeq.size()-1).onPush();
	}

}
