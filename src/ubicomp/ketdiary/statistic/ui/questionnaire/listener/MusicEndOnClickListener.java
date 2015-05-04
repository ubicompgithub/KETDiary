package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import android.util.Log;
import android.view.View;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;

public class MusicEndOnClickListener extends QuestionnaireOnClickListener {

	
	public MusicEndOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_END);
		Log.d("CONTENT","MUSIC END");
		msgBox.closeDialog();
	}

}
