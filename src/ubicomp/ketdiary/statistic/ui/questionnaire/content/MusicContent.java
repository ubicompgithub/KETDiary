package ubicomp.ketdiary.statistic.ui.questionnaire.content;

import android.media.MediaPlayer;
import android.util.Log;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.statistic.ui.QuestionnaireDialog;
import ubicomp.ketdiary.statistic.ui.questionnaire.listener.MusicEndOnClickListener;

public class MusicContent extends QuestionnaireContent {

	private static String[] TEXT;
	private static final int AID_START_IDX = 10;
	private int aid;
	
	public MusicContent(QuestionnaireDialog msgBox, int aid) {
		super(msgBox);
		this.aid = aid;
		TEXT = msgBox.getContext().getResources().getStringArray(R.array.question_solutions);
	}

	@Override
	protected void setContent() {
		msgBox.showCloseButton(false);
		msgBox.setNextButton("", null);
		setHelp(R.string.follow_the_guide_music);
		msgBox.setNextButton(TEXT[aid-AID_START_IDX],new MusicEndOnClickListener(msgBox));
		msgBox.showQuestionnaireLayout(false);
		Log.d("CONTENT","MEDIAPLAYER_CONTENT");
		MediaPlayer mediaPlayer = msgBox.createMediaPlayer(R.raw.emotion_0);
		mediaPlayer.start();
	}
	
}
