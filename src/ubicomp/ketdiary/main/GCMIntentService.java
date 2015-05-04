package ubicomp.ketdiary.main;

import static ubicomp.ketdiary.system.config.Config.SENDER_ID;
import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.GCMRead;
import ubicomp.ketdiary.system.gcm.GCMNotificationControl;
import ubicomp.ketdiary.system.gcm.GCMServerUtilities;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * Service for handling GCM service register and unregister (unregister is not
 * activated )
 * 
 * @author Stanley Wang
 */
public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		GCMServerUtilities.register(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		if (GCMRegistrar.isRegisteredOnServer(context)) {
			GCMServerUtilities.unregister(context, registrationId);
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		String message = intent.getExtras().getString("gcm_message");
		generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
	}

	private static void generateNotification(Context context, String message) {
		String msgText = message;

		DatabaseControl db = new DatabaseControl();
		long ts = System.currentTimeMillis();
		db.insertGCMRead(new GCMRead(ts, 0, msgText, false));

		GCMNotificationControl.generate(context);

	}

}