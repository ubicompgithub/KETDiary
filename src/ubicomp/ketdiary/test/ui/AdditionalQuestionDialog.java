package ubicomp.ketdiary.test.ui;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.AdditionalQuestionnaire;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.EnablePage;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.main.ui.toast.CustomToastSmall;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Questionnaire Dialog for the additional questionnaire appearing at night
 * 
 * @author Stanley Wang
 */
public class AdditionalQuestionDialog {

	private Context context;
	private LayoutInflater inflater;
	private RelativeLayout boxLayout = null,mainLayout;

	private TextView emotionText, cravingText, emotionShowText, cravingShowText, title, help, send, notSend;
	private SeekBar emotionSeekBar, cravingSeekBar;

	private ImageView emotionShow, cravingShow;

	private TextView[] eNum, cNum;

	private static String[] emotionStr, cravingStr;

	private LinearLayout questionLayout;

	private EndOnClickListener endListener;
	private CancelOnClickListener cancelListener;

	private Typeface digitTypeface, wordTypeface, wordTypefaceBold;

	private int[] emotionResIds = { R.drawable.emotion_0, R.drawable.emotion_1, R.drawable.emotion_2,
			R.drawable.emotion_3, R.drawable.emotion_4 };
	private int[] cravingResIds = { R.drawable.craving_0, R.drawable.craving_1, R.drawable.craving_2,
			R.drawable.craving_3, R.drawable.craving_4, R.drawable.craving_5, R.drawable.craving_6,
			R.drawable.craving_7, R.drawable.craving_8, R.drawable.craving_9 };

	private int text_color = App.getContext().getResources().getColor(R.color.dark_gray);
	private int highlight_color = App.getContext().getResources().getColor(R.color.lite_orange);
	
	private EndOnTouchListener endOnTouchListener;

	private boolean done, doneByDoubleClick;

	private EnablePage enablePage;

	/**
	 * Constructor
	 * 
	 * @param mainLayout
	 *            Layout contains the AdditionalQuestionDialog
	 * @param enablePage
	 *            the class support Interface EnablePage
	 */
	public AdditionalQuestionDialog(RelativeLayout mainLayout, EnablePage enablePage) {
		context = App.getContext();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mainLayout = mainLayout;
		emotionStr = context.getResources().getStringArray(R.array.emotion_state);
		cravingStr = context.getResources().getStringArray(R.array.craving_state);
		digitTypeface = Typefaces.getDigitTypeface();
		wordTypeface = Typefaces.getWordTypeface();
		wordTypefaceBold = Typefaces.getWordTypefaceBold();
		this.enablePage = enablePage;
		setting();
		mainLayout.addView(boxLayout);

	}

	private void setting() {

		endListener = new EndOnClickListener();
		cancelListener = new CancelOnClickListener();
		boxLayout = (RelativeLayout) inflater.inflate(R.layout.dialog_additional_question, null);
		boxLayout.setVisibility(View.INVISIBLE);

		questionLayout = (LinearLayout) boxLayout.findViewById(R.id.msg_question_layout);

		title = (TextView) boxLayout.findViewById(R.id.msg_title);
		title.setTypeface(wordTypefaceBold);

		emotionText = (TextView) boxLayout.findViewById(R.id.msg_emotion_text);
		emotionText.setTypeface(wordTypefaceBold);

		cravingText = (TextView) boxLayout.findViewById(R.id.msg_craving_text);
		cravingText.setTypeface(wordTypefaceBold);

		emotionSeekBar = (SeekBar) boxLayout.findViewById(R.id.msg_emotion_seek_bar);
		cravingSeekBar = (SeekBar) boxLayout.findViewById(R.id.msg_craving_seek_bar);

		emotionShow = (ImageView) boxLayout.findViewById(R.id.msg_emotion_show);
		cravingShow = (ImageView) boxLayout.findViewById(R.id.msg_craving_show);

		emotionSeekBar.setOnSeekBarChangeListener(new EmotionListener());
		cravingSeekBar.setOnSeekBarChangeListener(new DesireListener());

		emotionShowText = (TextView) boxLayout.findViewById(R.id.msg_emotion_show_text);
		emotionShowText.setTypeface(wordTypeface);

		cravingShowText = (TextView) boxLayout.findViewById(R.id.msg_craving_show_text);
		cravingShowText.setTypeface(wordTypeface);

		help = (TextView) boxLayout.findViewById(R.id.msg_help);
		help.setTypeface(wordTypefaceBold);

		eNum = new TextView[5];
		eNum[0] = (TextView) boxLayout.findViewById(R.id.msg_emotion_num0);
		eNum[1] = (TextView) boxLayout.findViewById(R.id.msg_emotion_num1);
		eNum[2] = (TextView) boxLayout.findViewById(R.id.msg_emotion_num2);
		eNum[3] = (TextView) boxLayout.findViewById(R.id.msg_emotion_num3);
		eNum[4] = (TextView) boxLayout.findViewById(R.id.msg_emotion_num4);

		for (int i = 0; i < 5; ++i) {
			eNum[i].setTypeface(digitTypeface);
		}

		cNum = new TextView[10];
		cNum[0] = (TextView) boxLayout.findViewById(R.id.msg_craving_num0);
		cNum[1] = (TextView) boxLayout.findViewById(R.id.msg_craving_num1);
		cNum[2] = (TextView) boxLayout.findViewById(R.id.msg_craving_num2);
		cNum[3] = (TextView) boxLayout.findViewById(R.id.msg_craving_num3);
		cNum[4] = (TextView) boxLayout.findViewById(R.id.msg_craving_num4);
		cNum[5] = (TextView) boxLayout.findViewById(R.id.msg_craving_num5);
		cNum[6] = (TextView) boxLayout.findViewById(R.id.msg_craving_num6);
		cNum[7] = (TextView) boxLayout.findViewById(R.id.msg_craving_num7);
		cNum[8] = (TextView) boxLayout.findViewById(R.id.msg_craving_num8);
		cNum[9] = (TextView) boxLayout.findViewById(R.id.msg_craving_num9);
		for (int i = 0; i < 10; ++i) {
			cNum[i].setTypeface(digitTypeface);
		}

		endOnTouchListener = new EndOnTouchListener();
		send = (TextView) boxLayout.findViewById(R.id.msg_send);
		send.setOnTouchListener(endOnTouchListener);
		send.setTypeface(wordTypefaceBold);
		notSend = (TextView) boxLayout.findViewById(R.id.msg_not_send);
		notSend.setOnTouchListener(endOnTouchListener);
		notSend.setTypeface(wordTypefaceBold);
	}

	/** remove the dialog and release the resources */
	public void clear() {
		if (mainLayout != null && boxLayout != null && boxLayout.getParent() != null
				&& boxLayout.getParent().equals(mainLayout))
			mainLayout.removeView(boxLayout);
		enablePage.enablePage(true);
	}

	

	private void enableSend(boolean enable) {
		if (enable) {
			send.setTextColor(highlight_color);
			notSend.setTextColor(highlight_color);
		} else {
			send.setTextColor(text_color);
			notSend.setTextColor(text_color);
		}
		done = enable;
		doneByDoubleClick = false;
	}

	private void enableSend(boolean enable, boolean click) {
		if (enable) {
			send.setTextColor(highlight_color);
			notSend.setTextColor(highlight_color);
		} else {
			send.setTextColor(text_color);
			notSend.setTextColor(text_color);
		}
		done = enable;
		doneByDoubleClick = click;
	}

	public void show() {
		RelativeLayout.LayoutParams boxParam = (LayoutParams) boxLayout.getLayoutParams();
		boxParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

		emotionSeekBar.setProgress(1);
		emotionSeekBar.setProgress(0);
		cravingSeekBar.setProgress(1);
		cravingSeekBar.setProgress(0);
		questionLayout.setVisibility(View.VISIBLE);
		boxLayout.setVisibility(View.VISIBLE);
		send.setOnClickListener(endListener);
		notSend.setOnClickListener(cancelListener);
		enablePage.enablePage(false);
		enableSend(false);
	}

	private class EndOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (!done) {
				CustomToastSmall.generateToast(R.string.msg_box_toast_send);
				enableSend(true, true);
				return;
			}

			if (doneByDoubleClick)
				ClickLog.Log(ClickLogId.TEST_ADDITIONAL_QUESTION_SEND_EMPTY);
			else
				ClickLog.Log(ClickLogId.TEST_ADDITIONAL_QUESTION_SEND);

			boxLayout.setVisibility(View.INVISIBLE);
			int craving = cravingSeekBar.getProgress();
			int emotion = emotionSeekBar.getProgress();
			DatabaseControl db = new DatabaseControl();
			int addScore = db.insertAdditionalQuestionnaire(new AdditionalQuestionnaire(System.currentTimeMillis(),
					true, emotion, craving, 0));
			if (PreferenceControl.checkCouponChange())
				PreferenceControl.setCouponChange(true);
			CustomToast.generateToast(R.string.additional_questionnaire_toast, addScore);
			clear();
		}
	}

	private class CancelOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {

			if (!done) {
				CustomToastSmall.generateToast(R.string.msg_box_toast_cancel);
				enableSend(true);
				return;
			}
			ClickLog.Log(ClickLogId.TEST_ADDITIONAL_QUESTION_CANCEL);
			boxLayout.setVisibility(View.INVISIBLE);
			int craving = -1;
			int emotion = -1;
			DatabaseControl db = new DatabaseControl();
			db.insertAdditionalQuestionnaire(new AdditionalQuestionnaire(System.currentTimeMillis(), false, emotion,
					craving, 0));
			clear();
		}
	}

	private class EmotionListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (emotionShow == null || emotionResIds == null)
				return;
			emotionShow.setImageResource(emotionResIds[progress]);
			emotionShowText.setText(emotionStr[progress]);
			for (int i = 0; i < eNum.length; ++i)
				eNum[i].setVisibility(View.INVISIBLE);
			eNum[progress].setVisibility(View.VISIBLE);
			enableSend(true);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private class DesireListener implements SeekBar.OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (cravingShow == null || cravingResIds == null)
				return;
			cravingShow.setImageResource(cravingResIds[progress]);
			cravingShowText.setText(cravingStr[progress]);
			for (int i = 0; i < cNum.length; ++i)
				cNum[i].setVisibility(View.INVISIBLE);
			cNum[progress].setVisibility(View.VISIBLE);
			enableSend(true);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private class EndOnTouchListener implements View.OnTouchListener {

		private Rect rect;
		private final int normalSize = App.getContext().getResources().getDimensionPixelSize(R.dimen.normal_title_size);
		private int largeSize = App.getContext().getResources().getDimensionPixelSize(R.dimen.large_title_size);

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int e = event.getAction();
			TextView tv = (TextView) v;
			switch (e) {
			case MotionEvent.ACTION_MOVE:
				if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()))
					tv.setTextSize(normalSize);
				break;
			case MotionEvent.ACTION_UP:
				tv.setTextSize(normalSize);
				break;
			case MotionEvent.ACTION_DOWN:
				tv.setTextSize(largeSize);
				rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
				break;
			}
			return false;
		}
	}
}
