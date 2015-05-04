package ubicomp.ketdiary.system.gcm;

import ubicomp.ketdiary.main.GCMIntentService;
import ubicomp.ketdiary.system.check.DefaultCheck;
import ubicomp.ketdiary.system.check.LockCheck;
import ubicomp.ketdiary.system.config.Config;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;

import com.google.android.gcm.GCMRegistrar;

/**
 * Control the GCM related utilities
 * 
 * @author Stanley Wang
 */
public class GCMUtilities {

	private static AsyncTask<Void, Void, Void> registerTask = null;

	/**
	 * Execute when onCreate
	 * 
	 * @param context
	 *            Activity context
	 */
	public static void onCreate(final Context context) {

		if (DefaultCheck.check() || LockCheck.check())
			return;

		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		context.registerReceiver(GCMReceiver, new IntentFilter("GCM_RECEIVE_ACTION"));
		final String regId = GCMRegistrar.getRegistrationId(context);
		if (regId.equals("")) {
			GCMRegistrar.register(context, Config.SENDER_ID);
		} else {
			if (!GCMRegistrar.isRegisteredOnServer(context)) {
				if (registerTask == null) {
					registerTask = new AsyncTask<Void, Void, Void>() {
						@Override
						protected Void doInBackground(Void... params) {
							if (!GCMServerUtilities.register(context, regId))
								GCMRegistrar.unregister(context);
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							registerTask = null;
						}

						@Override
						protected void onCancelled() {
							registerTask = null;
						}
					};
					if (Build.VERSION.SDK_INT >= 11)
						registerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
					else
						registerTask.execute();
				}
			}
		}
	}

	/**
	 * Execute when onDestroy
	 * 
	 * @param context
	 *            Activity context
	 */
	public static void onDestroy(Context context) {
		if (registerTask != null) {
			registerTask.cancel(true);
		}

		if (GCMRegistrar.isRegistered(context))
			GCMRegistrar.unregister(context);
		if (GCMRegistrar.isRegisteredOnServer(context))
			GCMRegistrar.setRegisteredOnServer(context, false);
		try {
			context.unregisterReceiver(GCMReceiver);
		} catch (IllegalArgumentException e) {
		}
		GCMRegistrar.onDestroy(context);
	}

	/** BroadcastReceiver for receiving the GCM */
	private final static BroadcastReceiver GCMReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent sIntent = new Intent(context, GCMIntentService.class);
			context.startService(sIntent);
		}
	};
}
