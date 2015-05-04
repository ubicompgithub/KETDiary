package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.CallCheckContent;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.view.View;

public class HotLineOnClickListener extends QuestionnaireOnClickListener {

	public HotLineOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_HOTLINE);
		seq.add(5);
		String emotion_hot_line = msgBox.getContext().getString(R.string.call_check_help_emotion_hot_line);
		contentSeq.add(new CallCheckContent(msgBox,emotion_hot_line,"0800788995",true));
		contentSeq.get(contentSeq.size()-1).onPush();
	}

}
