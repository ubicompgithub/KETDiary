package ubicomp.ketdiary.test.gps;

import android.location.LocationManager;
import android.os.AsyncTask;

/** AsyncTask for initializing GPS function */
public class GPSInitTask extends AsyncTask<Object, Void, Boolean> {

	private LocationManager locationManager;
	private GPSToastHandler toastHandler;
	private GPSInterface gpsInterface;

	/**
	 * Constructor
	 * 
	 * @param gpsInterface
	 *            GPSInterface
	 * @param locationManager
	 *            LocationManager
	 */
	public GPSInitTask(GPSInterface gpsInterface, LocationManager locationManager) {
		this.gpsInterface = gpsInterface;
		this.locationManager = locationManager;
		toastHandler = new GPSToastHandler();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		Boolean check = (Boolean) params[0];
		Boolean newIntent = false;
		if (check.booleanValue()) {
			boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (!network_enabled || !gps_enabled) {
				newIntent = true;
				gpsInterface.callGPSActivity();
				toastHandler.sendEmptyMessage(0);
			}
		}
		return newIntent;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		locationManager = null;
		if (!result.booleanValue()) {
			gpsInterface.runGPS();
		}
	}

}
