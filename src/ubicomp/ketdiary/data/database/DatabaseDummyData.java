package ubicomp.ketdiary.data.database;

import java.util.Calendar;
import java.util.Random;

import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.main.PreSettingActivity;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * This class is an AsyncTask for generating dummy BrAC test results
 * 
 * @author Stanley Wang
 */
public class DatabaseDummyData extends AsyncTask<Void, Void, Void> {

	private Context context;
	private ProgressDialog dialog = null;

	private static final long MORNING_TIMESTAMP = 6 * 60 * 60 * 1000;
	private static final long AFTERNOON_TIMESTAMP = 14 * 60 * 60 * 1000;
	private static final long NIGHT_TIMESTAMP = 21 * 60 * 60 * 1000;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Context of the Activity
	 */
	public DatabaseDummyData(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setMessage("Please Wait...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		insertDummyData();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (dialog != null)
			dialog.dismiss();
		Intent intent = new Intent(context, PreSettingActivity.class);
		context.startActivity(intent);
	}

	/** This method implements the generate algorithm of the dummy data */
	private void insertDummyData() {
		DatabaseRestoreControl drc = new DatabaseRestoreControl();
		drc.deleteAll();

		DatabaseControl db = new DatabaseControl();

		Calendar startDate = PreferenceControl.getStartDate();
		Calendar curDate = Calendar.getInstance();

		Random rand = new Random();

		while (startDate.before(curDate)) {
			int rand_num = rand.nextInt(100);
			long ts = startDate.getTimeInMillis();

			if (rand_num < 50) {
				Detection morning = new Detection(0.f, ts + MORNING_TIMESTAMP, 3 + rand.nextInt(2), rand.nextInt(2),
						true, 0, 0);
				Detection afternoon = new Detection(0.f, ts + AFTERNOON_TIMESTAMP, 3 + rand.nextInt(2),
						rand.nextInt(2), true, 0, 0);
				Detection night = new Detection(0.f, ts + NIGHT_TIMESTAMP, 3 + rand.nextInt(2), rand.nextInt(2), true,
						0, 0);
				db.insertDetection(morning, false);
				db.setDetectionUploaded(morning.getTv().getTimestamp());
				db.insertDetection(afternoon, false);
				db.setDetectionUploaded(afternoon.getTv().getTimestamp());
				db.insertDetection(night, false);
				db.setDetectionUploaded(night.getTv().getTimestamp());
			} else if (rand_num < 70) {
				Detection morning = new Detection(0.f, ts + MORNING_TIMESTAMP, 2 + rand.nextInt(3), rand.nextInt(3),
						true, 0, 0);
				Detection afternoon = new Detection(0.f, ts + AFTERNOON_TIMESTAMP, 2 + rand.nextInt(3),
						rand.nextInt(3), true, 0, 0);
				Detection night = new Detection(0.f, ts + NIGHT_TIMESTAMP, 2 + rand.nextInt(3), rand.nextInt(3), true,
						0, 0);
				db.insertDetection(morning, false);
				db.setDetectionUploaded(morning.getTv().getTimestamp());
				db.insertDetection(afternoon, false);
				db.setDetectionUploaded(afternoon.getTv().getTimestamp());
				db.insertDetection(night, false);
				db.setDetectionUploaded(night.getTv().getTimestamp());
			} else if (rand_num < 80) {
				float brac = 0.06f + rand.nextFloat() * 0.5f;
				Detection morning = new Detection(0.f, ts + MORNING_TIMESTAMP, 2 + rand.nextInt(2),
						2 + rand.nextInt(3), true, 0, 0);
				Detection afternoon = new Detection(0.f, ts + AFTERNOON_TIMESTAMP, 2 + rand.nextInt(2),
						2 + rand.nextInt(3), true, 0, 0);
				Detection night = new Detection(0.f, ts + NIGHT_TIMESTAMP, 2 + rand.nextInt(2), 2 + rand.nextInt(3),
						true, 0, 0);
				int rand_timeslot = rand.nextInt(3);
				if (rand_timeslot == 0)
					morning = new Detection(brac, ts + MORNING_TIMESTAMP, rand.nextInt(2), 5 + rand.nextInt(3), true,
							0, 0);
				else if (rand_timeslot == 1)
					afternoon = new Detection(brac, ts + AFTERNOON_TIMESTAMP, rand.nextInt(2), 5 + rand.nextInt(3),
							true, 0, 0);
				else
					night = new Detection(brac, ts + NIGHT_TIMESTAMP, rand.nextInt(2), 5 + rand.nextInt(3), true, 0, 0);

				db.insertDetection(morning, false);
				db.setDetectionUploaded(morning.getTv().getTimestamp());
				db.insertDetection(afternoon, false);
				db.setDetectionUploaded(afternoon.getTv().getTimestamp());
				db.insertDetection(night, false);
				db.setDetectionUploaded(night.getTv().getTimestamp());
			} else if (rand_num < 90) {
				float brac = 0.06f + rand.nextFloat() * 0.5f;
				Detection morning = new Detection(brac, ts + MORNING_TIMESTAMP, rand.nextInt(2), 5 + rand.nextInt(3),
						true, 0, 0);
				brac = 0.06f + rand.nextFloat() * 0.5f;
				Detection afternoon = new Detection(brac, ts + AFTERNOON_TIMESTAMP, rand.nextInt(2),
						5 + rand.nextInt(3), true, 0, 0);
				brac = 0.06f + rand.nextFloat() * 0.5f;
				Detection night = new Detection(brac, ts + NIGHT_TIMESTAMP, rand.nextInt(2), 5 + rand.nextInt(3), true,
						0, 0);
				int rand_timeslot = rand.nextInt(3);
				if (rand_timeslot != 0) {
					db.insertDetection(morning, false);
					db.setDetectionUploaded(morning.getTv().getTimestamp());
				} else if (rand_timeslot != 1) {
					db.insertDetection(afternoon, false);
					db.setDetectionUploaded(afternoon.getTv().getTimestamp());
				} else if (rand_timeslot != 2) {
					db.insertDetection(night, false);
					db.setDetectionUploaded(night.getTv().getTimestamp());
				}
			} else {
				Detection morning = new Detection(0.f, ts + MORNING_TIMESTAMP, 3 + rand.nextInt(2), rand.nextInt(2),
						true, 0, 0);
				Detection afternoon = new Detection(0.f, ts + AFTERNOON_TIMESTAMP, 3 + rand.nextInt(2),
						rand.nextInt(2), true, 0, 0);
				Detection night = new Detection(0.f, ts + NIGHT_TIMESTAMP, 3 + rand.nextInt(2), rand.nextInt(2), true,
						0, 0);
				int rand_timeslot = rand.nextInt(3);

				if (rand_timeslot != 0) {
					db.insertDetection(morning, false);
					db.setDetectionUploaded(morning.getTv().getTimestamp());
				} else if (rand_timeslot != 1) {
					db.insertDetection(afternoon, false);
					db.setDetectionUploaded(afternoon.getTv().getTimestamp());
				} else if (rand_timeslot != 2) {
					db.insertDetection(night, false);
					db.setDetectionUploaded(night.getTv().getTimestamp());
				}
			}
			startDate.add(Calendar.DATE, 1);
		}

	}

}
