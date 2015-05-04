package ubicomp.ketdiary.system.gcm;

import android.content.Context;

import java.util.Random;

import ubicomp.ketdiary.system.check.DefaultCheck;
import ubicomp.ketdiary.system.check.LockCheck;

/**
 * Control the register and unregister of gcm id to the server
 * 
 * @author Stanley Wang
 */
public final class GCMServerUtilities {

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	/**
	 * Register
	 * 
	 * @param context
	 *            Activity or Service context
	 * @param regId
	 *            GCM id registered from Google
	 */
	public static boolean register(final Context context, final String regId) {
		if (DefaultCheck.check() || LockCheck.check())
			return false;

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			if (GCMRegisterUtilities.register(context, regId))
				return true;
			else {
				try {
					Thread.sleep(backoff);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return false;
				}
				backoff <<= 1;
			}
		}
		return false;
	}

	/**
	 * Unregister (Do nothing in this case)
	 * 
	 * @param context
	 *            Activity or Service context
	 * @param regId
	 *            GCM id registered from Google
	 */
	public static void unregister(final Context context, final String regId) {
		if (DefaultCheck.check() || LockCheck.check())
			return;
	}
}