package ubicomp.ketdiary.test.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

/**
 * Bluetooth functions only for reading voltage
 * 
 * @author Stanley Wang
 */
public class SimpleBluetooth {

	private static final String TAG = "simpleBluetooth";
	private static SimpleBluetooth sb;
	private static Thread volThread = null;

	private BluetoothAdapter btAdapter;
	private BluetoothDevice sensor;
	private BluetoothSocket socket;
	private Context context;
	private static String DEVICE_NAME_FORMAL = "sober123_";
	private static String DEVICE_NAME_FORMAL_NEW = "sober456_";
	private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private InputStream in;
	private OutputStream out;
	private BluetoothDebugger debugger;

	private final static byte[] sendStartMessage = { 'y', 'y', 'y' };
	private final static byte[] sendEndMessage = { 'z', 'z', 'z' };
	private boolean connected = false;

	/**
	 * Constructor
	 * 
	 * @param debugger
	 */
	public SimpleBluetooth(BluetoothDebugger debugger) {
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		this.debugger = debugger;
	}

	private void enableAdapter() {
		Log.d(TAG, "Enable Adapter Start");
		if (!btAdapter.isEnabled()) {
			Log.d(TAG, "Enable Adapter Not Start");
			btAdapter.enable();
			int state = btAdapter.getState();
			while (state != BluetoothAdapter.STATE_ON) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				state = btAdapter.getState();
			}
		}
	}

	public boolean pair() {
		sensor = null;
		Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
		Iterator<BluetoothDevice> iter = devices.iterator();
		while (iter.hasNext()) {
			BluetoothDevice device = iter.next();
			if (device.getName() != null) {
				if (device.getName().startsWith(DEVICE_NAME_FORMAL)
						|| device.getName().startsWith(DEVICE_NAME_FORMAL_NEW)) {
					sensor = device;
					return true;
				}
			}
		}
		if (sensor == null) {
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			BroadcastReceiver receiver = new btReceiver();
			context.registerReceiver(receiver, filter);
			btAdapter.startDiscovery();
		}
		return false;
	}

	protected class btReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null)
					return;
				String name = device.getName();
				if (name == null)
					return;
				if (name.startsWith(DEVICE_NAME_FORMAL) || name.startsWith(DEVICE_NAME_FORMAL_NEW)) {
					if (btAdapter.isDiscovering())
						btAdapter.cancelDiscovery();
					sensor = device;
					connect();
					Log.d(TAG, "close by receiver");
					close();
				}
			}
		}
	}

	private boolean connect() {
		if (btAdapter != null && btAdapter.isDiscovering())
			btAdapter.cancelDiscovery();

		if (sensor == null) {
			Log.d(TAG, "close by connection");
			close();
			return false;
		}
		try {
			if (Build.VERSION.SDK_INT < 11)
				socket = sensor.createRfcommSocketToServiceRecord(uuid);
			else
				socket = sensor.createRfcommSocketToServiceRecord(uuid);
			try {
				socket.close();
				if (Build.VERSION.SDK_INT < 11)
					socket = sensor.createRfcommSocketToServiceRecord(uuid);
				else
					socket = sensor.createRfcommSocketToServiceRecord(uuid);
			} catch (Exception e) {
				Log.d(TAG, "FAIL TO CLOSE BEFORE CONNECTION");
			}
			socket.connect();
		} catch (Exception e) {
			Log.e(TAG, "FAIL TO CONNECT TO THE SENSOR: " + e.toString());
			close();
			return false;
		}
		return true;
	}

	private int READ_NULL = 0;
	private int READ_VOLTAGE = 1;

	/** Read from Bluetooth */
	public void read() {

		boolean end = false;
		byte[] temp = new byte[256];
		int bytes = 0;
		String msg = "";
		int read_type = READ_NULL;
		try {
			in = socket.getInputStream();
			if (in.available() > 0)
				bytes = in.read(temp);

			while (bytes >= 0) {

				for (int i = 0; i < bytes; ++i) {
					char read_char = (char) temp[i];
					if (read_char == 'a' || read_char == 'c' || read_char == 'm' || read_char == 's') {
						if (read_type == READ_VOLTAGE) {
							parseMsg(msg);
						}
						msg = "";
						read_type = READ_NULL;
					} else if (read_char == 'b') {
						throw new Exception("NO BATTERY");
					} else if (read_char == 'v') {
						msg = "v";
						read_type = READ_VOLTAGE;
					} else if (read_type != READ_NULL) {
						msg += (char) temp[i];
					}
				}
				if (end)
					break;

				if (in.available() > 0) {
					bytes = in.read(temp);
				} else {
					bytes = 0;
					Thread.sleep(50);
				}

			}
			Log.d(TAG, "close by read normal end");
			close();
		} catch (Exception e) {
			Log.d(TAG, "close by read exception");
			close();
		}
	}

	protected void close() {
		Log.d(TAG, "NORMAL CLOSE");

		sendEnd();

		Log.d(TAG, "SEND END DONE");
		try {
			if (in != null)
				in.close();
		} catch (Exception e) {
			Log.e(TAG, "FAIL TO CLOSE THE SENSOR INPUTSTREAM");
		}
		try {
			if (out != null)
				out.close();
		} catch (Exception e) {
			Log.e(TAG, "FAIL TO CLOSE THE SENSOR OUTPUTSTREAM");
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "FAIL TO CLOSE THE SENSOR");
		}

	}

	private void parseMsg(String msg) {
		msg = msg.substring(1);
		// Log.d(TAG,"read vol "+msg);
		debugger.showDebug(msg, 1);
	}

	/** Send start message to the sensor */
	public boolean sendStart() {
		try {
			int counter = 0;
			while (true) {
				out = socket.getOutputStream();
				in = socket.getInputStream();
				for (int i = 0; i < 5; ++i)
					out.write(sendStartMessage);
				Thread t1 = new Thread(new AckRunnable());
				Thread t2 = new Thread(new WaitRunnable());
				t1.start();
				t2.start();

				try {
					t2.join();
					if (!connected) {
						t1.join(1);
						++counter;
					} else {
						t1.join();
						break;
					}
					if (counter == 3)
						return false;
				} catch (InterruptedException e) {
					Log.d(TAG, "exception");
				}
			}
			return true;
		} catch (IOException e) {
			Log.e(TAG, "SEND START FAIL " + e.toString());
			close();
			return false;
		}
	}

	private class AckRunnable implements Runnable {
		@Override
		public void run() {
			try {
				in = socket.getInputStream();
				byte[] temp = new byte[256];
				// block for waiting for the response
				int bytes = in.read(temp);
				if (bytes > 0)
					connected = true;
			} catch (IOException e) {
			}
		}
	}

	private class WaitRunnable implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/** Send end message to the sensor */
	public void sendEnd() {
		try {
			if (out == null)
				return;
			for (int i = 0; i < 5; ++i)
				out.write(sendEndMessage);
			return;
		} catch (Exception e) {
			Log.e(TAG, "SEND END FAIL " + e.toString());
			return;
		}
	}

	public static class InitRunnable implements Runnable {

		public InitRunnable(BluetoothDebugger debugger) {
			if (sb == null)
				sb = new SimpleBluetooth(debugger);
		}

		@Override
		public void run() {
			try {
				sb.enableAdapter();
				Log.d(TAG, "step1");
				if (!sb.pair())
					sb.close();
				Log.d(TAG, "step2");
				if (!sb.connect())
					sb.close();
				Log.d(TAG, "step3");
				if (!sb.sendStart())
					sb.close();
				Log.d(TAG, "step4");
				sb.read();
			} catch (Exception e) {
				sb.close();
			}
		}
	}

	/** Show the voltage of the sensor */
	public static void showVoltage(BluetoothDebugger debugger) {
		if (volThread != null && volThread.isAlive()) {
			volThread.interrupt();
		}
		volThread = new Thread(new InitRunnable(debugger));
		volThread.start();
	}

	/** close the connection */
	public static void closeConnection() {
		if (sb != null)
			Log.d(TAG, "close by other classes");
		try {
			sb.close();
		} catch (Exception e) {
		}
		if (volThread != null && volThread.isAlive()) {
			volThread.interrupt();
		}
		volThread = null;
	}
}
