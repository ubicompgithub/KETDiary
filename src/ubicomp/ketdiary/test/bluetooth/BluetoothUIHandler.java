package ubicomp.ketdiary.test.bluetooth;

import android.os.Handler;
import android.os.Message;

/**
 * Handler controls the Bluetooth UI
 * 
 * @author Stanley Wang
 */
public class BluetoothUIHandler extends Handler {

	private BluetoothMessageUpdater updater;

	/**
	 * Constructor
	 * 
	 * @param updater
	 *            BluetoothMessageUpdater
	 * @see BluetoothMessageUpdater
	 */
	public BluetoothUIHandler(BluetoothMessageUpdater updater) {
		this.updater = updater;
	}

	@Override
	public void handleMessage(Message msg) {
		if (msg.what == 2) {
			int time = msg.getData().getInt("TIME");
			updater.changeBluetoothCircle(time);
		} else if (msg.what == 3) {
			float value = msg.getData().getFloat("value");
			updater.changeBluetoothValue(value);
		}
	}

}
