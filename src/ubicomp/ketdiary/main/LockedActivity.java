package ubicomp.ketdiary.main;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.Typefaces;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;

/**
 * Activity shows when the KetDiary is locked
 * 
 * @author Stanley Wang
 */
public class LockedActivity extends Activity {

	private Typeface wordTypefaceBold, wordTypeface;
	private LinearLayout nextButton;
	private TextView titleText, about, nextText;
	private TextView messageText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locked);

		wordTypefaceBold = Typefaces.getWordTypefaceBold();
		wordTypeface = Typefaces.getWordTypeface();

		titleText = (TextView) this.findViewById(R.id.lock_title);
		about = (TextView) this.findViewById(R.id.lock_about);
		messageText = (TextView) this.findViewById(R.id.lock_message);
		nextText = (TextView) this.findViewById(R.id.lock_goto_about);
		nextButton = (LinearLayout) this.findViewById(R.id.lock_about_button);

		titleText.setTypeface(wordTypefaceBold);
		about.setTypeface(wordTypefaceBold);
		messageText.setTypeface(wordTypeface);
		nextText.setTypeface(wordTypefaceBold);

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getBaseContext(), AboutActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent a_intent = new Intent(this, UploadService.class);
		this.startService(a_intent);
	}
}
