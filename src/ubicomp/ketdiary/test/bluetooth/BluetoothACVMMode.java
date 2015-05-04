package ubicomp.ketdiary.test.bluetooth;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import ubicomp.ketdiary.system.config.PreferenceControl;
import ubicomp.ketdiary.test.camera.CameraRunHandler;
import ubicomp.ketdiary.test.data.BracValueDebugHandler;
import ubicomp.ketdiary.test.data.BracValueFileHandler;

/**
 * Control Bluetooth related functions for training
 * 
 * @author Stanley Wang
 */
public class BluetoothACVMMode extends Bluetooth {

	protected BracValueDebugHandler bracDebugHandler;

	protected static float PRESSURE_DIFF_MIN_RANGE_OLD = 50f;
	protected static float PRESSURE_DIFF_MIN_OLD = 100.f;
	protected static float PRESSURE_DIFF_MIN_RANGE_NEW = 30.f;
	protected static float PRESSURE_DIFF_MIN_NEW = 20.f;
	protected final static long MAX_TEST_TIME = 50000;
	private long showBrACTime = 0;

	private static int READ_A0 = 10;
	private static int READ_A1 = 11;

	private int showPressureCount = 0;
	private static final int SHOW_PRESSURE_COUNT_MOD = 5;
	
	/**
	 * Constructor
	 * 
	 * @param debugger
	 *            BluetoothDebugger
	 * @param updater
	 *            BluetoothMessageUpdater
	 * @param cameraRunHandler
	 *            CameraRunHandler
	 * @param bracFileHandler
	 *            BracValueFileHandler
	 * @param recordDetail
	 *            record detail information of BrAC detection
	 * @param bracDebugHandler
	 *            BracValueDebugHandler
	 * @see BluetoothDebugger
	 * @see BluetoothMessageUpdater
	 * @see CameraRunHandler
	 * @see BracValueFileHandler
	 * @see BracValueDebugHandler
	 */
	public BluetoothACVMMode(BluetoothDebugger debugger, BluetoothMessageUpdater updater,
			CameraRunHandler cameraRunHandler, BracValueFileHandler bracFileHandler,
			BracValueDebugHandler bracDebugHandler) {
		super(debugger, updater, cameraRunHandler, bracFileHandler, true);
		this.bracDebugHandler = bracDebugHandler;
	}

	@Override
	protected void setSensorPressureLimit() {
		String sensorId = PreferenceControl.getSensorID();
		if (sensorId.startsWith(DEVICE_NAME_FORMAL_OLD)) {
			PRESSURE_DIFF_MIN_RANGE = PRESSURE_DIFF_MIN_RANGE_OLD;
			PRESSURE_DIFF_MIN = PRESSURE_DIFF_MIN_OLD;
		} else {
			PRESSURE_DIFF_MIN_RANGE = PRESSURE_DIFF_MIN_RANGE_NEW;
			PRESSURE_DIFF_MIN = PRESSURE_DIFF_MIN_NEW;
		}
	}

	@Override
	public void start() {
		debugger.showDebug("bluetooth start the test");
		start = true;
	}

	@Override
	public void read() {

		boolean end = false;
		byte[] temp = new byte[READ_BUFFER_SIZE];
		int bytes = 0;
		String msg = "";
		isPeak = false;
		pressureMin = MAX_PRESSURE;
		pressureCurrent = 0;
		int read_type = READ_NULL;
		totalDuration = 0;
		tempDuration = 0;
		firstStartTime = -1;
		imageCount = 0;
		showValue = temp_A0 = temp_A1 = 0.f;
		startToRecord = false;
		showBrACTime = 0;
		showPressureCount = 0;
		try {
			in = socket.getInputStream();
			debugger.showDebug("bluetooth start to read");
			if (in.available() > 0)
				bytes = in.read(temp);

			while (bytes >= 0) {
				long time = System.currentTimeMillis();
				long time_gap = time - firstStartTime;
				if (firstStartTime == -1)
					firstStartTime = time;
				else if (time_gap > MAX_TEST_TIME)
					throw new Exception(EXCEPTION_TIME_OUT);

				for (int i = 0; i < bytes; ++i) {
					if ((char) temp[i] == 'a') {
						end = parseMsg(msg);
						sendDebugMsg(msg);
						msg = "a";
						read_type = READ_A0;
					} else if ((char) temp[i] == 'c') {
						end = parseMsg(msg);
						sendDebugMsg(msg);
						msg = "c";
						read_type = READ_A1;
					} else if ((char) temp[i] == 'm') {
						end = parseMsg(msg);
						sendDebugMsg(msg);
						msg = "m";
						read_type = READ_PRESSURE;
					} else if ((char) temp[i] == 'v') {
						end = parseMsg(msg);
						sendDebugMsg(msg);
						msg = "v";
						read_type = READ_VOLTAGE;
					} else if ((char) temp[i] == 's') {
						end = parseMsg(msg);
						read_type = READ_NULL;
					} else if ((char) temp[i] == 'b') {
						throw new Exception(EXCEPTION_NO_BATTERY);
					} else if ((char) temp[i] == 'p') {
						throw new Exception(EXCEPTION_PRESSURE_ERROR);
					} else if (read_type != READ_NULL) {
						msg += (char) temp[i];
					}
				}
				if (end)
					break;

				if (in.available() > 0) {
					bytes = in.read(temp);
					sleepTime /= 2;
					sleepTime = Math.max(MIN_SLEEP_TIME, sleepTime);
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(sleepTime);
							} catch (InterruptedException e) {
							}
						}
					});
					t.start();
					t.join();
				} else {
					bytes = 0;
					if (disconnectionMillis > MAX_ZERO_DURATION)
						throw new Exception(EXCEPTION_DISCONNECTION);
					sleepTime *= 2;
					sleepTime = Math.min(MAX_SLEEP_TIME, sleepTime);

					int try_time = 0;
					while (in.available() <= 0 && try_time < sleepTime) {
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(5);
								} catch (InterruptedException e) {
								}
							}
						});
						t.start();
						t.join();
						try_time += 5;
					}
					disconnectionMillis += try_time;
				}

			}
			closeSuccess();
		} catch (Exception e) {
			closeSuccess();
			handleException(e);
		}
	}

	@Override
	protected void handleException(Exception e) {
		Log.e(TAG, "FAIL TO READ DATA FROM THE SENSOR: " + e.toString());
		if (e.getMessage() != null && e.getMessage().equals(EXCEPTION_TIME_OUT)) {
			debugger.showDebug("Close by timeout");
			cameraRunHandler.sendEmptyMessage(3);
		} else if (e.getMessage() != null && e.getMessage().equals(EXCEPTION_BLOW_TWICE)) {
			debugger.showDebug("Close by blowing twice");
			cameraRunHandler.sendEmptyMessage(4);
			debugger.showDebug("Close by bad connection");
		} else if (e.getMessage() != null && e.getMessage().equals(EXCEPTION_DISCONNECTION)) {
			cameraRunHandler.sendEmptyMessage(5);
		} else if (e.getMessage() != null && e.getMessage().equals(EXCEPTION_PRESSURE_ERROR)) {
			debugger.showDebug("Close by pressure sensor corrupt");
			cameraRunHandler.sendEmptyMessage(6);
		} else {
			debugger.showDebug("Close by exception");
			cameraRunHandler.sendEmptyMessage(2);
		}
	}

	@Override
	public void closeSuccess() {
		close();
		if (bracDebugHandler != null)
			bracDebugHandler.close();
	}

	protected String debugMsg = "";

	protected StringBuilder debugMsgBuilder = new StringBuilder();

	protected void sendDebugMsg(String msg) {
		if (msg == "")
			return;
		if (msg.charAt(0) == 'm') { // pressure
			debugMsgBuilder.append(',');
			debugMsgBuilder.append(msg.substring(1, msg.length() - 1));
			debugMsgBuilder.append("\n");
		} else if (msg.charAt(0) == 'a') { // alcohol voltage a0
			long timestamp = System.currentTimeMillis();
			debugMsgBuilder.append(timestamp);
			debugMsgBuilder.append(',');
			debugMsgBuilder.append(msg.substring(1, msg.length() - 1));
			return;
		} else if (msg.charAt(0) == 'c') { // alcohol voltage a1
			debugMsgBuilder.append(',');
			debugMsgBuilder.append(msg.substring(1, msg.length() - 1));
			return;
		} else if (msg.charAt(0) == 'v') { // voltage
			debugMsgBuilder.append(',');
			debugMsgBuilder.append(msg.substring(1, msg.length() - 1));
			return;
		} else
			return;

		Message message = new Message();
		Bundle data = new Bundle();
		String output = debugMsgBuilder.toString();
		data.putString("ALCOHOL_DEBUG", output);
		debugMsgBuilder = new StringBuilder();
		message.setData(data);
		bracDebugHandler.sendMessage(message);
	}

	private float temp_A0, temp_A1;
	private String temp_pressure;

	@Override
	protected boolean parseMsg(String msg) {
		synchronized (lock) {
			if (msg == "")
				;
			// Do nothing
			else if (msg.charAt(0) == 'a') {
				if (isPeak) {
					long timeStamp = System.currentTimeMillis() / 1000L;
					float alcohol = Float.valueOf(msg.substring(1));
					String output = timeStamp + "\t" + temp_pressure + "\t" + alcohol;
					// debugger.showDebug("time: " + timeStamp);
					// debugger.showDebug("a0: " + alcohol);
					if (startToRecord) {
						temp_A0 = alcohol;
						// write to the file
						writeToFile(output);
					}
				}
			} else if (msg.charAt(0) == 'c') {
				if (isPeak) {
					float alcohol = Float.valueOf(msg.substring(1));
					String output = "\t" + alcohol;
					// debugger.showDebug("a1: " + alcohol);
					if (startToRecord) {
						temp_A1 = alcohol;
						writeToFile(output);
					}
				}
			} else if (msg.charAt(0) == 'm') {
				temp_pressure = msg.substring(1, msg.length() - 1);
				pressureCurrent = Float.valueOf(temp_pressure);

				long time = System.currentTimeMillis();

				if (!start && pressureCurrent < pressureMin) {
					pressureMin = pressureCurrent;
					debugger.showDebug("absolute min setting: " + pressureMin);
				}

				if (!start) {
					// debugger.showDebug("read before start testing");
					return false;
				}

				float diff_limit = PRESSURE_DIFF_MIN_RANGE * (5000.f - tempDuration) / 5000.f + PRESSURE_DIFF_MIN;
				if (showPressureCount == 0)
					debugger.showDebug("p: " + pressureCurrent + " min: " + pressureMin + " l:" + diff_limit);
				showPressureCount = (showPressureCount + 1) % SHOW_PRESSURE_COUNT_MOD;

				if (pressureCurrent > pressureMin + diff_limit && !isPeak) {
					debugger.showDebug("Peak start");
					isPeak = true;
					startTime = time;
					tempDuration = 0;
				} else if (pressureCurrent > pressureMin + diff_limit && isPeak) {
					// debugger.showDebug("is Peak");
					endTime = time;
					totalDuration += (endTime - startTime);
					tempDuration += (endTime - startTime);
					startTime = endTime;

					showValue = temp_A1 - temp_A0;

					if (totalDuration > MILLIS_5 && updateCircleTimes < 5) {
						showBrACCircle(5);
						updateCircleTimes = 5;
					} else if (totalDuration > MILLIS_4 && updateCircleTimes < 4) {
						showBrACCircle(4);
						updateCircleTimes = 4;
					} else if (totalDuration > MILLIS_3 && updateCircleTimes < 3) {
						showBrACCircle(3);
						updateCircleTimes = 3;
					} else if (totalDuration > MILLIS_2 && updateCircleTimes < 2) {
						showBrACCircle(2);
						updateCircleTimes = 2;
					} else if (totalDuration > MILLIS_1 && updateCircleTimes < 1) {
						showBrACCircle(1);
						updateCircleTimes = 1;
					}

					if (totalDuration > showBrACTime) {
						showBrACValue(showValue);
						showBrACTime += 200;
					}

					if (totalDuration >= START_MILLIS)
						startToRecord = true;

					if (imageCount == 0 && totalDuration > IMAGE_MILLIS_0) {
						cameraRunHandler.sendEmptyMessage(0);
						++imageCount;
					} else if (imageCount == 1 && totalDuration > IMAGE_MILLIS_1) {
						cameraRunHandler.sendEmptyMessage(0);
						++imageCount;
					} else if (imageCount == 2 && totalDuration > IMAGE_MILLIS_2) {
						cameraRunHandler.sendEmptyMessage(0);
						++imageCount;
					} else if (imageCount == 3 && totalDuration > MAX_DURATION_MILLIS) {
						debugger.showDebug("test end");
						showBrACCircle(6);
						return true;
					}
				} else if (isPeak) {
					debugger.showDebug("Peak end");
					isPeak = false;
					startTime = endTime = 0;
				}
			} else if (msg.charAt(0) == 'v') {
				if (isPeak) {
					float voltage = Float.valueOf(msg.substring(1));
					String output = "\t" + voltage + "\n";
					// debugger.showDebug("v: " + voltage);
					if (startToRecord)
						writeToFile(output);
				}
			} else if (msg.charAt(0) == 's') {
				// Do nothing
			}
		}
		return false;
	}

}
