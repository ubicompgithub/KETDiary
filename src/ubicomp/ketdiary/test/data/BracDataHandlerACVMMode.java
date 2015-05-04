package ubicomp.ketdiary.test.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.system.config.PreferenceControl;

/**
 * BrAC data handler for debug ACVM mode
 * 
 * @author Stanley Wang
 */
public class BracDataHandlerACVMMode extends BracDataHandler {

	/**
	 * Constructor
	 * 
	 * @param timestamp
	 *            timestamp of the detection
	 */
	public BracDataHandlerACVMMode(long timestamp) {
		super(timestamp);
	}

	@Override
	/** start to handle the detection data */
	public void start() {

		PreferenceControl.setUpdateDetection(false);
		PreferenceControl.setUpdateDetectionTimestamp(0);
		CustomToast.generateToast(R.string.after_test_pass, 0);
		PreferenceControl.setDebugDetectionTimestamp(ts);
	}

	@Override
	protected double parseTextFile(File textFile) {
		double avg = 0;
		try {
			@SuppressWarnings("resource")
			Scanner s = new Scanner(textFile);
			int index = 0;
			List<String> valueArray_A0 = new ArrayList<String>();
			List<String> valueArray_A1 = new ArrayList<String>();
			while (s.hasNext()) {
				index++;
				String word = s.next();
				if (index % 5 == 3)
					valueArray_A0.add(word);
				else if (index % 5 == 4)
					valueArray_A1.add(word);
			}

			int len = valueArray_A0.size();
			int len2 = valueArray_A1.size();
			if (len2 < len)
				len = len2;
			for (int i = 0; i < len; ++i) {
				avg += Double.parseDouble(valueArray_A1.get(i)) - Double.parseDouble(valueArray_A0.get(i));
			}
			if (len == 0)
				return ERROR;
			avg /= len;

		} catch (FileNotFoundException e1) {
			return ERROR;
		}
		return avg;
	}

}
