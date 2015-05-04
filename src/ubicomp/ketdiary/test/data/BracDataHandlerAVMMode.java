package ubicomp.ketdiary.test.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.system.config.PreferenceControl;

/**
 * BrAC data handler for debug AVM mode
 * 
 * @author Stanley Wang
 */
public class BracDataHandlerAVMMode extends BracDataHandler {

	/**
	 * Constructor
	 * 
	 * @param timestamp
	 *            timestamp of the detection
	 */
	public BracDataHandlerAVMMode(long timestamp) {
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
		double median = 0;
		try {
			@SuppressWarnings("resource")
			Scanner s = new Scanner(textFile);
			int index = 0;
			List<Double> valueArray = new ArrayList<Double>();
			while (s.hasNext()) {
				index++;
				String word = s.next();
				if (index % 4 == 3)
					valueArray.add(Double.valueOf(word));
			}
			if (valueArray.size() == 0)
				return ERROR;
			Double[] values = valueArray.toArray(new Double[valueArray.size()]);
			Arrays.sort(values);
			median = values[(values.length - 1) / 2];

		} catch (FileNotFoundException e1) {
			return ERROR;
		}
		return median;
	}

}
