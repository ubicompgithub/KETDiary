package ubicomp.ketdiary.test.gps;

/** Interface defines GPS related functions */
public interface GPSInterface {
	/** Start to run GPS */
	public void runGPS();

	/** Call GPS Setting Activity in Android */
	public void callGPSActivity();

	/**
	 * Initialize GPS services
	 * 
	 * @param enable
	 *            true if activates the service
	 */
	public void initializeGPS(boolean enable);
}
