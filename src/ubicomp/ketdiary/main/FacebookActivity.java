package ubicomp.ketdiary.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.FacebookInfo;
import ubicomp.ketdiary.main.ui.BarButtonGenerator;
import ubicomp.ketdiary.main.ui.LoadingDialogControl;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.spinnergroup.SingleIconRadioGroup;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.main.ui.toast.CustomToastSmall;
import ubicomp.ketdiary.storytelling.facebook.BitmapGenerator;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.config.PreferenceControl;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.widget.LoginButton;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;

/**
 * Activity for user to upload Storytelling image on their facebook
 * 
 * @author Stanley Wang
 */
public class FacebookActivity extends Activity {

	private static final String TAG = "FACEBOOK";

	private RelativeLayout loginLayout, checkLayout;
	private RelativeLayout bgLayout;
	private ScrollView inputScrollview;
	private LinearLayout inputLayout;
	private LoginButton authButton;

	private LayoutInflater inflater;

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

	private TextView titleText, loginText;
	private ImageView image;
	private Typeface wordTypeface, wordTypefaceBold;

	private int image_week, image_score;

	private EditText texts;

	private View shareButton, inputMessage, privacySelection;

	private Bitmap state_bmp;

	private UiLifecycleHelper uiHelper;

	private static final int[] choices = { R.string.fb_friend, R.string.fb_self };
	private static final int[] icons = { R.drawable.fb_friend, R.drawable.fb_self };

	private SingleIconRadioGroup sendRadioGroup = new SingleIconRadioGroup(App.getContext(), choices, icons, 0,
			ClickLogId.FACEBOOK_PRIVACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook);

		Bundle data = this.getIntent().getExtras();
		image_week = data.getInt("image_week", 0);
		image_score = data.getInt("image_score", 0);

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		wordTypeface = Typefaces.getWordTypeface();
		wordTypefaceBold = Typefaces.getWordTypefaceBold();

		bgLayout = (RelativeLayout) this.findViewById(R.id.fb_main_layout);
		titleText = (TextView) this.findViewById(R.id.fb_title);
		image = (ImageView) this.findViewById(R.id.fb_input_image);
		inputLayout = (LinearLayout) this.findViewById(R.id.fb_input_layout);
		loginLayout = (RelativeLayout) this.findViewById(R.id.fb_login_layout);
		inputScrollview = (ScrollView) this.findViewById(R.id.facebook_scrollview);
		loginText = (TextView) this.findViewById(R.id.fb_login_message);

		titleText.setTypeface(wordTypefaceBold);
		loginText.setTypeface(wordTypeface);

		authButton = (LoginButton) this.findViewById(R.id.authButton);
		authButton.setReadPermissions(Arrays.asList("basic_info", "read_friendlists"));
		authButton.setTypeface(wordTypefaceBold);
		authButton.setTextColor(App.getContext().getResources().getColor(R.color.lite_orange));
		authButton.setBackgroundResource(R.drawable.transparent_bg);

		state_bmp = BitmapGenerator.generateBitmap(image_week, image_score);

		image.setImageBitmap(state_bmp);

		inputMessage = createEditView();
		inputLayout.addView(inputMessage);

		View privacyTextView = BarButtonGenerator.createTextView(R.string.fb_privacy);
		inputLayout.addView(privacyTextView);
		LinearLayout.LayoutParams privacyParam = (LinearLayout.LayoutParams) privacyTextView.getLayoutParams();
		privacyParam.topMargin = (int) App.getContext().getResources().getDimension(R.dimen.fb_gap);

		privacySelection = sendRadioGroup.getView();// createSendGroupView();
		inputLayout.addView(privacySelection);

		shareButton = BarButtonGenerator.createIconView(R.string.fb_share, 0, new SendOnClickListener());

		inputLayout.addView(shareButton);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		Session.openActiveSession(this, true, callback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ClickLog.Log(ClickLogId.FACEBOOK_ENTER);
		enablePage(true);
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		ClickLog.Log(ClickLogId.FACEBOOK_LEAVE);
		if (checkLayout != null && checkLayout.getParent() != null)
			bgLayout.removeView(checkLayout);
		enablePage(true);
		uiHelper.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (state_bmp != null && !state_bmp.isRecycled()) {
			state_bmp.recycle();
		}
		uiHelper.onDestroy();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		uiHelper.onStop();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		Log.d(TAG, state.name() + " " + state.toString());
		if (state.isOpened()) {
			inputScrollview.setVisibility(View.VISIBLE);
			loginLayout.setVisibility(View.INVISIBLE);
		} else if (state.isClosed()) {
			inputScrollview.setVisibility(View.INVISIBLE);
			loginLayout.setVisibility(View.VISIBLE);
		} else {
			inputScrollview.setVisibility(View.INVISIBLE);
			loginLayout.setVisibility(View.VISIBLE);
		}
	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private void publishStory() {
		Session session = Session.getActiveSession();

		if (session != null) {
			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this,
						PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			LoadingDialogControl.show(this, 1);

			Request.Callback callback = new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					boolean result = false;
					if (response != null) {
						GraphObject gobj = response.getGraphObject();
						if (gobj == null) {
							Log.d(TAG, "upload failed");
							CustomToastSmall.generateToast(R.string.fb_fail_toast);
						} else {
							JSONObject graphResponse = gobj.getInnerJSONObject();
							try {
								graphResponse.getString("id");
							} catch (Exception e) {
								Log.i(TAG, "upload exception" + e.getMessage());
							}
							FacebookRequestError error = response.getError();
							if (error == null) {
								result = true;
							}
						}
					}

					String text_msg = null;
					if (PreferenceControl.uploadFacebookInfo()) {
						if (texts != null && texts.getText() != null)
							text_msg = texts.getText().toString();
						else
							text_msg = "";
					}
					FacebookInfo info;

					DatabaseControl db = new DatabaseControl();

					if (result) {
						Log.d(TAG, "upload success");
						info = new FacebookInfo(System.currentTimeMillis(), image_week, image_score, text_msg, false,
								true, sendRadioGroup.getResult(), 0);
						int addScore = db.insertFacebookInfo(info);
						if (PreferenceControl.checkCouponChange())
							PreferenceControl.setCouponChange(true);
						CustomToast.generateToast(R.string.fb_success_toast, addScore);

					} else {
						Log.d(TAG, "upload failed");
						info = new FacebookInfo(System.currentTimeMillis(), image_week, image_score, text_msg, false,
								false, sendRadioGroup.getResult(), 0);
						db.insertFacebookInfo(info);
						CustomToastSmall.generateToast(R.string.fb_fail_toast);
					}
					LoadingDialogControl.dismiss();
					if (result)
						finish();
				}
			};

			Request request = Request.newUploadPhotoRequest(session, state_bmp, callback);
			Bundle params = request.getParameters();
			if (texts != null && texts.getText().length() > 0)
				params.putString("name", texts.getText() + "\n" + getString(R.string.app_name) + " "
						+ getString(R.string.homepage));
			else
				params.putString("name", getString(R.string.app_name) + " " + getString(R.string.homepage));

			JSONObject privacy = new JSONObject();
			try {
				switch (sendRadioGroup.getResult()) {
				case 0:
					privacy.put("value", "ALL_FRIENDS");
					break;
				case 1:
					privacy.put("value", "SELF");
					break;
				}
			} catch (JSONException e) {
			}

			params.putString("privacy", privacy.toString());

			request.setParameters(params);
			request.executeAsync();

		}

	}

	private View createEditView() {

		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.bar_large_edit_item, null);
		texts = (EditText) layout.findViewById(R.id.question_edit);
		texts.setTypeface(wordTypefaceBold);
		return layout;
	}

	@SuppressLint("InlinedApi")
	private class SendOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			enablePage(false);

			ClickLog.Log(ClickLogId.FACEBOOK_SUBMIT);
			if (checkLayout == null) {
				checkLayout = (RelativeLayout) inflater.inflate(R.layout.dialog_facebook_check, null);
				TextView fbOK = (TextView) checkLayout.findViewById(R.id.fb_ok_button);
				TextView fbCancel = (TextView) checkLayout.findViewById(R.id.fb_cancel_button);
				TextView fbHelp = (TextView) checkLayout.findViewById(R.id.fb_help);
				fbHelp.setTypeface(wordTypefaceBold);
				fbOK.setTypeface(wordTypefaceBold);
				fbCancel.setTypeface(wordTypefaceBold);

				fbOK.setOnClickListener(new CallOnClickListener());
				fbCancel.setOnClickListener(new CallCancelOnClickListener());
			}

			bgLayout.addView(checkLayout);
			RelativeLayout.LayoutParams boxParam = (RelativeLayout.LayoutParams) checkLayout.getLayoutParams();
			boxParam.width = LayoutParams.MATCH_PARENT;
			boxParam.height = LayoutParams.MATCH_PARENT;
			boxParam.addRule(RelativeLayout.CENTER_IN_PARENT);

		}
	}

	private class CallOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ClickLog.Log(ClickLogId.FACEBOOK_SUBMIT_OK);
			publishStory();
			bgLayout.removeView(checkLayout);
			enablePage(true);
		}
	}

	private class CallCancelOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ClickLog.Log(ClickLogId.FACEBOOK_SUBMIT_CANCEL);
			bgLayout.removeView(checkLayout);
			enablePage(true);
		}
	}

	private void enablePage(boolean enable) {
		authButton.setEnabled(enable);
		shareButton.setEnabled(enable);
		texts.setEnabled(enable);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ClickLog.Log(ClickLogId.FACEBOOK_RETURN);
			if (checkLayout != null && checkLayout.getParent() != null) {
				bgLayout.removeView(checkLayout);
				enablePage(true);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
