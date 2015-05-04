package ubicomp.ketdiary.storytelling.ui;

import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecordBox implements RecordBlockCaller, RecorderCallee {

	private RecorderCaller recorderCaller;
	private LinearLayout contentLayout;
	private RelativeLayout mainLayout;

	private VoiceRecordBlock voiceRecordBlock;
	private EmotionManageRecordBlock emRecordBlock;
	private TextView title;

	public RecordBox(RecorderCaller recorderCaller, Context context) {
		this.recorderCaller = recorderCaller;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mainLayout = (RelativeLayout) inflater.inflate(R.layout.storytelling_record_box, null);
		this.title = (TextView) mainLayout.findViewById(R.id.record_title);
		title.setTypeface(Typefaces.getWordTypefaceBold());
		this.contentLayout = (LinearLayout) mainLayout.findViewById(R.id.record_content_layout);
		this.voiceRecordBlock = new VoiceRecordBlock(this);
		this.emRecordBlock = new EmotionManageRecordBlock(this, context);
	}

	@Override
	public void updateHasRecorder(int idx) {
		recorderCaller.updateHasRecorder(idx);
	}

	@Override
	public void cleanRecordBox() {
		voiceRecordBlock.cleanRecordBox();
		emRecordBlock.cleanRecordBox();
	}

	@Override
	public View getRecordBox(TimeValue tv, int selected_button) {
		title.setText(App.getContext().getString(R.string.record_title) + "  " + tv.toSimpleDateString());
		contentLayout.removeAllViews();
		contentLayout.addView(voiceRecordBlock.getRecordBox(tv, selected_button));
		contentLayout.addView(emRecordBlock.getRecordBox(tv, selected_button));
		return mainLayout;
	}

	@Override
	public void enablePage(boolean enable, int CalleeId) {
		if (CalleeId == 0) {// voice
			recorderCaller.enablePage(enable);
			emRecordBlock.enableRecordBox(enable);
		}
	}

	@Override
	public void enableRecordBox(boolean enable) {
		voiceRecordBlock.enableRecordBox(enable);
		emRecordBlock.enableRecordBox(enable);
	}

}
