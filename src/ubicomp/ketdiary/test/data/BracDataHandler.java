package ubicomp.ketdiary.test.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.file.MainStorage;
import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.storytelling.ui.StorytellingGraphics;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.content.Context;
import android.util.Log;

/**
 * Handle the BrAC detection data
 * 
 * @author Stanley Wang
 */
public class BracDataHandler {

	private static final String TAG = "BrAC_DATA_HANDLER";

	protected long ts;
	protected Context context;
	protected double sensorResult = 0;
	protected DatabaseControl db;

	public static final int NOTHING = 0;
	public static final int ERROR = -1;
	public static final int SUCCESS = 1;

	/**
	 * Constructor
	 * 
	 * @param timestamp
	 *            timestamp of the detection
	 */
	public BracDataHandler(long timestamp) {
		ts = timestamp;
		this.context = App.getContext();
		db = new DatabaseControl();
	}

	/** start to handle the detection data */
	public void start() {

		File mainStorageDir = MainStorage.getMainStorageDirectory();
		File textFile, questionFile;

		textFile = new File(mainStorageDir.getPath() + File.separator + ts + File.separator + ts + ".txt");
		questionFile = new File(mainStorageDir.getPath() + File.separator + ts + File.separator + "question.txt");
		sensorResult = parseTextFile(textFile);

		int q_result = getQuestionResult(questionFile);
		int emotion = q_result / 100;
		int craving = q_result % 100;
		if (q_result == -1) {
			emotion = -1;
			craving = -1;
		}

		float brac = (float) sensorResult;
		long timestamp = ts;

		Detection detection = new Detection(brac, timestamp, emotion, craving, false, 0, 0);

		boolean update = false;
		if (timestamp == PreferenceControl.getUpdateDetectionTimestamp())
			update = true;
		PreferenceControl.setUpdateDetection(false);
		PreferenceControl.setUpdateDetectionTimestamp(0);

		int addScore = db.insertDetection(detection, update);
		if (addScore == 0 && !detection.isPass()) // TestFail & get no credit
			CustomToast.generateToast(R.string.after_test_fail, -1);
		else if (!detection.isPass())
			CustomToast.generateToast(R.string.after_test_fail, addScore);
		else
			CustomToast.generateToast(R.string.after_test_pass, addScore);

		int prevShowWeek = PreferenceControl.getPrevShowWeek();
		int prevShowWeekState = PreferenceControl.getPrevShowWeekState();
		Detection curDetection = db.getLatestDetection();
		int curState = StorytellingGraphics.getPageIdx(curDetection.getWeeklyScore(), curDetection.getTv().getWeek());

		if (prevShowWeek < curDetection.getTv().getWeek())
			prevShowWeekState = 0;
		boolean pageChange = (prevShowWeekState < curState);
		PreferenceControl.setPageChange(pageChange);

		if (sensorResult < Detection.BRAC_THRESHOLD)
			if (emotion <= 2 || craving >= 4)
				PreferenceControl.setTestResult(1);
			else
				PreferenceControl.setTestResult(0);
		else if (sensorResult < Detection.BRAC_THRESHOLD_HIGH)
			PreferenceControl.setTestResult(2);
		else
			PreferenceControl.setTestResult(3);

	}

	/**
	 * get detection result
	 * 
	 * @return BrAC value reading from the sensor
	 */
	public double getResult() {
		return sensorResult;
	}

	/**
	 * Parse the detection text file
	 * 
	 * @param textFile
	 *            file contains all the detection value
	 * @return BrAC value
	 */
	protected double parseTextFile(File textFile) {
		double median = 0;
		try {
			@SuppressWarnings("resource")
			Scanner s = new Scanner(textFile);
			int index = 0;
			List<Double> valueArray2 = new ArrayList<Double>();

			while (s.hasNext()) {
				index++;
				String word = s.next();
				if (index % 2 == 0) {
					valueArray2.add(Double.valueOf(word));
				}
			}
			if (valueArray2.size() == 0)
				return ERROR;
			Double[] values = valueArray2.toArray(new Double[valueArray2.size()]);
			Arrays.sort(values);
			median = values[(values.length - 1) / 2];

		} catch (FileNotFoundException e1) {
			Log.d(TAG, "FILE NOT FOUND");
			return ERROR;
		}
		return median;
	}

	/**
	 * Parse the questionnaire file
	 * 
	 * @param textFile
	 *            file contains all the questionnaire result
	 * @return emotion * 100 + craving
	 */
	protected int getQuestionResult(File textFile) {
		int result = -1;
		try {
			@SuppressWarnings("resource")
			Scanner s = new Scanner(textFile);

			int emotion = 0;
			int craving = 0;

			if (s.hasNextInt())
				emotion = s.nextInt();
			if (s.hasNextInt())
				craving = s.nextInt();

			if (emotion == -1 || craving == -1)
				return -1;
			result = emotion * 100 + craving;

		} catch (FileNotFoundException e1) {
			return ERROR;
		}
		return result;
	}
}
