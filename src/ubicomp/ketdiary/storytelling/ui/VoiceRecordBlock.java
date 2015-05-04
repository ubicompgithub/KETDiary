package ubicomp.ketdiary.storytelling.ui;

import java.io.File;
import java.text.DecimalFormat;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.file.MainStorage;
import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.data.structure.UserVoiceRecord;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.main.ui.toast.CustomToastSmall;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class VoiceRecordBlock implements RecorderCallee {
	private RecordBlockCaller recordCaller;
	private Context context;
	private LayoutInflater inflater;
	private DatabaseControl db;

	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;

	private final static int MAX_MEDIA_DURATION = 60000;
	private static final int STATE_INIT = 0;
	private static final int STATE_ON_PLAY = 1;
	private static final int STATE_ON_RECORD = 2;
	private static final int STATE_PREPARING = 3;
	private static final int STATE_BEFORE_INIT = 4;
	private File mainDirectory;

	private Drawable playDrawable, playOffDrawable, recDrawable, stopDrawable;

	private RelativeLayout contentLayout, barLayout;

	private TextView help, countDownText;
	private ImageView bar_bg, barStart, barEnd, bar;

	private int chartItemIdx;
	private TimeValue curTV;
	private ImageView topIcon, bottomIcon;
	private RelativeLayout topButton, bottomButton;

	private final ChangeStateHandler changeStateHandler = new ChangeStateHandler();
	private final RecListener recListener = new RecListener();
	private final EndRecListener endRecListener = new EndRecListener();
	private final PlayListener playListener = new PlayListener();
	private final EndPlayListener endPlayListener = new EndPlayListener();

	private RelativeLayout.LayoutParams barParam;

	private int MAX_BAR_LENGTH;

	private RecordCountDownTimer countDownTimer;

	private DecimalFormat format;

	public VoiceRecordBlock(RecordBlockCaller recordCaller) {
		this.recordCaller = recordCaller;
		context = App.getContext();
		db = new DatabaseControl();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentLayout = (RelativeLayout) inflater.inflate(R.layout.storytelling_voice_record_block, null);
		barLayout = (RelativeLayout) contentLayout.findViewById(R.id.voice_record_bar_layout);
		help = (TextView) contentLayout.findViewById(R.id.voice_record_help);
		help.setTypeface(Typefaces.getWordTypefaceBold());
		countDownText = (TextView) contentLayout.findViewById(R.id.voice_record_count_down_help);
		countDownText.setTypeface(Typefaces.getWordTypefaceBold());

		bar_bg = (ImageView) contentLayout.findViewById(R.id.voice_record_bar_bg);
		barStart = (ImageView) contentLayout.findViewById(R.id.voice_record_bar_start);
		barEnd = (ImageView) contentLayout.findViewById(R.id.voice_record_bar_end);
		bar = (ImageView) contentLayout.findViewById(R.id.voice_record_bar);

		barParam = (LayoutParams) bar.getLayoutParams();
		barParam.width = 0;

		recDrawable = context.getResources().getDrawable(R.drawable.icon_rec);
		playDrawable = context.getResources().getDrawable(R.drawable.icon_play);
		stopDrawable = context.getResources().getDrawable(R.drawable.icon_stop);
		playOffDrawable = null;

		topIcon = (ImageView) contentLayout.findViewById(R.id.voice_record_top_icon);
		bottomIcon = (ImageView) contentLayout.findViewById(R.id.voice_record_bottom_icon);

		topButton = (RelativeLayout) contentLayout.findViewById(R.id.voice_record_top_button);
		bottomButton = (RelativeLayout) contentLayout.findViewById(R.id.voice_record_bottom_button);

		format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		format.setMinimumIntegerDigits(2);

		setStorage();
	}

	private void setStorage() {
		File dir = MainStorage.getMainStorageDirectory();
		mainDirectory = new File(dir, "audio_records");
		if (!mainDirectory.exists())
			if (!mainDirectory.mkdirs()) {
				return;
			}
	}

	@Override
	public View getRecordBox(TimeValue tv, int idx) {
		chartItemIdx = idx;
		curTV = tv;
		setButtonState(STATE_INIT);
		enableRecordBox(true);
		return contentLayout;
	}

	private void setButtonState(int state) {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}

		barParam.width = 0;
		bar.invalidate();
		barLayout.updateViewLayout(bar, barParam);
		countDownText.setText("");
		switch (state) {
		case STATE_INIT:
			help.setText(R.string.voice_main_help);
			topIcon.setImageDrawable(recDrawable);
			topButton.setOnClickListener(recListener);
			topButton.setBackgroundResource(R.drawable.record_box_top_button);
			if (db.hasUserVoiceRecord(curTV)) {
				bottomIcon.setImageDrawable(playDrawable);
				bottomButton.setBackgroundResource(R.drawable.record_box_bottom_button);
				bottomButton.setOnClickListener(playListener);
			} else {
				bottomIcon.setImageDrawable(playOffDrawable);
				bottomButton.setBackgroundResource(R.drawable.record_button_bottom);
				bottomButton.setOnClickListener(null);
			}
			recordCaller.enablePage(true, 0);
			break;
		case STATE_ON_PLAY:
			help.setText(R.string.audio_box_playing);
			countDownTimer = new RecordCountDownTimer(mediaPlayer.getDuration());
			countDownTimer.start();
			topIcon.setImageDrawable(null);
			topButton.setOnClickListener(null);
			topButton.setBackgroundResource(R.drawable.record_button_top);
			bottomIcon.setImageDrawable(stopDrawable);
			bottomButton.setOnClickListener(endPlayListener);
			bottomButton.setBackgroundResource(R.drawable.record_box_bottom_button);
			recordCaller.enablePage(false, 0);
			break;
		case STATE_ON_RECORD:
			help.setText(R.string.audio_box_recording);
			countDownTimer = new RecordCountDownTimer(MAX_MEDIA_DURATION);
			countDownTimer.start();
			topIcon.setImageDrawable(stopDrawable);
			topButton.setOnClickListener(endRecListener);
			topButton.setBackgroundResource(R.drawable.record_box_top_button);
			bottomIcon.setImageDrawable(null);
			bottomButton.setOnClickListener(null);
			bottomButton.setBackgroundResource(R.drawable.record_button_bottom);
			recordCaller.enablePage(false, 0);
			break;
		case STATE_PREPARING:
			help.setText(R.string.audio_box_preparing);
			topButton.setOnClickListener(null);
			topButton.setBackgroundResource(R.drawable.record_button_top);
			bottomButton.setOnClickListener(null);
			bottomButton.setBackgroundResource(R.drawable.record_button_bottom);
			recordCaller.enablePage(true, 0);
			break;
		case STATE_BEFORE_INIT:
			help.setText(R.string.audio_box_preparing);
			topButton.setOnClickListener(null);
			topButton.setBackgroundResource(R.drawable.record_button_top);
			bottomButton.setOnClickListener(null);
			bottomButton.setBackgroundResource(R.drawable.record_button_bottom);
			break;
		}
		topIcon.invalidate();
		bottomIcon.invalidate();
		topButton.invalidate();
		bottomButton.invalidate();
	}

	private class RecListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (curTV == null) {
				help.setText("null");
				return;
			}
			ClickLog.Log(ClickLogId.STORYTELLING_RECORD_REC);
			setButtonState(STATE_PREPARING);
			setStorage();
			File file = new File(mainDirectory, curTV.toFileString() + ".3gp");
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
			mediaRecorder.setOutputFile(file.getAbsolutePath());
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
			mediaRecorder.setMaxDuration(MAX_MEDIA_DURATION);
			mediaRecorder.setOnInfoListener(new RecorderInfoListener());
			try {
				mediaRecorder.prepare();
			} catch (Exception e) {
				setButtonState(STATE_INIT);
			}
			mediaRecorder.start();
			setButtonState(STATE_ON_RECORD);
		}
	}

	private class EndRecListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			setButtonState(STATE_BEFORE_INIT);
			Thread t = new Thread(new WaitRunnable(STATE_INIT));
			t.start();
			ClickLog.Log(ClickLogId.STORYTELLING_RECORD_PAUSE_REC);
			if (mediaRecorder != null) {
				try {
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
					int addScore = db.insertUserVoiceRecord(new UserVoiceRecord(System.currentTimeMillis(), curTV.getYear(),
							curTV.getMonth(), curTV.getDay(), 0));
					if (PreferenceControl.checkCouponChange())
						PreferenceControl.setCouponChange(true);

					CustomToast.generateToast(R.string.audio_box_toast_record_end, addScore);
				} catch (IllegalStateException e) {
				}
			}
			recordCaller.updateHasRecorder(chartItemIdx);
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
						int addScore = db.insertUserVoiceRecord(new UserVoiceRecord(System.currentTimeMillis(),
								curTV.getYear(), curTV.getMonth(), curTV.getDay(), 0));
						if (PreferenceControl.checkCouponChange())
							PreferenceControl.setCouponChange(true);

						CustomToast.generateToast(R.string.audio_box_toast_timeup, addScore);
						recordCaller.updateHasRecorder(chartItemIdx);
					} catch (IllegalStateException e) {
					}
				}
				setButtonState(STATE_INIT);
			}
		}
	}

	private class PlayListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			setButtonState(STATE_PREPARING);
			setStorage();
			File file = new File(mainDirectory, curTV.toFileString() + ".3gp");
			mediaPlayer = new MediaPlayer();
			if (file.exists() && db.hasUserVoiceRecord(curTV)) {
				ClickLog.Log(ClickLogId.STORYTELLING_RECORD_PLAY);
				try {
					mediaPlayer.setDataSource(file.getAbsolutePath());
					mediaPlayer.setScreenOnWhilePlaying(true);
					mediaPlayer.setVolume(5F, 5F);
					mediaPlayer.prepare();
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer arg0) {
							try {
								mediaPlayer.stop();
								mediaPlayer.release();
								mediaPlayer = null;
								setButtonState(STATE_INIT);
								CustomToastSmall.generateToast(R.string.audio_box_toast_play_end);
							} catch (IllegalStateException e) {
							}
						}
					});
					mediaPlayer.start();
				} catch (Exception e) {
					setButtonState(STATE_INIT);
				}
				setButtonState(STATE_ON_PLAY);
			}
		}
	}

	private class EndPlayListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			setButtonState(STATE_BEFORE_INIT);
			Thread t = new Thread(new WaitRunnable(STATE_INIT));
			t.start();
			ClickLog.Log(ClickLogId.STORYTELLING_RECORD_PAUSE_PLAY);
			if (mediaPlayer != null) {
				try {
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
					CustomToastSmall.generateToast(R.string.audio_box_toast_play_end);
				} catch (IllegalStateException e) {
				}
			}
		}
	}

	private class WaitRunnable implements Runnable {

		private int state;

		public WaitRunnable(int state) {
			this.state = state;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Message msg = new Message();
			Bundle data = new Bundle();
			data.putInt("STATE", state);
			msg.setData(data);
			msg.what = 0;
			changeStateHandler.sendMessage(msg);
		}
	}

	@SuppressLint("HandlerLeak")
	private class ChangeStateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int state = msg.getData().getInt("STATE");
			setButtonState(state);
		}
	}

	@Override
	public void cleanRecordBox() {
		enableRecordBox(false);
		if (changeStateHandler != null)
			changeStateHandler.removeMessages(0);
	}

	@Override
	public void enableRecordBox(boolean enable) {
		if (mediaRecorder != null) {
			try {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			} catch (IllegalStateException e) {
			}
		}
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			} catch (IllegalStateException e) {
			}
		}
		topButton.setEnabled(enable);
		bottomButton.setEnabled(enable);
	}

	private class RecordCountDownTimer extends CountDownTimer {

		private long TOTAL_MILLIS;
		private String TOTAL_MILLIS_STR;

		public RecordCountDownTimer(long millisInFuture) {
			super(millisInFuture, 10);
			this.TOTAL_MILLIS = millisInFuture;
			this.TOTAL_MILLIS_STR = "00:" + format.format(TOTAL_MILLIS / 1000L);
			int bar_bg_width = bar_bg.getRight() - bar_bg.getLeft();
			int bar_start_width = barStart.getRight() - barStart.getLeft();
			int bar_end_width = barEnd.getRight() - barEnd.getLeft();
			MAX_BAR_LENGTH = bar_bg_width - bar_start_width - bar_end_width;
		}

		@Override
		public void onFinish() {
			countDownText.setText("");
		}

		@Override
		public void onTick(long millisUntilFinished) {
			double length = ((double) (TOTAL_MILLIS - millisUntilFinished)) / (double) TOTAL_MILLIS;
			barParam.width = (int) (length * MAX_BAR_LENGTH);
			bar.invalidate();
			countDownText.setText("(00:" + format.format(millisUntilFinished / 1000L) + "/" + TOTAL_MILLIS_STR + ")");
			barLayout.updateViewLayout(bar, barParam);
		}
	}

}
