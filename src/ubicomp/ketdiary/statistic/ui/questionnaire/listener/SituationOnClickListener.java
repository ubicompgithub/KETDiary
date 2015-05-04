package ubicomp.ketdiary.statistic.ui.questionnaire.listener;

import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.SolutionContent;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.view.View;

public class SituationOnClickListener extends QuestionnaireOnClickListener {

	private int aid;
	public SituationOnClickListener(QuestionnaireDialog msgBox,int aid) {
		super(msgBox);
		this.aid = aid;
	}

	@Override
	public void onClick(View v) {
		ClickLog.Log(ClickLogId.STATISTIC_QUESTION_SITUATION);
		seq.add(aid);
		contentSeq.add(new SolutionContent(msgBox,aid));
		contentSeq.get(contentSeq.size()-1).onPush();

	}

}
