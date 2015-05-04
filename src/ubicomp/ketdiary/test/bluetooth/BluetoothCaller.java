package ubicomp.ketdiary.test.bluetooth;

import ubicomp.ketdiary.test.Tester;

public interface BluetoothCaller extends Tester {
	public void stopDueToInit();

	public void failBT();

	public void setPairMessage();
}
