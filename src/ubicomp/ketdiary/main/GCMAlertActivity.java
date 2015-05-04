package ubicomp.ketdiary.main;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.GCMRead;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.ScaleOnTouchListener;
import ubicomp.ketdiary.main.ui.Typefaces;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Activity for showing GCM sent from the server
 * 
 * @author Stanley Wang
 */
public class GCMAlertActivity extends Activity {

	private Typeface wordTypefaceBold, wordTypeface;

	private GCMRead data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long timestamp = getIntent().getExtras().getLong("gcm_ts", 0);
		DatabaseControl db = new DatabaseControl();
		data = db.getGCMRead(timestamp);
		if (data == null) {
			finish();
			return;
		}
		String message = data.getMessage();

		wordTypefaceBold = Typefaces.getWordTypefaceBold();
		wordTypeface = Typefaces.getWordTypeface();

		Builder builder = new AlertDialog.Builder(this);

		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				finish();
			}
		});
		dialog.getWindow().setContentView(R.layout.dialog_gcm);

		TextView msg_title = (TextView) dialog.findViewById(R.id.gcm_alert_title);
		msg_title.setTypeface(wordTypefaceBold);
		TextView msg_text = (TextView) dialog.findViewById(R.id.gcm_text);
		msg_text.setText(message);
		msg_text.setTypeface(wordTypeface);
		TextView msg_ok = (TextView) dialog.findViewById(R.id.gcm_alert_ok);
		msg_ok.setTypeface(wordTypefaceBold);
		msg_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		msg_ok.setOnTouchListener(new ScaleOnTouchListener());

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (data != null) {
			DatabaseControl db = new DatabaseControl();
			db.readGCMRead(new GCMRead(data.getTv().getTimestamp(), System.currentTimeMillis(), data.getMessage(), true));
		}
		finish();
	}
}
