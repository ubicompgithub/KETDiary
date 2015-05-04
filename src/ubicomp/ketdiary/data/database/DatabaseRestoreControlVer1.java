package ubicomp.ketdiary.data.database;

import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.data.structure.EmotionDIY;
import ubicomp.ketdiary.data.structure.EmotionManagement;
import ubicomp.ketdiary.data.structure.Questionnaire;
import ubicomp.ketdiary.data.structure.StorytellingRead;
import ubicomp.ketdiary.data.structure.UserVoiceRecord;
import ubicomp.ketdiary.main.App;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Control insertion and modification on database for the restore process
 * 
 * @author Stanley Wang
 * @see ubicomp.ketdiary.data.database.DatabaseRestoreVer1
 */
public class DatabaseRestoreControlVer1 {

	private SQLiteOpenHelper dbHelper = null;
	private SQLiteDatabase db = null;

	/** Constructor */
	public DatabaseRestoreControlVer1() {
		dbHelper = new DBHelper(App.getContext());
	}

	public void restoreDetection(Detection data) {
		db = dbHelper.getWritableDatabase();

		String sql = "SELECT * FROM Detection WHERE year = " + data.getTv().getYear() + " AND month="
				+ data.getTv().getMonth() + " AND day = " + data.getTv().getDay() + " AND timeslot="
				+ data.getTv().getTimeslot();

		Cursor cursor = db.rawQuery(sql, null);
		boolean isPrime = !cursor.moveToFirst();
		cursor.close();

		ContentValues content = new ContentValues();
		content.put("brac", data.getBrac());
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeslot", data.getTv().getTimeslot());
		content.put("emotion", data.getEmotion());
		content.put("craving", data.getCraving());
		content.put("isPrime", isPrime ? 1 : 0);
		content.put("weeklyScore", data.getWeeklyScore());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("Detection", null, content);
		db.close();
	}

	public void restoreEmotionDIY(EmotionDIY data) {
		db = dbHelper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeslot", data.getTv().getTimeslot());
		content.put("selection", data.getSelection());
		content.put("recreation", data.getRecreation());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("EmotionDIY", null, content);
		db.close();
	}

	public void restoreQuestionnaire(Questionnaire data) {
		db = dbHelper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeslot", data.getTv().getTimeslot());
		content.put("type", data.getType());
		content.put("sequence", data.getSeq());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("Questionnaire", null, content);
		db.close();
	}

	public void restoreEmotionManagement(EmotionManagement data) {
		db = dbHelper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeslot", data.getTv().getTimeslot());
		content.put("recordYear", data.getRecordTv().getYear());
		content.put("recordMonth", data.getRecordTv().getMonth());
		content.put("recordDay", data.getRecordTv().getDay());
		content.put("emotion", data.getEmotion());
		content.put("type", data.getType());
		content.put("reason", data.getReason());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("EmotionManagement", null, content);
		db.close();
	}

	public void restoreUserVoiceRecord(UserVoiceRecord data) {
		db = dbHelper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeSlot", data.getTv().getTimeslot());
		content.put("recordYear", data.getRecordTv().getYear());
		content.put("recordMonth", data.getRecordTv().getMonth());
		content.put("recordDay", data.getRecordTv().getDay());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("UserVoiceRecord", null, content);
		db.close();
	}

	public void restoreStorytellingRead(StorytellingRead data) {
		db = dbHelper.getWritableDatabase();
		ContentValues content = new ContentValues();
		content.put("year", data.getTv().getYear());
		content.put("month", data.getTv().getMonth());
		content.put("day", data.getTv().getDay());
		content.put("ts", data.getTv().getTimestamp());
		content.put("week", data.getTv().getWeek());
		content.put("timeSlot", data.getTv().getTimeslot());
		content.put("addedScore", 1);
		content.put("page", data.getPage());
		content.put("score", data.getScore());
		content.put("upload", 1);
		db.insert("StorytellingRead", null, content);
		db.close();
	}

	// Clean All
	public void deleteAll() {
		db = dbHelper.getWritableDatabase();
		String sql = null;
		sql = "DELETE FROM Detection";
		db.execSQL(sql);
		sql = "DELETE FROM Ranking";
		db.execSQL(sql);
		sql = "DELETE FROM RankingShort";
		db.execSQL(sql);
		sql = "DELETE FROM UserVoiceRecord";
		db.execSQL(sql);
		sql = "DELETE FROM EmotionDIY";
		db.execSQL(sql);
		sql = "DELETE FROM EmotionManagement";
		db.execSQL(sql);
		sql = "DELETE FROM Questionnaire";
		db.execSQL(sql);
		sql = "DELETE FROM StorytellingTest";
		db.execSQL(sql);
		sql = "DELETE FROM StorytellingRead";
		db.execSQL(sql);
		sql = "DELETE FROM GCMRead";
		db.execSQL(sql);
		sql = "DELETE FROM FacebookInfo";
		db.execSQL(sql);
		sql = "DELETE FROM AdditionalQuestionnaire";
		db.execSQL(sql);
		sql = "DELETE FROM UserVoiceFeedback";
		db.execSQL(sql);
		sql = "DELETE FROM ExchangeHistory";
		db.execSQL(sql);
		db.close();
	}
}
