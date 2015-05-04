package ubicomp.ketdiary.test.bluetooth;

import ubicomp.ketdiary.test.Tester;
import android.os.AsyncTask;

/**
 * AsyncTask for reading from the device
 * 
 * @author Stanley Wang
 */
public class BluetoothReadTask extends AsyncTask<Void, Void, Void> {

	private BluetoothCaller btCaller;
	private Bluetooth bt;

	/**
	 * Constructor
	 * 
	 * @param btCaller
	 *            Caller of the Bluetooth functions
	 * @param bt
	 *            Bluetooth control class
	 */
	public BluetoothReadTask(BluetoothCaller btCaller, Bluetooth bt) {
		this.btCaller = btCaller;
		this.bt = bt;
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (bt.sendStart())
			bt.read();
		else
			bt.closeFail();
		return null;
	}

	@Override
	protected void onCancelled(Void result) {
		bt.closeSuccess();
	};

	@Override
	protected void onPostExecute(Void result) {
		btCaller.updateDoneState(Tester._BT);
	}
}
