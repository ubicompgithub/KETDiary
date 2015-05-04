package ubicomp.ketdiary.test.ui;

import java.io.File;
import java.text.DecimalFormat;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.file.MainStorage;
import ubicomp.ketdiary.data.structure.UserVoiceFeedback;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * Dialog for recording user's feedback when the user retry the BrAC test
 * 
 * @author Stanley Wang
 */
public class FeedbackDialog {

	private Context context;
	private LayoutInflater inflater;
	private RelativeLayout mainLayout, barLayout = null;
	private LinearLayout boxLayout = null;
	private RelativeLayout leftLayout, rightLayout, topLayout;
	private TextView help, leftText, rightText;

	private Typeface wordTypefaceBold;

	private ImageView leftImg, bar, barBg, barStart, barEnd;
	private RelativeLayout.LayoutParams barParam;
	private File mainDirectory;

	private static final int MAX_RECORD_TIME = 30 * 1000;

	private static final int STATE_INIT = 0;
	private static final int STATE_ON_RECORD = 2;
	private static final int STATE_PREPARING = 3;
	private static final int STATE_BEFORE_INIT = 4;

	private long timestamp = 0;

	private MediaRecorder mediaRecorder;
	private final ChangeStateHandler changeStateHandler = new ChangeStateHandler();
	private final RecListener recListener = new RecListener();
	private final EndRecListener endRecListener = new EndRecListener();

	private RecordCountDownTimer countDownTimer;

	private FeedbackDialogCaller feedbackDialogCaller;

	private boolean testSuccess = false;

	private DecimalFormat format;

	/**
	 * Constructor
	 * 
	 * @param caller
	 *            Caller of the dialog
	 * @param mainLayout
	 *            RelativeLayout contains the dialog
	 */
	public FeedbackDialog(FeedbackDialogCaller caller, RelativeLayout mainLayout) {
		this.context = App.getContext();
		this.feedbackDialogCaller = caller;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		boxLayout = (LinearLayout) inflater.inflate(R.layout.dialog_feedback, null);
		this.mainLayout = mainLayout;
		wordTypefaceBold = Typefaces.getWordTypefaceBold();

		help = (TextView) boxLayout.findViewById(R.id.feedback_help);
		leftText = (TextView) boxLayout.findViewById(R.id.feedback_left_text);
		rightText = (TextView) boxLayout.findViewById(R.id.feedback_right_text);
		help.setTypeface(wordTypefaceBold);
		leftText.setTypeface(wordTypefaceBold);
		rightText.setTypeface(wordTypefaceBold);

		topLayout = (RelativeLayout) boxLayout.findViewById(R.id.feedback_top_bg);
		leftLayout = (RelativeLayout) boxLayout.findViewById(R.id.feedback_left_bg);
		rightLayout = (RelativeLayout) boxLayout.findViewById(R.id.feedback_right_bg);

		barLayout = (RelativeLayout) boxLayout.findViewById(R.id.feedback_bar_layout);
		bar = (ImageView) boxLayout.findViewById(R.id.feedback_cur_bar);
		barBg = (ImageView) boxLayout.findViewById(R.id.feedback_bar);
		barStart = (ImageView) boxLayout.findViewById(R.id.feedback_cur_bar_start);
		barEnd = (ImageView) boxLayout.findViewById(R.id.feedback_cur_bar_end);

		leftImg = (ImageView) boxLayout.findViewById(R.id.feedback_left_image);

		mainLayout.addView(boxLayout);
		boxLayout.setVisibility(View.INVISIBLE);

		barParam = (LayoutParams) bar.getLayoutParams();

		format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		format.setMinimumIntegerDigits(2);

		rightLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setStorage();
				ClickLog.Log(ClickLogId.TEST_FEEDBACK_DONE);
				File file = new File(mainDirectory, timestamp + ".3gp");
				DatabaseControl db = new DatabaseControl();
				db.insertUserVoiceFeedback(new UserVoiceFeedback(System.currentTimeMillis(), timestamp, testSuccess,
						file.exists()));
				PreferenceControl.setUpdateDetection(false);
				if (testSuccess)
					feedbackDialogCaller.feedbackToTestQuestionDialog();
				else {
					PreferenceControl.setUpdateDetection(false);
					PreferenceControl.setUpdateDetectionTimestamp(0);
					feedbackDialogCaller.feedbackToFail();
				}
			}
		});

	}

	private void setStorage() {
		File dir = MainStorage.getMainStorageDirectory();
		mainDirectory = new File(dir, "feedbacks");
		if (!mainDirectory.exists())
			if (!mainDirectory.mkdirs()) {
				Log.d("Feedback", "fail to mkdir");
				return;
			}
	}

	/** initialize the dialog */
	public void initialize() {

		RelativeLayout.LayoutParams boxParam = (LayoutParams) boxLayout.getLayoutParams();
		boxParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		setStorage();
	}

	private void setState(int state) {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
		barParam.width = 0;
		bar.invalidate();
		barLayout.updateViewLayout(bar, barParam);
		switch (state) {
		case STATE_INIT:

			leftLayout.setOnClickListener(recListener);
			help.setText(R.string.feedback_help);
			leftText.setText("");
			setStorage();
			File file = new File(mainDirectory, timestamp + ".3gp");
			if (file.exists())
				rightText.setText(R.string.done);
			else
				rightText.setText(R.string.feedback_skip);

			leftImg.setVisibility(View.VISIBLE);
			barLayout.setVisibility(View.GONE);
			topLayout.setBackgroundResource(R.drawable.feedback_dialog_top_small);
			leftLayout.setBackgroundResource(R.drawable.feedback_button_left_small);
			rightLayout.setBackgroundResource(R.drawable.feedback_button_right_small);
			topLayout.invalidate();
			leftLayout.invalidate();
			rightLayout.invalidate();

			break;
		case STATE_ON_RECORD:
			countDownTimer = new RecordCountDownTimer(MAX_RECORD_TIME);
			countDownTimer.start();

			leftText.setText(R.string.feedback_record_again);
			rightText.setText(R.string.done);

			leftImg.setVisibility(View.GONE);
			leftLayout.setOnClickListener(endRecListener);
			help.setText(R.string.audio_box_recording);
			barLayout.setVisibility(View.VISIBLE);
			topLayout.setBackgroundResource(R.drawable.feedback_dialog_top);
			leftLayout.setBackgroundResource(R.drawable.feedback_button_left);
			rightLayout.setBackgroundResource(R.drawable.feedback_button_right);
			topLayout.invalidate();
			leftLayout.invalidate();
			rightLayout.invalidate();
			break;
		case STATE_PREPARING:
			leftLayout.setOnClickListener(null);
			help.setText(R.string.audio_box_preparing);
			leftText.setText("");
			rightText.setText("");

			leftImg.setVisibility(View.VISIBLE);
			barLayout.setVisibility(View.GONE);
			topLayout.setBackgroundResource(R.drawable.feedback_dialog_top_small);
			leftLayout.setBackgroundResource(R.drawable.feedback_button_left_small);
			rightLayout.setBackgroundResource(R.drawable.feedback_button_right_small);
			topLayout.invalidate();
			leftLayout.invalidate();
			rightLayout.invalidate();
			break;
		case STATE_BEFORE_INIT:
			leftLayout.setOnClickListener(null);
			help.setText(R.string.audio_box_preparing);
			leftText.setText("");
			rightText.setText("");

			leftImg.setVisibility(View.VISIBLE);
			barLayout.setVisibility(View.GONE);
			topLayout.setBackgroundResource(R.drawable.feedback_dialog_top_small);
			leftLayout.setBackgroundResource(R.drawable.feedback_button_left_small);
			rightLayout.setBackgroundResource(R.drawable.feedback_button_right_small);
			topLayout.invalidate();
			leftLayout.invalidate();
			rightLayout.invalidate();
			break;
		}
		leftImg.invalidate();
	}

	/** remove the dialog and release the resources */
	public void clear() {
		if (mainLayout != null && boxLayout != null && boxLayout.getParent() != null
				&& boxLayout.getParent().equals(mainLayout))
			mainLayout.removeView(boxLayout);
	}

	/**
	 * show the dialog
	 * 
	 * @param testSuccess
	 *            If the BrAC test successfully complete
	 */
	public void show(boolean testSuccess) {
		PreferenceControl.setTestSuccess();
		timestamp = PreferenceControl.getUpdateDetectionTimestamp();
		boxLayout.setVisibility(View.VISIBLE);
		MainActivity.getMainActivity().enableTabAndClick(false);
		this.testSuccess = testSuccess;
		setState(STATE_INIT);
	}

	/**close the dialog*/
	public void close() {
		if (boxLayout != null)
			boxLayout.setVisibility(View.INVISIBLE);
		if (changeStateHandler != null)
			changeStateHandler.removeMessages(0);
		if (mediaRecorder != null) {
			try {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			} catch (IllegalStateException e) {
			}
		}
		File file = new File(mainDirectory, timestamp + ".3gp");
		if (file.exists())
			CustomToast.generateToast(R.string.feedback_record_toast, 0);
	}

	private class RecListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			setState(STATE_PREPARING);
			ClickLog.Log(ClickLogId.TEST_FEEDBACK_REC_BUTTON);
			setStorage();
			File file = new File(mainDirectory, timestamp + ".3gp");
			Log.d("Feedback", "file=" + file.getAbsolutePath());
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mediaRecorder.setOutputFile(file.getAbsolutePath());
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setMaxDuration(MAX_RECORD_TIME);
			mediaRecorder.setOnInfoListener(new RecorderInfoListener());
			try {
				mediaRecorder.prepare();
			} catch (Exception e) {
				setState(STATE_INIT);
				Log.d("Feedback", "fail on prepare");
			}
			mediaRecorder.start();
			setState(STATE_ON_RECORD);
		}
	}

	private class EndRecListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			setState(STATE_BEFORE_INIT);
			ClickLog.Log(ClickLogId.TEST_FEEDBACK_STOP_REC_BUTTON);
			if (mediaRecorder != null) {
				try {
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
				} catch (IllegalStateException e) {
				}
			}

			setState(STATE_PREPARING);
			setStorage();
			File file = new File(mainDirectory, timestamp + ".3gp");
			Log.d("Feedback", "file=" + file.getAbsolutePath());
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mediaRecorder.setOutputFile(file.getAbsolutePath());
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setMaxDuration(MAX_RECORD_TIME);
			mediaRecorder.setOnInfoListener(new RecorderInfoListener());
			try {
				mediaRecorder.prepare();
			} catch (Exception e) {
				setState(STATE_INIT);
				Log.d("Feedback", "fail on prepare");
			}
			mediaRecorder.start();
			setState(STATE_ON_RECORD);
		}
	}

	private class RecorderInfoListener implements MediaRecorder.OnInfoListener {

		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
				if (mediaRecorder != null) {
					try {
						mediaRecorder.stop();
						mediaRecorder.release();
						mediaRecorder = null;
						// CustomToast.generateToast(R.string.feedback_record_toast,
						// 0);
					} catch (IllegalStateException e) {
					}
				}
				setState(STATE_INIT);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private class ChangeStateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int state = msg.getData().getInt("STATE");
			setState(state);
		}
	}

	private class RecordCountDownTimer extends CountDownTimer {

		private long TOTAL_MILLIS;
		private String TOTAL_MILLIS_STR;
		private int MAX_BAR_LENGTH;

		public RecordCountDownTimer(long millisInFuture) {
			super(millisInFuture, 10);
			this.TOTAL_MILLIS = millisInFuture;
			this.TOTAL_MILLIS_STR = "00:" + format.format(TOTAL_MILLIS / 1000L);
			int bar_bg_width = barBg.getRight() - barBg.getLeft();
			int bar_start_width = barStart.getRight() - barStart.getLeft();
			int bar_end_width = barEnd.getRight() - barEnd.getLeft();
			MAX_BAR_LENGTH = bar_bg_width - bar_start_width - bar_end_width;
		}

		@Override
		public void onFinish() {
			help.setText(R.string.feedback_help);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			double length = ((double) (TOTAL_MILLIS - millisUntilFinished)) / (double) TOTAL_MILLIS;
			barParam.width = (int) (length * MAX_BAR_LENGTH);
			help.setText(App.getContext().getString(R.string.audio_box_recording) + "(00:"
					+ format.format(millisUntilFinished / 1000L) + "/" + TOTAL_MILLIS_STR + ")");
			bar.invalidate();
			barLayout.updateViewLayout(bar, barParam);
		}
	}
}
