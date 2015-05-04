package ubicomp.ketdiary.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Helper for initializing the database or update the database
 * 
 * @author Stanley Wang
 */
public class DBHelper extends SQLiteOpenHelper {

	/* SQLiteOpenHelper. need to migrate with */
	private static final String DATABASE_NAME = "ketdiary";
	private static final int DB_VERSION = 9;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Application Context
	 */
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Detection (" + " id INTEGER PRIMARY KEY AUTOINCREMENT, " + " brac FLOAT NOT NULL,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " emotion INTEGER NOT NULL," + " craving INTEGER NOT NULL," + " isPrime INTEGER NOT NULL, "
				+ " weeklyScore INTEGER NOT NULL," + " score INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0"
				+ ")");

		db.execSQL("CREATE TABLE Ranking (" + " user_id CHAR[255] PRIMERY KEY," + " total_score INTEGER NOT NULL,"
				+ " test_score INTEGER NOT NULL  DEFAULT 0," + " advice_score INTEGER NOT NULL  DEFAULT 0,"
				+ " manage_score INTEGER NOT NULL  DEFAULT 0," + " story_score INTEGER NOT NULL DEFAULT 0,"
				+ " advice_questionnaire INTEGER NOT NULL DEFAULT 0,"
				+ " advice_emotion_diy INTEGER NOT NULL DEFAULT 0," + " manage_voice INTEGER NOT NULL DEFAULT 0,"
				+ " manage_emotion INTEGER NOT NULL DEFAULT 0," + " manage_additional INTEGER NOT NULL DEFAULT 0,"
				+ " story_read INTEGER NOT NULL DEFAULT 0," + " story_test INTEGER NOT NULL DEFAULT 0,"
				+ " story_fb INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE RankingShort (" + " user_id CHAR[255] PRIMERY KEY,"
				+ " total_score INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE UserVoiceRecord (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " recordYear INTEGER NOT NULL," + " recordMonth INTEGER NOT NULL," + " recordDay INTEGER NOT NULL,"
				+ " score INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE EmotionDIY (" + " id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER NOT NULL,"
				+ " month INTEGER NOT NULL," + " day INTEGER NOT NULL," + " ts INTEGER NOT NULL,"
				+ " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL," + " selection INTEGER NOT NULL,"
				+ " recreation CHAR[255]," + " score INTEGER NOT NULL," + " upload INTEGER NOT NULL  DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE EmotionManagement (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " recordYear INTEGER NOT NULL," + " recordMonth INTEGER NOT NULL," + " recordDay INTEGER NOT NULL,"
				+ " emotion INTEGER NOT NULL," + " type INTEGER NOT NULL," + " reason CHAR[255] NOT NULL,"
				+ " score INTEGER NOT NULL," + " upload INTEGER NOT NULL  DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE Questionnaire (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " type INTEGER NOT NULL," + " sequence CHAR[255] NOT NULL," + " score INTEGER NOT NULL,"
				+ " upload INTEGER NOT NULL  DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE StorytellingTest (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " questionPage INTEGER NOT NULL," + " isCorrect INTEGER NOT NULL DEFAULT 0,"
				+ " selection CHAR[255] NOT NULL," + " agreement INTEGER NOT NULL DEFAULT 0,"
				+ " score INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE StorytellingRead (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " addedScore INTEGER NOT NULL DEFAULT 0," + " page INTEGER NOT NULL," + " score INTEGER NOT NULL,"
				+ " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE GCMRead (" + " id INTEGER PRIMARY KEY AUTOINCREMENT," + " ts INTEGER NOT NULL,"
				+ " readTs INTEGER NOT NULL DEFAULT 0," + " message CHAR[255] NOT NULL,"
				+ " read INTEGER NOT NULL DEFAULT 0," + " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE FacebookInfo (" + " id INTEGER PRIMARY KEY AUTOINCREMENT," + " year INTEGER NOT NULL,"
				+ " month INTEGER NOT NULL," + " day INTEGER NOT NULL," + " ts INTEGER NOT NULL,"
				+ " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL," + " pageWeek INTEGER NOT NULL,"
				+ " pageLevel INTEGER NOT NULL," + " text CHAR[500] NOT NULL," + " addedScore INTEGER NOT NULL,"
				+ " uploadSuccess INTEGER NOT NULL," + " privacy INTEGER NOT NULL DEFAULT 0,"
				+ " score INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE AdditionalQuestionnaire (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " year INTEGER NOT NULL," + " month INTEGER NOT NULL," + " day INTEGER NOT NULL,"
				+ " ts INTEGER NOT NULL," + " week INTEGER NOT NULL," + " timeSlot INTEGER NOT NULL,"
				+ " addedScore INTEGER NOT NULL DEFAULT 0," + " emotion INTEGER NOT NULL,"
				+ " craving INTEGER NOT NULL," + " score INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0"
				+ ")");

		db.execSQL("CREATE TABLE UserVoiceFeedback (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " ts INTEGER NOT NULL," + " detectionTs INTEGER NOT NULL,"
				+ " testSuccess INTEGER NOT NULL DEFAULT 0," + " hasData INTEGER NOT NULL DEFAULT 0,"
				+ " upload INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE ExchangeHistory (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " ts INTEGER NOT NULL," + " exchangeCounter INTEGER NOT NULL,"
				+ " upload INTEGER NOT NULL DEFAULT 0," + " testSuccess INTEGER NOT NULL DEFAULT 0,"
				+ " hasData INTEGER NOT NULL DEFAULT 0" + ")");

		db.execSQL("CREATE TABLE BreathDetail (" + " id INTEGER PRIMARY KEY AUTOINCREMENT," + " ts INTEGER NOT NULL,"
				+ " blowStartTimes INTEGER NOT NULL," + " blowBreakTimes INTEGER NOT NULL,"
				+ " pressureDiffMax FLOAT NOT NULL," + " pressureMin FLOAT NOT NULL,"
				+ " pressureAverage FLOAT NOT NULL," + " voltageInit INTEGER NOT NULL,"
				+ " disconnectionMillis INTEGER NOT NULL," + " serialDiffMax INTEGER NOT NULL,"
				+ " serialDiffAverage FLOAT NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0,"
				+ " sensorId CHAR[255] NOT NULL DEFAULT '' " + ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old_ver, int new_ver) {
		if (old_ver < 2) {
			db.execSQL("CREATE TABLE UserVoiceFeedback (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " ts INTEGER NOT NULL," + " detectionTs INTEGER NOT NULL," + " upload INTEGER NOT NULL DEFAULT 0"
					+ ")");
			db.execSQL("CREATE TABLE ExchangeHistory (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " ts INTEGER NOT NULL," + " exchangeCounter INTEGER NOT NULL,"
					+ " upload INTEGER NOT NULL DEFAULT 0" + ")");
		}
		if (old_ver < 3) {
			db.execSQL("ALTER TABLE UserVoiceFeedback ADD testSuccess INTEGER NOT NULL DEFAULT 0");
			db.execSQL("ALTER TABLE UserVoiceFeedback ADD hasData INTEGER NOT NULL DEFAULT 0");
		}
		if (old_ver < 4) {
			db.execSQL("DROP TABLE Ranking");
			db.execSQL("CREATE TABLE Ranking (" + " user_id CHAR[255] PRIMERY KEY," + " total_score INTEGER NOT NULL,"
					+ " test_score INTEGER NOT NULL  DEFAULT 0," + " advice_score INTEGER NOT NULL  DEFAULT 0,"
					+ " manage_score INTEGER NOT NULL  DEFAULT 0," + " story_score INTEGER NOT NULL DEFAULT 0,"
					+ " advice_questionnaire INTEGER NOT NULL DEFAULT 0,"
					+ " advice_emotion_diy INTEGER NOT NULL DEFAULT 0," + " manage_voice INTEGER NOT NULL DEFAULT 0,"
					+ " manage_emotion INTEGER NOT NULL DEFAULT 0," + " manage_additional INTEGER NOT NULL DEFAULT 0,"
					+ " story_read INTEGER NOT NULL DEFAULT 0," + " story_test INTEGER NOT NULL DEFAULT 0,"
					+ " story_fb INTEGER NOT NULL DEFAULT 0" + ")");
		}
		if (old_ver < 7) {
			db.execSQL("DROP TABLE UserVoiceFeedback");
			db.execSQL("CREATE TABLE UserVoiceFeedback (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " ts INTEGER NOT NULL," + " detectionTs INTEGER NOT NULL,"
					+ " testSuccess INTEGER NOT NULL DEFAULT 0," + " hasData INTEGER NOT NULL DEFAULT 0,"
					+ " upload INTEGER NOT NULL DEFAULT 0" + ")");
		}
		if (old_ver < 8) {
			db.execSQL("CREATE TABLE BreathDetail (" + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ " ts INTEGER NOT NULL," + " blowStartTimes INTEGER NOT NULL,"
					+ " blowBreakTimes INTEGER NOT NULL," + " pressureDiffMax FLOAT NOT NULL,"
					+ " pressureMin FLOAT NOT NULL," + " pressureAverage FLOAT NOT NULL,"
					+ " voltageInit INTEGER NOT NULL," + " disconnectionMillis INTEGER NOT NULL,"
					+ " serialDiffMax INTEGER NOT NULL," + " serialDiffAverage FLOAT NOT NULL,"
					+ " upload INTEGER NOT NULL DEFAULT 0" + ")");
		}
		if (old_ver < 9) {
			db.execSQL("ALTER TABLE BreathDetail ADD sensorId CHAR[255] NOT NULL DEFAULT '' ");
		}
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@Override
	public synchronized void close() {
		super.close();
	}

}
