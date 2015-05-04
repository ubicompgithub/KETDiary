package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import android.view.View;
import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;

public class TryAgainDoneOnClickListener extends QuestionnaireOnClickListener {

	public TryAgainDoneOnClickListener(QuestionnaireDialog msgBox) {
		super(msgBox);
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_TRYAGAIN);
		seq.add(8);
		msgBox.closeDialog(R.string.try_again_toast);
		PreferenceControl.setUpdateDetection(true);
		MainActivity.getMainActivity().changeTab(0);
	}

}
