package ubicomp.ketdiary.test.ui;

import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToastSmall;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.config.PreferenceControl;
import ubicomp.ketdiary.test.gps.GPSInterface;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class TestQuestionDialog {

	private TestQuestionCaller testQuestionCaller;
	private GPSInterface gpsInterface;
	private Context context;
	private LayoutInflater inflater;
	private RelativeLayout boxLayout = null;

	private TextView help, emotionText, cravingText, gpsText;
	private SeekBar emotionSeekBar, cravingSeekBar;
	private RadioGroup gpsRadioGroup;

	private RelativeLayout mainLayout;

	private ImageView emotionShow;
	private ImageView cravingShow;
	private TextView emotionShowText;
	private TextView cravingShowText;
	private RadioButton gpsNo, gpsYes;

	private TextView[] eNum, cNum;

	private static String[] emotionStr;
	private static String[] cravingStr;

	private LinearLayout questionLayout;

	private EndOnClickListener endListener;
	private CancelOnClickListener cancelListener;

	private Typeface digitTypeface;
	private Typeface wordTypeface;
	private Typeface wordTypefaceBold;

	private int[] emotionResIds = { R.drawable.emotion_0, R.drawable.emotion_1, R.drawable.emotion_2,
			R.drawable.emotion_3, R.drawable.emotion_4 };
	private int[] cravingResIds = { R.drawable.craving_0, R.drawable.craving_1, R.drawable.craving_2,
			R.drawable.craving_3, R.drawable.craving_4, R.drawable.craving_5, R.drawable.craving_6,
			R.drawable.craving_7, R.drawable.craving_8, R.drawable.craving_9 };

	private TextView title;
	private TextView send, notSend;

	private EndOnTouchListener endOnTouchListener;

	private boolean done, doneByDoubleClick;

	private int text_color = App.getContext().getResources().getColor(R.color.dark_gray);
	private int highlight_color = App.getContext().getResources().getColor(R.color.lite_orange);

	/**
	 * Constructor
	 * 
	 * @param gps
	 *            Class support the gps control-related functions
	 * @param testQuesionCaller
	 *            caller of the dialog
	 * @param mainLayout
	 *            layout contains the dialog
	 */
	public TestQuestionDialog(GPSInterface gps, TestQuestionCaller testQuestionCaller, RelativeLayout mainLayout) {
		this.testQuestionCaller = testQuestionCaller;
		this.gpsInterface = gps;
		this.context = App.getContext();
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mainLayout = mainLayout;
		emotionStr = context.getResources().getStringArray(R.array.emotion_state);
		cravingStr = context.getResources().getStringArray(R.array.craving_state);
		digitTypeface = Typefaces.getDigitTypeface();
		wordTypeface = Typefaces.getWordTypeface();
		wordTypefaceBold = Typefaces.getWordTypefaceBold();
		setting();
		mainLayout.addView(boxLayout);
	}

	protected void setting() {

		endListener = new EndOnClickListener();
		cancelListener = new CancelOnClickListener();
		boxLayout = (RelativeLayout) inflater.inflate(R.layout.dialog_test_question, null);
		boxLayout.setVisibility(View.INVISIBLE);

		questionLayout = (LinearLayout) boxLayout.findViewById(R.id.msg_question_layout);

		title = (TextView) boxLayout.findViewById(R.id.msg_title);
		title.setTypeface(wordTypefaceBold);

		help = (TextView) boxLayout.findViewById(R.id.msg_help);
		help.setTypeface(wordTypefaceBold);

		emotionText = (TextView) boxLayout.findViewById(R.id.msg_emotion_text);
		emotionText.setTypeface(wordTypefaceBold);

		cravingText = (TextView) boxLayout.findViewById(R.id.msg_craving_text);
		cravingText.setTypeface(wordTypefaceBold);

		gpsText = (TextView) boxLayout.findViewById(R.id.msg_gps_text);
		gpsText.setTypeface(wordTypefaceBold);

		gpsRadioGroup = (RadioGroup) boxLayout.findViewById(R.id.msg_gps_radiogroup);

		gpsNo = (RadioButton) boxLayout.findViewById(R.id.msg_gps_no);
		gpsNo.setTypeface(wordTypefaceBold);

		gpsYes = (RadioButton) boxLayout.findViewById(R.id.msg_gps_yes);
		gpsYes.setTypeface(wordTypefaceBold);

		emotionSeekBar = (SeekBar) boxLayout.findViewById(R.id.msg_emotion_seek_bar);
		cravingSeekBar = (SeekBar) boxLayout.findViewById(R.id.msg_craving_seek_bar);

		emotionSeekBar.setOnSeekBarChangeListener(new EmotionListener());
		cravingSeekBar.setOnSeekBarChangeListener(new CravingListener());

		emotionShow = (ImageView) boxLayout.findViewById(R.id.msg_emotion_show);
		cravingShow = (ImageView) boxLayout.findViewById(R.id.msg_craving_show);

		emotionShowText = (TextView) boxLayout.findViewById(R.id.msg_emotion_show_text);
		emotionShowText.setTypeface(wordTypeface);

		cravingShowText = (TextView) boxLayout.findViewById(R.id.msg_craving_show_text);
		cravingShowText.setTypeface(wordTypeface);

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

	/** Initialize the dialog */
	public void initialize() {

		RelativeLayout.LayoutParams boxParam = (LayoutParams) boxLayout.getLayoutParams();
		boxParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

		emotionSeekBar.setProgress(2);
		emotionSeekBar.setProgress(0);
		cravingSeekBar.setProgress(2);
		cravingSeekBar.setProgress(0);

		emotionSeekBar.invalidate();
		cravingSeekBar.invalidate();

		gpsRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				enableSend(true);
			}
		});
	}

	/** remove the dialog and release the resources */
	public void clear() {
		if (mainLayout != null && boxLayout != null && boxLayout.getParent() != null
				&& boxLayout.getParent().equals(mainLayout))
			mainLayout.removeView(boxLayout);
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

	/** show the dialog */
	public void show() {
		enableSend(false);
		PreferenceControl.setTestSuccess();
		help.setText("");
		questionLayout.setVisibility(View.VISIBLE);
		boxLayout.setVisibility(View.VISIBLE);
		send.setOnClickListener(endListener);
		notSend.setOnClickListener(cancelListener);

		gpsRadioGroup.check(R.id.msg_gps_no);
		enableSend(false);
		MainActivity.getMainActivity().enableTabAndClick(false);
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
				ClickLog.Log(ClickLogId.TEST_QUESTION_SEND_EMPTY);
			else
				ClickLog.Log(ClickLogId.TEST_QUESTION_SEND);

			boxLayout.setVisibility(View.INVISIBLE);
			boolean enableGPS = gpsRadioGroup.getCheckedRadioButtonId() == R.id.msg_gps_yes;
			int craving = cravingSeekBar.getProgress();
			int emotion = emotionSeekBar.getProgress();

			testQuestionCaller.writeQuestionFile(emotion, craving);
			gpsInterface.initializeGPS(enableGPS);
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
			ClickLog.Log(ClickLogId.TEST_QUESTION_CANCEL);
			boxLayout.setVisibility(View.INVISIBLE);
			boolean enableGPS = false;
			int craving = -1;
			int emotion = -1;

			testQuestionCaller.writeQuestionFile(emotion, craving);
			gpsInterface.initializeGPS(enableGPS);
		}
	}

	/** show waiting message */
	public void showWaiting() {
		send.setOnClickListener(null);
		notSend.setOnClickListener(null);
		help.setText(R.string.wait);
		questionLayout.setVisibility(View.INVISIBLE);
		boxLayout.setVisibility(View.VISIBLE);
		PreferenceControl.setLatestTestCompleteTime(System.currentTimeMillis());
	}

	/** close the dialog */
	public void close() {
		if (boxLayout != null)
			boxLayout.setVisibility(View.INVISIBLE);
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
			seekBar.invalidate();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private class CravingListener implements SeekBar.OnSeekBarChangeListener {
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
			seekBar.invalidate();
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
