package ubicomp.ketdiary.system.uploader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Used for generating Http POST
 * 
 * @author Stanley Wang
 */
public class HttpPostGenerator {

	private static String SERVER_URL_USER = "";
	private static String SERVER_URL_DETECTION = "";
	private static String SERVER_URL_EMOTION_DIY = "";
	private static String SERVER_URL_EMOTION_MANAGEMENT = "";
	private static String SERVER_URL_QUESTIONNAIRE = "";
	private static String SERVER_URL_STORYTELLING_TEST = "";
	private static String SERVER_URL_STORYTELLING_READ = "";
	private static String SERVER_URL_GCM_READ = "";
	private static String SERVER_URL_FACEBOOK_INFO = "";
	private static String SERVER_URL_USER_VOICE_RECORD = "";
	private static String SERVER_URL_ADDITIONAL_QUESTIONNAIRE = "";
	private static String SERVER_URL_CLICKLOG = "";
	private static String SERVER_URL_USER_VOICE_FEEDBACK = "";
	private static String SERVER_URL_EXCHANGE_HISTORY = "";
	private static String SERVER_URL_BREATH_DETAIL = "";

	/**
	 * Generate POST of User Information
	 * 
	 * @return HttpPost contains User Information
	 */
	public static HttpPost genPost() {
		SERVER_URL_USER = ServerUrl.SERVER_URL_USER();
		HttpPost httpPost = new HttpPost(SERVER_URL_USER);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("uid", uid));

		Calendar c = PreferenceControl.getStartDate();
		String joinDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);

		nvps.add(new BasicNameValuePair("userData[]", joinDate));
		nvps.add(new BasicNameValuePair("userData[]", PreferenceControl.getSensorID()));
		nvps.add(new BasicNameValuePair("userData[]", String.valueOf(PreferenceControl.getUsedCounter())));
		PackageInfo pinfo;
		try {
			pinfo = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), 0);
			String versionName = pinfo.versionName;
			nvps.add(new BasicNameValuePair("userData[]", versionName));
		} catch (NameNotFoundException e) {
		}

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}

		return httpPost;
	}

	/**
	 * Generate POST of Detections
	 * 
	 * @param data
	 *            Detection
	 * @return HttpPost contains Detections
	 * @see ubicomp.ketdiary.data.structure.Detection
	 */
	public static HttpPost genPost(Detection data) {
		SERVER_URL_DETECTION = ServerUrl.SERVER_URL_DETECTION();
		File mainStorageDir = MainStorage.getMainStorageDirectory();
		String uid = PreferenceControl.getUID();
		HttpPost httpPost = new HttpPost(SERVER_URL_DETECTION);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("uid", uid);
		builder.addTextBody("data[]", String.valueOf(data.getTv().getTimestamp()));
		builder.addTextBody("data[]", String.valueOf(data.getTv().getWeek()));
		builder.addTextBody("data[]", String.valueOf(data.getEmotion()));
		builder.addTextBody("data[]", String.valueOf(data.getCraving()));
		builder.addTextBody("data[]", String.valueOf(data.isPrime() ? 1 : 0));
		builder.addTextBody("data[]", String.valueOf(data.getWeeklyScore()));
		builder.addTextBody("data[]", String.valueOf(data.getScore()));

		String _ts = String.valueOf(data.getTv().getTimestamp());
		File[] imageFiles;
		File testFile, geoFile, detectionFile, geoAccuracyFile;
		imageFiles = new File[3];

		testFile = new File(mainStorageDir.getPath() + File.separator + _ts + File.separator + _ts + ".txt");
		geoFile = new File(mainStorageDir.getPath() + File.separator + _ts + File.separator + "geo.txt");
		detectionFile = new File(mainStorageDir.getPath() + File.separator + _ts + File.separator
				+ "detection_detail.txt");
		geoAccuracyFile = new File(mainStorageDir.getPath() + File.separator + _ts + File.separator + "geoAccuracy.txt");

		for (int i = 0; i < imageFiles.length; ++i)
			imageFiles[i] = new File(mainStorageDir.getPath() + File.separator + _ts + File.separator + "IMG_" + _ts
					+ "_" + (i + 1) + ".sob");

		if (testFile.exists())
			builder.addPart("file[]", new FileBody(testFile));
		if (geoFile.exists())
			builder.addPart("file[]", new FileBody(geoFile));
		if (geoAccuracyFile.exists())
			builder.addPart("file[]", new FileBody(geoAccuracyFile));
		if (detectionFile.exists())
			builder.addPart("file[]", new FileBody(detectionFile));
		for (int i = 0; i < imageFiles.length; ++i)
			if (imageFiles[i].exists())
				builder.addPart("file[]", new FileBody(imageFiles[i]));

		httpPost.setEntity(builder.build());
		return httpPost;
	}

	/**
	 * Generate POST of Emotion DIY Results
	 * 
	 * @param data
	 *            EmotionDIY
	 * @return HttpPost contains Emotion DIY
	 * @see ubicomp.ketdiary.data.structure.EmotionDIY
	 */
	public static HttpPost genPost(EmotionDIY data) {
		SERVER_URL_EMOTION_DIY = ServerUrl.SERVER_URL_EMOTION_DIY();
		HttpPost httpPost = new HttpPost(SERVER_URL_EMOTION_DIY);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getSelection())));
		nvps.add(new BasicNameValuePair("data[]", data.getRecreation()));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of Emotion Management results
	 * 
	 * @param data
	 *            EmotionManagement
	 * @return HttpPost contains EmotionManagement
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public static HttpPost genPost(EmotionManagement data) {
		SERVER_URL_EMOTION_MANAGEMENT = ServerUrl.SERVER_URL_EMOTION_MANAGEMENT();
		HttpPost httpPost = new HttpPost(SERVER_URL_EMOTION_MANAGEMENT);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getRecordTv().getYear())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getRecordTv().getMonth() + 1)));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getRecordTv().getDay())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getEmotion())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getType())));
		nvps.add(new BasicNameValuePair("data[]", data.getReason()));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of '!' Questionnaires
	 * 
	 * @param data
	 *            Questionnaire
	 * @return HttpPost contains '!' Questionnaires
	 * @see ubicomp.ketdiary.data.structure.Questionnaire
	 */
	public static HttpPost genPost(Questionnaire data) {
		SERVER_URL_QUESTIONNAIRE = ServerUrl.SERVER_URL_QUESTIONNAIRE();
		HttpPost httpPost = new HttpPost(SERVER_URL_QUESTIONNAIRE);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getType())));
		nvps.add(new BasicNameValuePair("data[]", data.getSeq()));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of test results of Storytelling
	 * 
	 * @param data
	 *            StorytellingTest
	 * @return HttpPost contains StorytellingTest
	 * @see ubicomp.ketdiary.data.structure.StorytellingTest
	 */
	public static HttpPost genPost(StorytellingTest data) {
		SERVER_URL_STORYTELLING_TEST = ServerUrl.SERVER_URL_STORYTELLING_TEST();
		HttpPost httpPost = new HttpPost(SERVER_URL_STORYTELLING_TEST);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getQuestionPage())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.isCorrect() ? 1 : 0)));
		nvps.add(new BasicNameValuePair("data[]", data.getSelection()));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getAgreement())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of Extend Reading of Storytelling
	 * 
	 * @param data
	 *            StorytellingRead
	 * @return HttpPost contains StorytellingRead
	 * @see ubicomp.ketdiary.data.structure.StorytellingRead
	 */
	public static HttpPost genPost(StorytellingRead data) {
		SERVER_URL_STORYTELLING_READ = ServerUrl.SERVER_URL_STORYTELLING_READ();
		HttpPost httpPost = new HttpPost(SERVER_URL_STORYTELLING_READ);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.isAddedScore() ? 1 : 0)));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPage())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of readings the message sent from GCM
	 * 
	 * @param data
	 *            GCMRead
	 * @return HttpPost contains GCMRead
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public static HttpPost genPost(GCMRead data) {
		SERVER_URL_GCM_READ = ServerUrl.SERVER_URL_GCM_READ();
		HttpPost httpPost = new HttpPost(SERVER_URL_GCM_READ);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getReadTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", data.getMessage()));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of Information uploaded to Facebook
	 * 
	 * @param data
	 *            FacebookInfo
	 * @return HttpPost contains FacebookInfo
	 * @see ubicomp.ketdiary.data.structure.FacebookInfo
	 */
	public static HttpPost genPost(FacebookInfo data) {
		SERVER_URL_FACEBOOK_INFO = ServerUrl.SERVER_URL_FACEBOOK();
		HttpPost httpPost = new HttpPost(SERVER_URL_FACEBOOK_INFO);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPageWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPageLevel())));
		nvps.add(new BasicNameValuePair("data[]", data.getText()));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.isAddedScore() ? 1 : 0)));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.isUploadSuccess() ? 1 : 0)));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPrivacy())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of voice record from users
	 * 
	 * @param data
	 *            UserVoiceRecord
	 * @return HttpPost contains UserVoiceRecord
	 * @see ubicomp.ketdiary.data.structure.UserVoiceRecord
	 */
	public static HttpPost genPost(UserVoiceRecord data) {
		SERVER_URL_USER_VOICE_RECORD = ServerUrl.SERVER_URL_USER_VOICE();
		HttpPost httpPost = new HttpPost(SERVER_URL_USER_VOICE_RECORD);
		String uid = PreferenceControl.getUID();
		File mainStorageDir = new File(MainStorage.getMainStorageDirectory(), "audio_records");
		if (!mainStorageDir.exists())
			mainStorageDir.mkdirs();
		boolean uploadFile = PreferenceControl.uploadVoiceRecord();

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("uid", uid);
		builder.addTextBody("data[]", String.valueOf(data.getTv().getTimestamp()));
		builder.addTextBody("data[]", String.valueOf(data.getTv().getWeek()));
		builder.addTextBody("data[]", String.valueOf(data.getRecordTv().getYear()));
		builder.addTextBody("data[]", String.valueOf(data.getRecordTv().getMonth() + 1));
		builder.addTextBody("data[]", String.valueOf(data.getRecordTv().getDay()));
		builder.addTextBody("data[]", String.valueOf(data.getScore()));
		builder.addTextBody("data[]", String.valueOf(uploadFile ? 1 : 0));

		if (mainStorageDir != null) {
			File audio = new File(mainStorageDir, data.toFileString() + ".3gp");
			if (uploadFile && audio.exists())
				builder.addPart("file[]", new FileBody(audio));
		}

		httpPost.setEntity(builder.build());

		return httpPost;
	}

	/**
	 * Generate POST of daily additional questionnaire appears at night
	 * 
	 * @param data
	 *            AdditionalQuestionnaire
	 * @return HttpPost contains AdditionalQuestionnaire
	 * @see ubicomp.ketdiary.data.structure.AdditionalQuestionnaire
	 */
	public static HttpPost genPost(AdditionalQuestionnaire data) {
		SERVER_URL_ADDITIONAL_QUESTIONNAIRE = ServerUrl.SERVER_URL_ADDITIONAL_QUESTIONNAIRE();
		HttpPost httpPost = new HttpPost(SERVER_URL_ADDITIONAL_QUESTIONNAIRE);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getWeek())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getEmotion())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getCraving())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.isAddedScore() ? 1 : 0)));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getScore())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of ClickLog
	 * 
	 * @param logFile
	 *            file of the click log
	 * @return HttpPost contains click log file
	 */
	public static HttpPost genPost(File logFile) {
		SERVER_URL_CLICKLOG = ServerUrl.SERVER_URL_CLICKLOG();
		HttpPost httpPost = new HttpPost(SERVER_URL_CLICKLOG);
		String uid = PreferenceControl.getUID();

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("uid", uid);
		if (logFile.exists()) {
			builder.addPart("file[]", new FileBody(logFile));
		}
		httpPost.setEntity(builder.build());

		return httpPost;
	}

	/**
	 * Generate POST of voice feedback after they retry the BrAC test
	 * 
	 * @param data
	 *            UserVoiceFeedback
	 * @return HttpPost contains UserVoiceFeedback
	 * @see ubicomp.ketdiary.data.structure.UserVoiceFeedback
	 */
	public static HttpPost genPost(UserVoiceFeedback data) {
		SERVER_URL_USER_VOICE_FEEDBACK = ServerUrl.SERVER_URL_USER_FEEDBACK();
		HttpPost httpPost = new HttpPost(SERVER_URL_USER_VOICE_FEEDBACK);
		String uid = PreferenceControl.getUID();
		File mainStorageDir = new File(MainStorage.getMainStorageDirectory(), "feedbacks");
		if (!mainStorageDir.exists())
			mainStorageDir.mkdirs();

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("uid", uid);
		builder.addTextBody("data[]", String.valueOf(data.getTv().getTimestamp()));
		builder.addTextBody("data[]", String.valueOf(data.getDetectionTv().getTimestamp()));
		builder.addTextBody("data[]", String.valueOf(data.isTestSuccess() ? 1 : 0));
		builder.addTextBody("data[]", String.valueOf(data.hasData() ? 1 : 0));
		if (mainStorageDir != null) {
			File audio = new File(mainStorageDir, data.getDetectionTv().getTimestamp() + ".3gp");
			if (audio.exists() && data.hasData()) {
				builder.addPart("file[]", new FileBody(audio));
			}
		}

		httpPost.setEntity(builder.build());

		return httpPost;
	}

	/**
	 * Generate POST of credits exchange history
	 * 
	 * @param data
	 *            ExchangeHistory
	 * @return HttpPost contains ExchangeHistory
	 * @see ubicomp.ketdiary.data.structure.ExchangeHistory
	 */
	public static HttpPost genPost(ExchangeHistory data) {
		SERVER_URL_EXCHANGE_HISTORY = ServerUrl.SERVER_URL_EXCHANGE_HISTORY();
		HttpPost httpPost = new HttpPost(SERVER_URL_EXCHANGE_HISTORY);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getExchangeNum())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

	/**
	 * Generate POST of detail data of BrAC test
	 * 
	 * @param data
	 *            BreathDetail
	 * @return HttpPost contains BreathDetail
	 * @see ubicomp.ketdiary.data.structure.BreathDetail
	 */
	public static HttpPost genPost(BreathDetail data) {
		SERVER_URL_BREATH_DETAIL = ServerUrl.SERVER_URL_BREATH_DETAIL();
		HttpPost httpPost = new HttpPost(SERVER_URL_BREATH_DETAIL);
		String uid = PreferenceControl.getUID();
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("uid", uid));

		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getTv().getTimestamp())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getBlowStartTimes())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getBlowBreakTimes())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPressureDiffMax())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPressureMin())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getPressureAverage())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getVoltageInit())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getDisconnectionMillis())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getSerialDiffMax())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getSerialDiffAverage())));
		nvps.add(new BasicNameValuePair("data[]", String.valueOf(data.getSensorId())));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
		}
		return httpPost;
	}

}
