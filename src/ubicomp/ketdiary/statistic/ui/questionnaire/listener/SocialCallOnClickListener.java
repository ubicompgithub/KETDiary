package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.ConnectContent;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import android.view.View;

public class SocialCallOnClickListener extends QuestionnaireOnClickListener {

	public SocialCallOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_SOCIAL);
		contentSeq.add(new ConnectContent(msgBox,ConnectContent.TYPE_SOCIAL));
		contentSeq.get(contentSeq.size()-1).onPush();
	}

}
