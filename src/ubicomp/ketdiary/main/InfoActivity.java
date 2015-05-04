package ubicomp.ketdiary.main;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.BarButtonGenerator;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.spinnergroup.MultiRadioGroup;
import ubicomp.ketdiary.main.ui.spinnergroup.SingleRadioGroup;
import ubicomp.ketdiary.statistic.ui.questionnaire.content.ConnectSocialInfo;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Activity for normal user setting
 * 
 * @author Stanley Wang
 */
public class InfoActivity extends Activity {

	private LayoutInflater inflater;

	private Typeface wordTypeface;
	private Typeface wordTypefaceBold;

	private LinearLayout titleLayout;
	private LinearLayout mainLayout;

	private Activity activity;

	private View fbView;
	private View uvView;
	private RelativeLayout[] recreationViews;
	private RelativeLayout[] contactViews;
	private MultiRadioGroup socialGroup;
	private View socialGroupView;
	private SingleRadioGroup notificationGroup;
	private View notificationGroupView;
	private View bluetoothView;

	private static final int PRIVACY = 0, RECREATION = 100, CONTACT = 200, SOCIAL = 300, ALARM = 400, SYSTEM = 500;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		this.activity = this;
		titleLayout = (LinearLayout) this.findViewById(R.id.info_title_layout);
		mainLayout = (LinearLayout) this.findViewById(R.id.info_item_list);
		inflater = LayoutInflater.from(activity);
		wordTypeface = Typefaces.getWordTypeface();
		wordTypefaceBold = Typefaces.getWordTypefaceBold();

		mainLayout.removeAllViews();

		View title = BarButtonGenerator.createTitleView(R.string.info_title);
		titleLayout.addView(title);

		initialization();

	}

	private void initialization(){
		View instructionView = createListView(R.string.info_instruction, new OnClickListener() {
			private boolean visible = false;

			@Override
			public void onClick(View v) {
				// ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SOCIAL);
				Log.i("FORTEST", "instructionsView");
			}
		});
		mainLayout.addView(instructionView);

		View bluetoothView = createListView(R.string.info_bluetooth, new OnClickListener() {
			private boolean visible = false;

			@Override
			public void onClick(View v) {
				// ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SOCIAL);
				MainActivity.getMainActivity().newBLE();
				Log.i("FORTEST", "bluetoothView");
			}
		});
		mainLayout.addView(bluetoothView);

		View faqView = createListView(R.string.info_faq, new OnClickListener() {
			private boolean visible = false;

			@Override
			public void onClick(View v) {
				// ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SOCIAL);
				Log.i("FORTEST", "faqView");
			}
		});
		mainLayout.addView(faqView);

		View settingView = createListView(R.string.info_setting, new OnClickListener() {
			private boolean visible = false;

			@Override
			public void onClick(View v) {
				// ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SOCIAL);
				Log.i("FORTEST", "settingView");
			}
		});
		mainLayout.addView(settingView);

	}


	


	// private void setting() {

	// 	RelativeLayout privacyView = createListView(R.string.setting_privacy, new OnClickListener() {
	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + PRIVACY);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				fbView.setVisibility(View.GONE);
	// 				uvView.setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				fbView.setVisibility(View.VISIBLE);
	// 				uvView.setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}
	// 	});
	// 	mainLayout.addView(privacyView);

	// 	fbView = createCheckBoxView(R.string.setting_facebook, new OnCheckedChangeListener() {

	// 		@Override
	// 		public void onCheckedChanged(RadioGroup group, int checkedId) {
	// 			ClickLog.Log(ClickLogId.SETTING_CHECK + PRIVACY + 0);
	// 			boolean isChecked = (checkedId == R.id.question_check_yes);
	// 			PreferenceControl.setUploadFacebookInfo(isChecked);
	// 		}
	// 	}, PreferenceControl.uploadFacebookInfo());
	// 	fbView.setVisibility(View.GONE);
	// 	mainLayout.addView(fbView);

	// 	uvView = createCheckBoxView(R.string.setting_user_voice, new OnCheckedChangeListener() {
	// 		@Override
	// 		public void onCheckedChanged(RadioGroup group, int checkedId) {
	// 			ClickLog.Log(ClickLogId.SETTING_CHECK + PRIVACY + 1);
	// 			boolean isChecked = (checkedId == R.id.question_check_yes);
	// 			PreferenceControl.setUploadVoiceRecord(isChecked);
	// 		}
	// 	}, PreferenceControl.uploadVoiceRecord());
	// 	uvView.setVisibility(View.GONE);
	// 	mainLayout.addView(uvView);

	// 	RelativeLayout recreationView = createListView(R.string.setting_recreation, new OnClickListener() {

	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + RECREATION);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				for (int i = 0; i < recreationViews.length; ++i)
	// 					recreationViews[i].setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				for (int i = 0; i < recreationViews.length; ++i)
	// 					recreationViews[i].setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}
	// 	});
	// 	mainLayout.addView(recreationView);

	// 	String[] recreations = PreferenceControl.getRecreations();
	// 	recreationViews = new RelativeLayout[recreations.length];
	// 	for (int i = 0; i < recreations.length; ++i) {
	// 		recreationViews[i] = createEditRecreationView(recreations[i], i);
	// 		recreationViews[i].setVisibility(View.GONE);
	// 		mainLayout.addView(recreationViews[i]);
	// 	}

	// 	RelativeLayout contactView = createListView(R.string.setting_contact, new OnClickListener() {
	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + CONTACT);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				for (int i = 0; i < contactViews.length; ++i)
	// 					contactViews[i].setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				for (int i = 0; i < contactViews.length; ++i)
	// 					contactViews[i].setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}

	// 	});
	// 	mainLayout.addView(contactView);

	// 	String[] names = PreferenceControl.getConnectFamilyName();
	// 	String[] phones = PreferenceControl.getConnectFamilyPhone();
	// 	int contactLen = names.length;
	// 	contactViews = new RelativeLayout[contactLen];
	// 	for (int i = 0; i < contactLen; ++i) {
	// 		contactViews[i] = createEditPhoneView(names[i], phones[i], i);
	// 		contactViews[i].setVisibility(View.GONE);
	// 		mainLayout.addView(contactViews[i]);
	// 	}

	// 	int[] socialSelections = PreferenceControl.getConnectSocialHelpIdx();
	// 	boolean[] socialSelected = new boolean[ConnectSocialInfo.NAME.length];
	// 	for (int i = 0; i < socialSelected.length; ++i) {
	// 		socialSelected[i] = false;
	// 		for (int j = 0; j < socialSelections.length; ++j)
	// 			if (i == socialSelections[j])
	// 				socialSelected[i] = true;
	// 	}

	// 	socialGroup = new MultiRadioGroup(activity, ConnectSocialInfo.NAME, socialSelected, 3, R.string.setting_limit,
	// 			ClickLogId.SETTING_SELECT + SOCIAL);
	// 	socialGroupView = socialGroup.getView();

	// 	View socialView = createListView(R.string.setting_social, new OnClickListener() {
	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SOCIAL);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				socialGroupView.setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				socialGroupView.setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}
	// 	});
	// 	mainLayout.addView(socialView);

	// 	mainLayout.addView(socialGroupView);
	// 	socialGroupView.setVisibility(View.GONE);

	// 	String[] strs = App.getContext().getResources().getStringArray(R.array.setting_time_gap);
	// 	notificationGroup = new SingleRadioGroup(activity, strs, PreferenceControl.getNotificationTimeIdx(),
	// 			ClickLogId.SETTING_SELECT + ALARM);
	// 	notificationGroupView = notificationGroup.getView();
	// 	notificationGroupView.setVisibility(View.GONE);

	// 	RelativeLayout alarmView = createListView(R.string.setting_alarm, new OnClickListener() {
	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + ALARM);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				notificationGroupView.setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				notificationGroupView.setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}

	// 	});
	// 	mainLayout.addView(alarmView);
	// 	mainLayout.addView(notificationGroupView);

	// 	bluetoothView = BarButtonGenerator.createSettingButtonView(R.string.setting_bluetooth, new OnClickListener() {

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_CHECK + SYSTEM);
	// 			Intent intentBluetooth = new Intent();
	// 			intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
	// 			startActivity(intentBluetooth);
	// 		}

	// 	});
	// 	bluetoothView.setVisibility(View.GONE);

	// 	RelativeLayout systemView = createListView(R.string.setting_system, new OnClickListener() {
	// 		private boolean visible = false;

	// 		@Override
	// 		public void onClick(View v) {
	// 			ClickLog.Log(ClickLogId.SETTING_TITLE_LIST + SYSTEM);
	// 			ImageView list = (ImageView) v.findViewById(R.id.question_list);
	// 			if (visible) {
	// 				bluetoothView.setVisibility(View.GONE);
	// 				list.setVisibility(View.INVISIBLE);
	// 			} else {
	// 				bluetoothView.setVisibility(View.VISIBLE);
	// 				list.setVisibility(View.VISIBLE);
	// 			}
	// 			visible = !visible;
	// 		}

	// 	});
	// 	mainLayout.addView(systemView);
	// 	mainLayout.addView(bluetoothView);

	// }

	@Override
	protected void onResume() {
		super.onResume();
//		ClickLog.Log(ClickLogId.SETTING_ENTER);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private RelativeLayout createListView(int titleStr, OnClickListener listener) {

		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.bar_list_item, null);
		TextView text = (TextView) layout.findViewById(R.id.question_description);
		text.setTypeface(wordTypefaceBold);
		text.setText(titleStr);
		layout.setOnClickListener(listener);
		return layout;
	}

	

}
