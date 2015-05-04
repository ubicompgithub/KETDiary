package ubicomp.ketdiary.system.uploader;

import ubicomp.ketdiary.system.config.PreferenceControl;

/** For generating the server url */
public class ServerUrl {
	private static final String SERVER_URL = "https://140.112.30.171/drugfreediary/";
	private static final String SERVER_URL_DEVELOP = "https://140.112.30.171/drugfreediary_develop/";

	/**
	 * URL for inserting table Alcoholic
	 * 
	 * @return url
	 */
	public static String SERVER_URL_USER() {
		final String URL = "upload/user.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table Detection
	 * 
	 * @return url
	 */
	public static String SERVER_URL_DETECTION() {
		final String URL = "upload/detection.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table EmotionDIY
	 * 
	 * @return url
	 */
	public static String SERVER_URL_EMOTION_DIY() {
		final String URL = "upload/emotionDIY.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table EmotionManagement
	 * 
	 * @return url
	 */
	public static String SERVER_URL_EMOTION_MANAGEMENT() {
		final String URL = "upload/emotionManagement.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table Questionnaire
	 * 
	 * @return url
	 */
	public static String SERVER_URL_QUESTIONNAIRE() {
		final String URL = "upload/questionnaire.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table StorytellingRead
	 * 
	 * @return url
	 */
	public static String SERVER_URL_STORYTELLING_READ() {
		final String URL = "upload/storytellingRead.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table StorytellingTest
	 * 
	 * @return url
	 */
	public static String SERVER_URL_STORYTELLING_TEST() {
		final String URL = "upload/storytellingTest.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for updating column GCM_Id in table Alcoholic
	 * 
	 * @return url
	 */
	public static String SERVER_URL_GCM_REGISTER() {
		final String URL = "GCM/register.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table StorytellingRecord
	 * 
	 * @return url
	 */
	public static String SERVER_URL_USER_VOICE() {
		final String URL = "upload/userVoice.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table Facebook
	 * 
	 * @return url
	 */
	public static String SERVER_URL_FACEBOOK() {
		final String URL = "upload/facebook.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table AdditionalQuestionnaire
	 * 
	 * @return url
	 */
	public static String SERVER_URL_ADDITIONAL_QUESTIONNAIRE() {
		final String URL = "upload/additionalQuestionnaire.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for uploading clicklog
	 * 
	 * @return url
	 */
	public static String SERVER_URL_CLICKLOG() {
		final String URL = "upload/clickLog.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table GCMRead
	 * 
	 * @return url
	 */
	public static String SERVER_URL_GCM_READ() {
		final String URL = "upload/gcmRead.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for querying the long-term ranking
	 * 
	 * @return url
	 */
	public static String SERVER_URL_RANK_ALL() {
		final String URL = "rank/rankAll.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for querying the short-term ranking
	 * 
	 * @return url
	 */
	public static String SERVER_URL_RANK_WEEK() {
		final String URL = "rank/rankWeek.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table ExchangeHistory
	 * 
	 * @return url
	 */
	public static String SERVER_URL_EXCHANGE_HISTORY() {
		final String URL = "upload/exchangeHistory.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table TryAgainFeedback
	 * 
	 * @return url
	 */
	public static String SERVER_URL_USER_FEEDBACK() {
		final String URL = "upload/userFeedback.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}

	/**
	 * URL for inserting table BreathDetail
	 * 
	 * @return url
	 */
	public static String SERVER_URL_BREATH_DETAIL() {
		final String URL = "upload/breathDetail2.php";
		boolean develop = PreferenceControl.isDeveloper();
		if (develop)
			return SERVER_URL_DEVELOP + URL;
		else
			return SERVER_URL + URL;
	}
}
