package ubicomp.ketdiary.test.bluetooth;

import ubicomp.ketdiary.test.Tester;
import android.os.Handler;
import android.os.Message;

/**
 * Handler of initializing Bluetooth functions
 * 
 * @author Stanley Wang
 */
public class BluetoothInitHandler extends Handler {
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
	public BluetoothInitHandler(BluetoothCaller btCaller, Bluetooth bt) {
		this.btCaller = btCaller;
		this.bt = bt;
	}

	@Override
	public void handleMessage(Message msg) {
		bt.enableAdapter();
		if (bt.pair()) {
			if (bt.connect())
				btCaller.updateInitState(Tester._BT);
			else {
				btCaller.stopDueToInit();
				btCaller.failBT();
			}
		} else {
			btCaller.setPairMessage();
		}
	}
}
