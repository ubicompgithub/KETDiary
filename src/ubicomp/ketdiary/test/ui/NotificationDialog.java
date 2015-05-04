package ubicomp.ketdiary.test.ui;

import java.util.Random;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.EmotionActivity;
import ubicomp.ketdiary.main.FacebookActivity;
import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.StorytellingTestActivity;
import ubicomp.ketdiary.main.ui.EnablePage;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.system.check.StartDateCheck;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Dialog for notifying functions which is seldom used by the user
 * 
 * @author Stanley Wang
 */
public class NotificationDialog {

	private FrameLayout layout;

	private TextView title, subtitle, comment, cancelText, gotoText;
	private ImageView image;

	private String[] subtitles = App.getContext().getResources().getStringArray(R.array.notification_subtitles);
	private String[] comments = App.getContext().getResources().getStringArray(R.array.notification_comments);
	private int[] IMAGE_ID = { R.drawable.notification_brac_test, R.drawable.notification_additional_questionnaire,
			R.drawable.notification_emotion_diy, R.drawable.notification_voice_record,
			R.drawable.notification_emotion_management, R.drawable.notification_storytelling,
			R.drawable.notification_fb, R.drawable.notification_questionnaire,
			R.drawable.notification_storytelling_test };

	private DatabaseControl db = new DatabaseControl();

	private RelativeLayout mainLayout;
	private EnablePage enablePage;

	private int showType = -1;

	private NotificationDialogCaller notificationCaller;

	private Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Activity context
	 * @param mainLayout
	 *            Layout contains the dialog
	 * @param enablePage
	 *            class supporting EnablePage
	 * @param caller
	 *            Caller of the dialog
	 */
	public NotificationDialog(Context context, RelativeLayout mainLayout, EnablePage enablePage,
			NotificationDialogCaller caller) {

		this.context = context;
		this.mainLayout = mainLayout;
		this.enablePage = enablePage;
		this.notificationCaller = caller;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = (FrameLayout) inflater.inflate(R.layout.dialog_notification, null);

		Typeface wordTypefaceBold = Typefaces.getWordTypefaceBold();
		Typeface wordTypeface = Typefaces.getWordTypeface();

		title = (TextView) layout.findViewById(R.id.notification_title);
		title.setTypeface(wordTypefaceBold);

		subtitle = (TextView) layout.findViewById(R.id.notification_subtitle);
		subtitle.setTypeface(wordTypefaceBold);

		comment = (TextView) layout.findViewById(R.id.notification_comment);
		comment.setTypeface(wordTypeface);

		cancelText = (TextView) layout.findViewById(R.id.notification_cancel);
		cancelText.setTypeface(wordTypefaceBold);
		cancelText.setOnClickListener(new CancelOnClickListener());
		cancelText.setOnTouchListener(new EndOnTouchListener());

		gotoText = (TextView) layout.findViewById(R.id.notification_ok);
		gotoText.setTypeface(wordTypefaceBold);
		gotoText.setOnClickListener(new GotoOnClickListener());
		gotoText.setOnTouchListener(new EndOnTouchListener());

		image = (ImageView) layout.findViewById(R.id.notification_image);
	}

	/** Initialize the dialog */
	public boolean initialize() {

		showType = -1;

		if (!StartDateCheck.afterStartDate())
			return false;

		boolean check = PreferenceControl.showNotificationDialog();

		if (!check)
			return false;

		long curTime = System.currentTimeMillis();
		TimeValue curTv = TimeValue.generate(curTime);

		TimeValue[] tvs = new TimeValue[subtitles.length];
		tvs[0] = db.getLatestDetection().getTv();
		tvs[1] = db.getLatestAdditionalQuestionnaire().getTv();
		tvs[2] = db.getLatestEmotionDIY().getTv();
		tvs[3] = db.getLatestUserVoiceRecord().getTv();
		tvs[4] = db.getLatestEmotionManagement().getTv();
		tvs[5] = db.getLatestStorytellingRead().getTv();
		tvs[6] = db.getLatestFacebookInfo().getTv();
		tvs[7] = db.getLatestQuestionnaire().getTv();
		tvs[8] = db.getLatestStorytellingTest().getTv();

		if (curTv.getWeek() <= 0)// StorytellingReading
			tvs[5] = curTv;

		boolean[] show_dialog = new boolean[subtitles.length];

		int mod = 0;
		for (int i = 0; i < tvs.length; ++i) {
			if (i != 6)
				show_dialog[i] = tvs[i].showNotificationDialog(curTime, false);
			else
				show_dialog[i] = tvs[i].showNotificationDialog(curTime, true);
			if (show_dialog[i])
				++mod;
		}

		if (mod == 0)
			return false;

		PreferenceControl.setShowedNotificationDialog();

		Random rand = new Random();
		int randNum = rand.nextInt(mod);
		int type = -1;
		for (int i = 0; i < show_dialog.length; ++i) {
			if (randNum == 0) {
				type = i;
				break;
			} else if (show_dialog[i])
				--randNum;
		}

		settingType(type);

		clear();
		addView();

		return true;
	}

	private void settingType(int type) {
		if (type < 0)
			return;

		showType = type;

		subtitle.setText(subtitles[type]);
		comment.setText(comments[type]);
		image.setImageResource(IMAGE_ID[type]);
	}

	private void addView() {
		mainLayout.addView(layout);
		LayoutParams param = layout.getLayoutParams();
		param.width = param.height = LayoutParams.MATCH_PARENT;
		enablePage.enablePage(false);
	}

	/** Remove the dialog and release the resources */
	public void clear() {
		if (layout != null && layout.getParent() != null) {
			ViewGroup vg = (ViewGroup) layout.getParent();
			vg.removeView(layout);
			enablePage.enablePage(true);
		}
	}

	private class CancelOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ClickLog.Log(ClickLogId.TEST_NOTIFICATION_CANCEL);
			clear();
		}
	}

	private class GotoOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ClickLog.Log(ClickLogId.TEST_NOTIFICATION_GOTO);
			clear();
			Integer[] page_states = new DatabaseControl().getDetectionScoreByWeek();
			int page_week = page_states.length - 1;
			switch (showType) {
			case 0:
				notificationCaller.notifyStartButton();
				break;
			case 1:
				notificationCaller.notifyAdditionalQuestionnaire();
				break;
			case 2:
				Intent intentEmotionActivity = new Intent(context, EmotionActivity.class);
				context.startActivity(intentEmotionActivity);
				break;
			case 3:
				MainActivity.getMainActivity().changeTab(2, MainActivity.ACTION_RECORD);
				break;
			case 4:
				MainActivity.getMainActivity().changeTab(2, MainActivity.ACTION_RECORD);
				break;
			case 5:
				MainActivity.getMainActivity().changeTab(2);
				break;
			case 6:
				Intent intentFB = new Intent(context, FacebookActivity.class);
				intentFB.putExtra("image_week", page_week);
				intentFB.putExtra("image_score", page_states[page_week]);
				context.startActivity(intentFB);
				break;
			case 7:
				MainActivity.getMainActivity().changeTab(1, MainActivity.ACTION_QUESTIONNAIRE);
				break;
			case 8:
				Intent intentStorytellingTest = new Intent(context, StorytellingTestActivity.class);
				intentStorytellingTest.putExtra("image_week", page_week);
				intentStorytellingTest.putExtra("image_score", page_states[page_week]);
				context.startActivity(intentStorytellingTest);
			}

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
