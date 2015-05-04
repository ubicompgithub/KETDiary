package ubicomp.ketdiary.test.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Handle the file contain breath detail condition
 * 
 * @author Stanley Wang
 */
public class BreathDetailHandler extends Handler {

	private static final String TAG = "BrAC_PRESSURE_HANDLER";
	private File file;
	private BufferedWriter writer;

	/**
	 * Constructor
	 * 
	 * @param directory
	 *            of the detection
	 */
	public BreathDetailHandler(File directory) {
		file = new File(directory, "detection_detail.txt");
	}

	@Override
	public void handleMessage(Message msg) {
		String str = msg.getData().getString("pressure");
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			Log.d(TAG, "FAIL TO OPEN");
			writer = null;
		}
		if (writer != null) {
			try {
				writer.write(str);
			} catch (IOException e) {
				Log.d(TAG, "FAIL TO WRITE");
			}
		} else {
			Log.d(TAG, "NULL TO WRITE");
		}
		close();
	}

	private void close() {
		if (writer != null) {
			try {
				writer.close();
				writer = null;
			} catch (IOException e) {
				Log.d(TAG, "FAIL TO CLOSE");
			}
		}
	}
}
