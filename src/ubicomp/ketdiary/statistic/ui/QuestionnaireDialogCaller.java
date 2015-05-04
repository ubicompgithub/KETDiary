package ubicomp.ketdiary.statistic.ui;

import android.content.Context;
import ubicomp.ketdiary.main.ui.EnablePage;

public interface QuestionnaireDialogCaller extends EnablePage {
	public void setQuestionAnimation();
	public void updateSelfHelpCounter();
	public Context getContext();
}
