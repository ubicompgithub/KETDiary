package ubicomp.ketdiary.system.gcm;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import com.google.android.gcm.GCMRegistrar;

import ubicomp.ketdiary.system.config.PreferenceControl;
import ubicomp.ketdiary.system.uploader.HttpSecureClientGenerator;
import ubicomp.ketdiary.system.uploader.ServerUrl;
import android.content.Context;

/**
 * Register GCM id to the server
 * 
 * @author Stanley Wang
 */
public class GCMRegisterUtilities {

	/**
	 * register to the server
	 * 
	 * @param context
	 *            Activity or Service context
	 * @param regId
	 *            gcmId registered from Google
	 */
	public static boolean register(Context context, String regId) {

		String serverUrl = ServerUrl.SERVER_URL_GCM_REGISTER();
		String uid = PreferenceControl.getUID();

		try {
			DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
			HttpPost httpPost = new HttpPost(serverUrl);
			httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addTextBody("uid", uid);
			builder.addTextBody("regId", regId);

			httpPost.setEntity(builder.build());
			boolean result = uploader(httpClient, httpPost);
			GCMRegistrar.setRegisteredOnServer(context, result);
			return result;
		} catch (Exception e) {
			GCMRegistrar.setRegisteredOnServer(context, false);
			return false;
		}
	}

	private static boolean uploader(HttpClient httpClient, HttpPost httpPost) {
		HttpResponse httpResponse;
		ResponseHandler<String> res = new BasicResponseHandler();
		boolean result = false;
		try {
			httpResponse = httpClient.execute(httpPost);
			result = (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
			if (result) {
				String response = res.handleResponse(httpResponse).toString();
				result = response.contains("upload success");
			}
		} catch (Exception e) {
		} finally {
			if (httpClient != null) {
				ClientConnectionManager ccm = httpClient.getConnectionManager();
				if (ccm != null)
					ccm.shutdown();
			}
		}
		return result;
	}
}
