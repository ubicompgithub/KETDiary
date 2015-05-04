package ubicomp.ketdiary.system.uploader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.file.MainStorage;
import ubicomp.ketdiary.data.structure.AdditionalQuestionnaire;
import ubicomp.ketdiary.data.structure.BreathDetail;
import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.data.structure.EmotionDIY;
import ubicomp.ketdiary.data.structure.EmotionManagement;
import ubicomp.ketdiary.data.structure.ExchangeHistory;
import ubicomp.ketdiary.data.structure.FacebookInfo;
import ubicomp.ketdiary.data.structure.GCMRead;
import ubicomp.ketdiary.data.structure.Questionnaire;
import ubicomp.ketdiary.data.structure.StorytellingRead;
import ubicomp.ketdiary.data.structure.StorytellingTest;
import ubicomp.ketdiary.data.structure.UserVoiceFeedback;
import ubicomp.ketdiary.data.structure.UserVoiceRecord;
import ubicomp.ketdiary.system.check.DefaultCheck;
import ubicomp.ketdiary.system.check.NetworkCheck;
import ubicomp.ketdiary.system.cleaner.Cleaner;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Used for upload data to the server
 * 
 * @author Stanley Wang
 */
public class DataUploader {

	private static DataUploadTask uploader = null;

	private static Thread cleanThread = null;

	private static final String TAG = "UPLOAD";

	/** Upload the data & remove uploaded data */
	public static void upload() {

		if (cleanThread != null && !cleanThread.isInterrupted()) {
			cleanThread.interrupt();
			cleanThread = null;
		}

		cleanThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Cleaner.clean();
				} catch (Exception e) {
				}
			}
		});
		cleanThread.start();
		try {
			cleanThread.join(500);
		} catch (InterruptedException e) {
		}

		if (DefaultCheck.check() || !NetworkCheck.networkCheck())
			return;

		if (SynchronizedLock.sharedLock.tryLock()) {
			SynchronizedLock.sharedLock.lock();
			uploader = new DataUploadTask();
			uploader.execute();
		}
	}

	/** AsyncTask handles the data uploading task */
	public static class DataUploadTask extends AsyncTask<Void, Void, Void> {

		private DatabaseControl db;

		/** ENUM UPLOAD ERROR */
		public static final int ERROR = -1;
		/** ENUM UPLOAD SUCCESS */
		public static final int SUCCESS = 1;
		private File logDir;

		/** Constructor */
		public DataUploadTask() {
			db = new DatabaseControl();
			logDir = new File(MainStorage.getMainStorageDirectory(), "sequence_log");
		}

		@Override
		protected Void doInBackground(Void... arg0) {

			Log.d(TAG, "upload start");

			// UserInfo
			if (connectToServer() == ERROR) {
				Log.d(TAG, "FAIL TO CONNECT TO THE SERVER");
			}

			// Detection
			Detection detections[] = db.getAllNotUploadedDetection();
			if (detections != null) {
				for (int i = 0; i < detections.length; ++i) {
					if (connectToServer(detections[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - DETECTION");
				}
			}
			// EmotionDIY
			EmotionDIY e_data[] = db.getNotUploadedEmotionDIY();
			if (e_data != null) {
				for (int i = 0; i < e_data.length; ++i) {
					if (connectToServer(e_data[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - EMOTION DIY");
				}
			}

			// EmotionManagement
			EmotionManagement em_data[] = db.getNotUploadedEmotionManagement();
			if (em_data != null) {
				for (int i = 0; i < em_data.length; ++i) {
					if (connectToServer(em_data[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - EMOTION MANAGEMENT");
				}
			}

			// Questionnaire
			Questionnaire[] q_data = db.getNotUploadedQuestionnaire();
			if (q_data != null) {
				for (int i = 0; i < q_data.length; ++i) {
					if (connectToServer(q_data[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - QUESTIONNAIRE");
				}
			}

			// StorytellingTest
			StorytellingTest[] sTest = db.getNotUploadedStorytellingTest();
			if (sTest != null) {
				for (int i = 0; i < sTest.length; ++i) {
					if (connectToServer(sTest[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - STORYTELLING TEST");
				}
			}

			// StorytellingRead
			StorytellingRead[] sRead = db.getNotUploadedStorytellingRead();
			if (sRead != null) {
				for (int i = 0; i < sRead.length; ++i) {
					if (connectToServer(sRead[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - STORYTELLING READ");
				}
			}

			// GCMRead
			GCMRead[] gcm = db.getNotUploadedGCMRead();
			if (gcm != null) {
				for (int i = 0; i < gcm.length; ++i) {
					if (connectToServer(gcm[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - GCM READ");
				}
			}

			// FacebookInfo
			FacebookInfo[] finfo = db.getNotUploadedFacebookInfo();
			if (finfo != null) {
				for (int i = 0; i < finfo.length; ++i) {
					if (connectToServer(finfo[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - FB");
				}
			}

			// UserVoiceRecord
			UserVoiceRecord[] record = db.getNotUploadedUserVoiceRecord();
			if (record != null) {
				for (int i = 0; i < record.length; ++i) {
					if (connectToServer(record[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - UserVoice");
				}
			}

			// Additional Questionnaire
			AdditionalQuestionnaire[] addq = db.getNotUploadedAdditionalQuestionnaire();
			if (addq != null) {
				for (int i = 0; i < addq.length; ++i) {
					if (connectToServer(addq[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - AdditionalQuestionnaire");
				}
			}

			// ClickLog
			String not_uploaded_files[] = getNotUploadedClickLog();
			if (not_uploaded_files != null) {
				for (int i = 0; i < not_uploaded_files.length; ++i) {
					File logFile = new File(logDir.getPath(), not_uploaded_files[i]);
					if (logFile.exists()) {
						Log.d(TAG, "file = " + logFile.getPath());
						if (connectToServer(logFile) == ERROR)
							Log.d(TAG, "FAIL TO UPLOAD - Clicklog");
					}
				}
			}

			// UserVoiceFeedback
			UserVoiceFeedback[] uvfs = db.getNotUploadedUserVoiceFeedback();
			if (uvfs != null) {
				Log.d(TAG, "uvfs != null " + uvfs.length);
				for (int i = 0; i < uvfs.length; ++i) {
					if (connectToServer(uvfs[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - UserVoiceFeedback");
				}
			}

			// ExchangeHistory
			ExchangeHistory[] ehs = db.getNotUploadedExchangeHistory();
			if (ehs != null) {
				for (int i = 0; i < ehs.length; ++i) {
					if (connectToServer(ehs[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - ExchangeHistory");
				}
			}

			// BreathDetail
			BreathDetail[] bds = db.getNotUploadedBreathDetail();
			if (bds != null) {
				for (int i = 0; i < bds.length; ++i) {
					if (connectToServer(bds[i]) == ERROR)
						Log.d(TAG, "FAIL TO UPLOAD - breathDetail");
				}
			} else
				Log.d(TAG, "NO BREATH DETAIL");

			return null;
		}

		private String[] getNotUploadedClickLog() {
			if (!logDir.exists()) {
				Log.d(TAG, "Cannot find clicklog dir");
				return null;
			}

			String[] all_logs = null;
			String latestUpload = null;
			File latestUploadFile = new File(logDir, "latest_uploaded");
			if (latestUploadFile.exists()) {
				try {
					@SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(new FileReader(latestUploadFile));
					latestUpload = br.readLine();
				} catch (IOException e) {
				}
			}
			all_logs = logDir.list(new logFilter(latestUpload));
			return all_logs;
		}

		private class logFilter implements FilenameFilter {
			String _latestUpload;
			String today;

			@SuppressLint("SimpleDateFormat")
			public logFilter(String latestUpload) {
				_latestUpload = latestUpload;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
				today = sdf.format(cal.getTime()) + ".txt";
			}

			@Override
			public boolean accept(File arg0, String arg1) {
				if (arg1.equals("latest_uploaded"))
					return false;
				else {
					if (today.compareTo(arg1) > 0)
						if (_latestUpload == null || (_latestUpload != null && (arg1.compareTo(_latestUpload)) > 0))
							return true;
					return false;
				}
			}
		}

		private void set_uploaded_logfile(String name) {
			File latestUploadFile = new File(logDir, "latest_uploaded");
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(latestUploadFile));
				writer.write(name);
				writer.newLine();
				writer.flush();
				writer.close();
			} catch (IOException e) {
				writer = null;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			uploader = null;
			SynchronizedLock.sharedLock.unlock();
		}

		@Override
		protected void onCancelled() {
			uploader = null;
			SynchronizedLock.sharedLock.unlock();
		}

		private int connectToServer() {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost();
				if (!upload(httpClient, httpPost))
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(Detection detection) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(detection);
				if (upload(httpClient, httpPost)) {
					db.setDetectionUploaded(detection.getTv().getTimestamp());
				} else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(EmotionDIY data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setEmotionDIYUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(EmotionManagement data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setEmotionManagementUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(StorytellingRead data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setStorytellingReadUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(StorytellingTest data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setStorytellingTestUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(GCMRead data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setGCMReadUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(FacebookInfo data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setFacebookInfoUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(UserVoiceRecord data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setUserVoiceRecordUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(Questionnaire data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setQuestionnaireUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(AdditionalQuestionnaire data) {
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setAdditionalQuestionnaireUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(File data) {// ClickLog
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					set_uploaded_logfile(data.getName());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(UserVoiceFeedback data) {// UserVoiceFeedback
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setUserVoiceFeedbackUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(ExchangeHistory data) {// ExchangeHistory
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setExchangeHistoryUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private int connectToServer(BreathDetail data) {// BreathDetail
			try {
				DefaultHttpClient httpClient = HttpSecureClientGenerator.getSecureHttpClient();
				HttpPost httpPost = HttpPostGenerator.genPost(data);
				if (upload(httpClient, httpPost))
					db.setBreathDetailUploaded(data.getTv().getTimestamp());
				else
					return ERROR;
			} catch (Exception e) {
				Log.d(TAG, "EXCEPTION:" + e.toString());
				return ERROR;
			}
			return SUCCESS;
		}

		private boolean upload(HttpClient httpClient, HttpPost httpPost) {
			HttpResponse httpResponse;
			ResponseHandler<String> res = new BasicResponseHandler();
			boolean result = false;
			try {
				httpResponse = httpClient.execute(httpPost);
				int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				result = (httpStatusCode == HttpStatus.SC_OK);
				if (result) {
					String response = res.handleResponse(httpResponse).toString();
					Log.d(TAG, "response=" + response);
					result &= (response.contains("upload success"));
					Log.d(TAG, "result=" + result);
				} else {
					Log.d(TAG, "fail result=" + result);
				}
			} catch (ClientProtocolException e) {
				Log.d(TAG, "ClientProtocolException " + e.toString());
			} catch (IOException e) {
				Log.d(TAG, "IOException " + e.toString());
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

}
