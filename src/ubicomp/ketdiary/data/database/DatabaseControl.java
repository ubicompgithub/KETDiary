package ubicomp.ketdiary.data.database;

import java.util.Calendar;

import ubicomp.ketdiary.data.structure.AdditionalQuestionnaire;
import ubicomp.ketdiary.data.structure.BreathDetail;
import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.data.structure.EmotionDIY;
import ubicomp.ketdiary.data.structure.EmotionManagement;
import ubicomp.ketdiary.data.structure.ExchangeHistory;
import ubicomp.ketdiary.data.structure.FacebookInfo;
import ubicomp.ketdiary.data.structure.GCMRead;
import ubicomp.ketdiary.data.structure.Questionnaire;
import ubicomp.ketdiary.data.structure.Rank;
import ubicomp.ketdiary.data.structure.StorytellingRead;
import ubicomp.ketdiary.data.structure.StorytellingTest;
import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.data.structure.UserVoiceFeedback;
import ubicomp.ketdiary.data.structure.UserVoiceRecord;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.GPSService;
import ubicomp.ketdiary.system.check.StartDateCheck;
import ubicomp.ketdiary.system.check.WeekNumCheck;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is used for controlling database on the mobile phone side
 * 
 * @author Stanley Wang
 */
public class DatabaseControl {

	/**
	 * SQLiteOpenHelper
	 * 
	 * @see ubicomp.ketdiary.data.database.DBHelper
	 */
	private SQLiteOpenHelper dbHelper = null;
	/** SQLLiteDatabase */
	private SQLiteDatabase db = null;
	/** Lock for preventing congestion */
	private static final Object sqlLock = new Object();

	/** Constructor of DatabaseControl */
	public DatabaseControl() {
		dbHelper = new DBHelper(App.getContext());
	}

	// Detection

	/**
	 * This method is used for getting all prime brac Detection
	 * 
	 * @return An array of Detection. If there are no detections, return null
	 * @see ubicomp.ketdiary.data.structure.Detection
	 */
	public Detection[] getAllPrimeDetection() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM DETECTION WHERE isPrime = 1 ORDER BY ts ASC";
			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			Detection[] detections = new Detection[count];
			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				float brac = cursor.getFloat(1);
				long ts = cursor.getLong(5);
				int emotion = cursor.getInt(8);
				int craving = cursor.getInt(9);
				boolean isPrime = cursor.getInt(10) == 1;
				int weeklyScore = cursor.getInt(11);
				int score = cursor.getInt(12);
				detections[i] = new Detection(brac, ts, emotion, craving,
						isPrime, weeklyScore, score);
			}

			cursor.close();
			db.close();
			return detections;
		}
	}

	/**
	 * This method is used for the latest brac detection
	 * 
	 * @return Detection. If there are no Detection, return a dummy data.
	 * @see ubicomp.ketdiary.data.structure.Detection
	 */
	public Detection getLatestDetection() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM DETECTION ORDER BY ts DESC LIMIT 1";
			Cursor cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new Detection(0f, 0, -1, -1, false, 0, 0);
			}

			float brac = cursor.getFloat(1);
			long ts = cursor.getLong(5);
			int emotion = cursor.getInt(8);
			int craving = cursor.getInt(9);
			boolean isPrime = cursor.getInt(10) == 1;
			int weeklyScore = cursor.getInt(11);
			int score = cursor.getInt(12);
			Detection detection = new Detection(brac, ts, emotion, craving,
					isPrime, weeklyScore, score);

			cursor.close();
			db.close();
			return detection;
		}
	}

	/**
	 * This method is used for inserting a brac detection
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            Inserted Detection
	 * @param update
	 *            If update = true, the previous prime detection will be
	 *            replaced by current Detection
	 * @see ubicomp.ketdiary.data.structure.Detection
	 */
	public int insertDetection(Detection data, boolean update) {
		synchronized (sqlLock) {

			Detection prev_data = getLatestDetection();
			int weeklyScore = prev_data.getWeeklyScore();
			if (prev_data.getTv().getWeek() < data.getTv().getWeek())
				weeklyScore = 0;
			int score = prev_data.getScore();
			db = dbHelper.getWritableDatabase();
			if (!update) {
				boolean isPrime = !(data.isSameTimeBlock(prev_data));
				int isPrimeValue = isPrime ? 1 : 0;
				int addScore = 0;
				addScore += isPrimeValue;
				addScore += isPrime && data.isPass() ? 1 : 0;
				if (!StartDateCheck.afterStartDate())
					addScore = 0;

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
				content.put("isPrime", isPrimeValue);
				content.put("weeklyScore", weeklyScore + addScore);
				content.put("score", score + addScore);
				db.insert("Detection", null, content);
				db.close();
				return addScore;
			} else {
				int addScore = data.isPass() ? 1 : 0;
				if (!StartDateCheck.afterStartDate())
					addScore = 0;
				String sql = "UPDATE Detection SET isPrime = 0 WHERE ts ="
						+ prev_data.getTv().getTimestamp();
				db.execSQL(sql);
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
				content.put("isPrime", 1);
				content.put("weeklyScore", weeklyScore + addScore);
				content.put("score", score + addScore);
				db.insert("Detection", null, content);
				db.close();
				return addScore;
			}
		}
	}

	/**
	 * This method is used for getting brac values of today's prime detections
	 * 
	 * @return An array of float (length = 3) [0]:morning [1]:afternoon
	 *         [2]:night
	 */
	public Float[] getTodayPrimeBrac() {
		synchronized (sqlLock) {
			Float[] brac = new Float[3];
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DATE);

			db = dbHelper.getReadableDatabase();

			String sql = "SELECT brac,timeSlot FROM Detection WHERE year = "
					+ year + " AND month = " + month + " AND day = " + day
					+ " AND isPrime = 1" + " ORDER BY timeSlot ASC";
			Cursor cursor = db.rawQuery(sql, null);

			int count = cursor.getCount();

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				float _brac = cursor.getFloat(0);
				int _timeSlot = cursor.getInt(1);
				brac[_timeSlot] = _brac;
			}
			cursor.close();
			db.close();
			return brac;
		}
	}

	/**
	 * This method is used for getting brac values of previous n-day prime
	 * detections
	 * 
	 * @return An array of float (length = n_days*3) [idx], idx%3=0:morning,
	 *         idx%3=1:afternoon, idx%[3]=2:night
	 */
	public Float[] getMultiDaysPrimeBrac(int n_days) {
		synchronized (sqlLock) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			final long DAY = AlarmManager.INTERVAL_DAY;
			long ts_days = (long) (n_days - 1) * DAY;
			long start_ts = cal.getTimeInMillis() - ts_days;

			String sql = "SELECT brac,ts,timeSlot FROM Detection WHERE ts >="
					+ start_ts + " AND isPrime = 1" + " ORDER BY ts ASC";
			db = dbHelper.getReadableDatabase();
			Cursor cursor = db.rawQuery(sql, null);

			Float[] brac = new Float[3 * n_days];
			long ts_from = start_ts;
			long ts_to = start_ts + DAY;
			int pointer = 0;
			int count = cursor.getCount();

			for (int i = 0; i < brac.length; ++i) {
				int timeSlot = i % 3;

				float _brac;
				long _ts;
				int _timeSlot;
				while (pointer < count) {
					cursor.moveToPosition(pointer);
					_brac = cursor.getFloat(0);
					_ts = cursor.getLong(1);
					_timeSlot = cursor.getInt(2);
					if (_ts < ts_from) {
						++pointer;
						continue;
					} else if (_ts >= ts_to) {
						break;
					}
					if (_timeSlot > timeSlot)
						break;
					else if (_timeSlot < timeSlot) {
						++pointer;
						continue;
					}
					brac[i] = _brac;
					break;
				}

				if (timeSlot == 2) {
					ts_from += DAY;
					ts_to += DAY;
				}
			}
			cursor.close();
			db.close();
			return brac;
		}
	}

	/**
	 * This method is used for labeling which detection is uploaded to the
	 * server
	 * 
	 * @param ts
	 *            timestamp of the detection
	 */
	public void setDetectionUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE Detection SET upload = 1 WHERE ts=" + ts;
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * This method is used for getting weekly scores of current week's
	 * detections
	 * 
	 * @return An array of weekly score. Length=# weeks
	 */
	public Integer[] getDetectionScoreByWeek() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			int curWeek = WeekNumCheck.getWeek(Calendar.getInstance()
					.getTimeInMillis());
			Integer[] scores = new Integer[curWeek + 1];

			String sql = "SELECT weeklyScore, week FROM Detection WHERE week<="
					+ curWeek + " GROUP BY week";

			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			int pointer = 0;
			int week = 0;
			for (int i = 0; i < scores.length; ++i) {
				while (pointer < count) {
					cursor.moveToPosition(pointer);
					week = cursor.getInt(1);
					if (week < i) {
						++pointer;
						continue;
					} else if (week > i)
						break;
					int weeklyScore = cursor.getInt(0);
					scores[i] = weeklyScore;
					break;
				}
			}
			for (int i = 0; i < scores.length; ++i)
				if (scores[i] == null)
					scores[i] = 0;

			cursor.close();
			db.close();
			return scores;
		}
	}

	/**
	 * This method is used for getting detections which are not uploaded to the
	 * server
	 * 
	 * @return An array of Detection. If there are no detections, return null.
	 * @see ubicomp.ketdiary.data.structure.Detection
	 */
	public Detection[] getAllNotUploadedDetection() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			long cur_ts = System.currentTimeMillis();
			long gps_ts = PreferenceControl.getGPSStartTime()
					+ GPSService.GPS_TOTAL_TIME;
			String sql;
			if (cur_ts <= gps_ts) {
				long gps_detection_ts = PreferenceControl
						.getDetectionTimestamp();
				sql = "SELECT * FROM Detection WHERE upload = 0 AND ts <> "
						+ gps_detection_ts + " ORDER BY ts ASC";
			} else {
				sql = "SELECT * FROM Detection WHERE upload = 0  ORDER BY ts ASC";
			}
			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			Detection[] detections = new Detection[count];
			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				float brac = cursor.getFloat(1);
				long ts = cursor.getLong(5);
				int emotion = cursor.getInt(8);
				int craving = cursor.getInt(9);
				boolean isPrime = cursor.getInt(10) == 1;
				int weeklyScore = cursor.getInt(11);
				int score = cursor.getInt(12);
				detections[i] = new Detection(brac, ts, emotion, craving,
						isPrime, weeklyScore, score);
			}
			cursor.close();
			db.close();
			return detections;
		}
	}

	/**
	 * This method is used for checking if the user do the brac detection at
	 * this time slot
	 * 
	 * @return true if the user do a brac detection at the current time slot
	 */
	public boolean detectionIsDone() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			long ts = System.currentTimeMillis();
			TimeValue tv = TimeValue.generate(ts);
			String sql = "SELECT id FROM Detection WHERE" + " year ="
					+ tv.getYear() + " AND month = " + tv.getMonth()
					+ " AND day= " + tv.getDay() + " AND timeSlot= "
					+ tv.getTimeslot();
			Cursor cursor = db.rawQuery(sql, null);
			boolean result = cursor.getCount() > 0;
			cursor.close();
			db.close();
			return result;
		}
	}

	/**
	 * This method is used for counting total passed prime detections
	 * 
	 * @return # of passed prime detections
	 */
	public int getPrimeDetectionPassTimes() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM Detection WHERE isPrime = 1 AND brac < "
					+ Detection.BRAC_THRESHOLD;
			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			cursor.close();
			db.close();
			return count;
		}
	}

	/**
	 * This method is used for checking if the user can replace the current
	 * detection
	 * 
	 * @return true if the user is allowed to replace the detection
	 */
	public boolean canTryAgain() {
		synchronized (sqlLock) {
			TimeValue curTV = TimeValue.generate(System.currentTimeMillis());
			int year = curTV.getYear();
			int month = curTV.getMonth();
			int day = curTV.getDay();
			int timeslot = curTV.getTimeslot();
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM DETECTION WHERE year=" + year
					+ " AND month=" + month + " AND day=" + day
					+ " AND timeSlot=" + timeslot;
			Cursor cursor = db.rawQuery(sql, null);
			return (cursor.getCount() == 1);
		}
	}

	// Ranking

	/**
	 * Get the user's rank
	 * 
	 * @return Rank. If there are no data, return dummy data with UID=""
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public Rank getMyRank() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM Ranking WHERE user_id='"
					+ PreferenceControl.getUID() + "'";
			Cursor cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new Rank("", 0);
			}
			String uid = cursor.getString(0);
			int score = cursor.getInt(1);
			int test = cursor.getInt(2);
			int advice = cursor.getInt(3);
			int manage = cursor.getInt(4);
			int story = cursor.getInt(5);
			int[] additionals = new int[8];
			for (int j = 0; j < additionals.length; ++j)
				additionals[j] = cursor.getInt(6 + j);
			Rank rank = new Rank(uid, score, test, advice, manage, story,
					additionals);
			cursor.close();
			db.close();
			return rank;
		}
	}

	/**
	 * Get all user's ranks
	 * 
	 * @return An array of Rank. If there are no Rank, return null.
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public Rank[] getAllRanks() {
		synchronized (sqlLock) {
			Rank[] ranks = null;
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM Ranking ORDER BY total_score DESC, user_id ASC";
			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}
			ranks = new Rank[count];
			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				String uid = cursor.getString(0);
				int score = cursor.getInt(1);
				int test = cursor.getInt(2);
				int advice = cursor.getInt(3);
				int manage = cursor.getInt(4);
				int story = cursor.getInt(5);
				int[] additionals = new int[8];
				for (int j = 0; j < additionals.length; ++j)
					additionals[j] = cursor.getInt(6 + j);
				ranks[i] = new Rank(uid, score, test, advice, manage, story,
						additionals);
			}
			cursor.close();
			db.close();
			return ranks;
		}
	}

	/**
	 * Get the user's rank in a short period
	 * 
	 * @return An array of Rank. If there are no Rank, return null.
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public Rank[] getAllRankShort() {
		synchronized (sqlLock) {
			Rank[] ranks = null;
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM RankingShort ORDER BY total_score DESC, user_id ASC";
			Cursor cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}
			ranks = new Rank[count];
			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				String uid = cursor.getString(0);
				int score = cursor.getInt(1);
				ranks[i] = new Rank(uid, score);
			}
			cursor.close();
			db.close();
			return ranks;
		}
	}

	/**
	 * Truncate the Ranking table
	 * 
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public void clearRank() {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "DELETE  FROM Ranking";
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * Update the Rank
	 * 
	 * @param data
	 *            Updated Rank
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public void updateRank(Rank data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "SELECT * FROM Ranking WHERE user_id = '"
					+ data.getUid() + "'";
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.getCount() == 0) {
				ContentValues content = new ContentValues();
				content.put("user_id", data.getUid());
				content.put("total_score", data.getScore());
				content.put("test_score", data.getTest());
				content.put("advice_score", data.getAdvice());
				content.put("manage_score", data.getManage());
				content.put("story_score", data.getStory());
				content.put("advice_questionnaire",
						data.getAdviceQuestionnaire());
				content.put("advice_emotion_diy", data.getAdviceEmotionDiy());
				content.put("manage_voice", data.getManageVoice());
				content.put("manage_emotion", data.getManageEmotion());
				content.put("manage_additional", data.getManageAdditional());
				content.put("story_read", data.getStoryRead());
				content.put("story_test", data.getStoryTest());
				content.put("story_fb", data.getStoryFb());
				db.insert("Ranking", null, content);
			} else {
				sql = "UPDATE Ranking SET" + " total_score = "
						+ data.getScore() + "," + " test_score = "
						+ data.getTest() + "," + " advice_score = "
						+ data.getAdvice() + "," + " manage_score="
						+ data.getManage() + "," + " story_score = "
						+ data.getStory() + "," + " advice_questionnaire="
						+ data.getAdviceQuestionnaire() + ","
						+ " advice_emotion_diy=" + data.getAdviceEmotionDiy()
						+ "," + " manage_voice=" + data.getManageVoice() + ","
						+ " manage_emotion=" + data.getManageEmotion() + ","
						+ " manage_additional=" + data.getManageAdditional()
						+ "," + " story_read=" + data.getStoryRead() + ","
						+ " story_test=" + data.getStoryTest() + ","
						+ " story_fb=" + data.getStoryFb()
						+ " WHERE user_id = " + "'" + data.getUid() + "'";
				db.execSQL(sql);
			}
			cursor.close();
			db.close();
		}
	}

	/**
	 * Truncate the RankingShort table
	 * 
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public void clearRankShort() {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "DELETE  FROM RankingShort";
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * Update the Rank in a short period
	 * 
	 * @param data
	 *            Updated Rank in a short period
	 * @see ubicomp.ketdiary.data.structure.Rank
	 */
	public void updateRankShort(Rank data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "SELECT * FROM RankingShort WHERE user_id = '"
					+ data.getUid() + "'";
			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.getCount() == 0) {
				ContentValues content = new ContentValues();
				content.put("user_id", data.getUid());
				content.put("total_score", data.getScore());
				db.insert("RankingShort", null, content);
			} else {
				sql = "UPDATE RankingShort SET" + " total_score = "
						+ data.getScore() + " WHERE user_id = " + "'"
						+ data.getUid() + "'";
				db.execSQL(sql);
			}
			cursor.close();
			db.close();
		}
	}

	// Questionnaire

	/**
	 * Get the latest ''! Questionnaire
	 * 
	 * @return Questionnaire. If there are no Questionnaire, return a dummy
	 *         data.
	 * @see ubicomp.ketdiary.data.structure.Questionnaire
	 */
	public Questionnaire getLatestQuestionnaire() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM Questionnaire WHERE type >= 0 ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new Questionnaire(0, 0, null, 0);
			}
			long ts = cursor.getLong(4);
			int type = cursor.getInt(7);
			String seq = cursor.getString(8);
			int score = cursor.getInt(9);
			return new Questionnaire(ts, type, seq, score);
		}
	}

	/**
	 * Insert the latest ''! Questionnaire
	 * 
	 * @return # credits got by the user
	 * @param data
	 *            Inserted Questionnaire
	 * @see ubicomp.ketdiary.data.structure.Questionnaire
	 */
	public int insertQuestionnaire(Questionnaire data) {
		synchronized (sqlLock) {
			Questionnaire prev_data = getLatestQuestionnaire();
			int addScore = 0;
			if (!prev_data.getTv().isSameTimeBlock(data.getTv())
					&& data.getType() >= 0)
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;
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
			content.put("score", prev_data.getScore() + addScore);
			db.insert("Questionnaire", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all ''! Questionnaire which are not uploaded to the server
	 * 
	 * @return An array of Questionnaire. If there are no Questionnaire, return
	 *         null.
	 * @see ubicomp.ketdiary.data.structure.Questionnaire
	 */
	public Questionnaire[] getNotUploadedQuestionnaire() {
		synchronized (sqlLock) {
			Questionnaire[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM Questionnaire WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new Questionnaire[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int type = cursor.getInt(7);
				String seq = cursor.getString(8);
				int score = cursor.getInt(9);
				data[i] = new Questionnaire(ts, type, seq, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the ''! Questionnaire uploaded
	 * 
	 * @param ts
	 *            Timestamp of the Questionnaire
	 * @see ubicomp.ketdiary.data.structure.Questionnaire
	 */
	public void setQuestionnaireUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE Questionnaire SET upload = 1 WHERE ts = " + ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// EmotionDIY

	/**
	 * Get the latest Emotion DIY result
	 * 
	 * @return EmotionDIY. If there are no EmotionDIY, return a dummy data.
	 * @see ubicomp.ketdiary.data.structure.EmotionDIY
	 */
	public EmotionDIY getLatestEmotionDIY() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM EmotionDIY ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new EmotionDIY(0, 0, null, 0);
			}
			long ts = cursor.getLong(4);
			int selection = cursor.getInt(7);
			String recreation = cursor.getString(8);
			int score = cursor.getInt(9);
			return new EmotionDIY(ts, selection, recreation, score);
		}
	}

	/**
	 * Insert an Emotion DIY result
	 * 
	 * @return # credits got by the user
	 * @see ubicomp.ketdiary.data.structure.EmotionDIY
	 */
	public int insertEmotionDIY(EmotionDIY data) {
		synchronized (sqlLock) {
			EmotionDIY prev_data = getLatestEmotionDIY();
			int addScore = 0;
			if (!prev_data.getTv().isSameTimeBlock(data.getTv()))
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;
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
			content.put("score", prev_data.getScore() + addScore);
			db.insert("EmotionDIY", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all Emotion DIY results which are not uploaded to the server
	 * 
	 * @return An array of EmotionDIY. If there are no EmotionDIY, return null.
	 * @see ubicomp.ketdiary.data.structure.EmotionDIY
	 */
	public EmotionDIY[] getNotUploadedEmotionDIY() {
		synchronized (sqlLock) {
			EmotionDIY[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM EmotionDIY WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new EmotionDIY[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int selection = cursor.getInt(7);
				String recreation = cursor.getString(8);
				int score = cursor.getInt(9);
				data[i] = new EmotionDIY(ts, selection, recreation, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the Emotion DIY result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the Emotion DIY result
	 * @see ubicomp.ketdiary.data.structure.EmotionDIY
	 */
	public void setEmotionDIYUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE EmotionDIY SET upload = 1 WHERE ts = " + ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// EmotionManagement

	/**
	 * Get the latest Emotion Management result
	 * 
	 * @return EmotionManagement. If there are no EmotionManagement, return a
	 *         dummy data.
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public EmotionManagement getLatestEmotionManagement() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM EmotionManagement ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(0);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);
				return new EmotionManagement(0, year, month, day, 0, 0, null, 0);
			}
			long ts = cursor.getLong(4);
			int year = cursor.getInt(7);
			int month = cursor.getInt(8);
			int day = cursor.getInt(9);
			int emotion = cursor.getInt(10);
			int type = cursor.getInt(11);
			String reason = cursor.getString(12);
			int score = cursor.getInt(13);
			return new EmotionManagement(ts, year, month, day, emotion, type,
					reason, score);
		}
	}

	/**
	 * Insert an Emotion Management result
	 * 
	 * @return # of credits got by the user
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public int insertEmotionManagement(EmotionManagement data) {
		synchronized (sqlLock) {
			EmotionManagement prev_data = getLatestEmotionManagement();
			int addScore = 0;
			if (!prev_data.getTv().isSameTimeBlock(data.getTv()))
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;

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
			content.put("score", prev_data.getScore() + addScore);
			db.insert("EmotionManagement", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all EmotionManagement results which are not uploaded to the server
	 * 
	 * @return An array of EmotionManagement results If there are no
	 *         EmotionManagement, return null.
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public EmotionManagement[] getNotUploadedEmotionManagement() {
		synchronized (sqlLock) {
			EmotionManagement[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM EmotionManagement WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new EmotionManagement[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int year = cursor.getInt(7);
				int month = cursor.getInt(8);
				int day = cursor.getInt(9);
				int emotion = cursor.getInt(10);
				int type = cursor.getInt(11);
				String reason = cursor.getString(12);
				int score = cursor.getInt(13);
				data[i] = new EmotionManagement(ts, year, month, day, emotion,
						type, reason, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Get EmotionManagement results by date
	 * 
	 * @param rYear
	 *            record Year
	 * @param rMonth
	 *            record Month (0~11)
	 * @param rDay
	 *            record Day of Month
	 * @return An array of EmotionManagement results @ rYear/rMonth/rDay. If
	 *         there are no EmotionManagement, return null.
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public EmotionManagement[] getDayEmotionManagement(int rYear, int rMonth,
			int rDay) {
		synchronized (sqlLock) {
			EmotionManagement[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM EmotionManagement WHERE recordYear = " + rYear
					+ " AND recordMonth = " + rMonth + " AND recordDay = "
					+ rDay + " ORDER BY id DESC";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new EmotionManagement[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int year = cursor.getInt(7);
				int month = cursor.getInt(8);
				int day = cursor.getInt(9);
				int emotion = cursor.getInt(10);
				int type = cursor.getInt(11);
				String reason = cursor.getString(12);
				int score = cursor.getInt(13);
				data[i] = new EmotionManagement(ts, year, month, day, emotion,
						type, reason, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the EmotionManagement result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded EmotionManagement
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 */
	public void setEmotionManagementUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE EmotionManagement SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * Get the latest 4 reasons of EmotionManagement by reason type
	 * 
	 * @param type
	 *            reason type of EmotionManagement.
	 * @return An array of reasons. There are no reasons, return null
	 */
	public String[] getEmotionManagementString(int type) {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT DISTINCT reason FROM EmotionManagement WHERE type = "
					+ type + " ORDER BY ts DESC LIMIT 4";
			String[] out = null;

			Cursor cursor = db.rawQuery(sql, null);
			if (cursor.getCount() == 0) {
				cursor.close();
				db.close();
				return null;
			}
			out = new String[cursor.getCount()];

			for (int i = 0; i < out.length; ++i)
				if (cursor.moveToPosition(i))
					out[i] = cursor.getString(0);

			cursor.close();
			db.close();
			return out;
		}
	}

	/**
	 * Get if there are EmotionManagement results at the date
	 * 
	 * @param tv
	 *            TimeValue of the date
	 * @return true if exists EmotionManagement
	 * @see ubicomp.ketdiary.data.structure.TimeValue
	 */
	public boolean hasEmotionManagement(TimeValue tv) {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM EmotionManagement WHERE" + " recordYear ="
					+ tv.getYear() + " AND recordMonth=" + tv.getMonth()
					+ " AND recordDay =" + tv.getDay();
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			cursor.close();
			db.close();
			return count > 0;
		}
	}

	// StorytellingTest

	/**
	 * Get the latest StorytellingTest result
	 * 
	 * @return StorytellingTest. If there are no StorytellingTest, return a
	 *         dummy data.
	 * @see ubicomp.ketdiary.data.structure.StorytellingTest
	 */
	public StorytellingTest getLatestStorytellingTest() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM StorytellingTest WHERE isCorrect = 1 ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new StorytellingTest(0, 0, false, "", 0, 0);
			}
			long ts = cursor.getLong(4);
			int page = cursor.getInt(7);
			boolean isCorrect = (cursor.getInt(8) == 1) ? true : false;
			String selection = cursor.getString(9);
			int agreement = cursor.getInt(10);
			int score = cursor.getInt(11);
			return new StorytellingTest(ts, page, isCorrect, selection,
					agreement, score);
		}
	}

	/**
	 * Insert a StorytellingTest result
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            inserted StorytellingTest
	 * @see ubicomp.ketdiary.data.structure.StorytellingTest
	 */
	public int insertStorytellingTest(StorytellingTest data) {
		synchronized (sqlLock) {
			StorytellingTest prev_data = getLatestStorytellingTest();
			int addScore = 0;
			if (!prev_data.getTv().isSameTimeBlock(data.getTv())
					&& data.isCorrect())
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;

			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("year", data.getTv().getYear());
			content.put("month", data.getTv().getMonth());
			content.put("day", data.getTv().getDay());
			content.put("ts", data.getTv().getTimestamp());
			content.put("week", data.getTv().getWeek());
			content.put("timeslot", data.getTv().getTimeslot());
			content.put("questionPage", data.getQuestionPage());
			content.put("isCorrect", data.isCorrect() ? 1 : 0);
			content.put("selection", data.getSelection());
			content.put("agreement", data.getAgreement());
			content.put("score", prev_data.getScore() + addScore);
			db.insert("StorytellingTest", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all StorytellingTest results which are not uploaded to the server
	 * 
	 * @return An array of StorytellingTest. If there are no StorytellingTest,
	 *         return null.
	 * @see ubicomp.ketdiary.data.structure.StorytellingTest
	 */
	public StorytellingTest[] getNotUploadedStorytellingTest() {
		synchronized (sqlLock) {
			StorytellingTest[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM StorytellingTest  WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new StorytellingTest[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int page = cursor.getInt(7);
				boolean isCorrect = (cursor.getInt(8) == 1) ? true : false;
				String selection = cursor.getString(9);
				int agreement = cursor.getInt(10);
				int score = cursor.getInt(11);
				data[i] = new StorytellingTest(ts, page, isCorrect, selection,
						agreement, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the StorytellingTest result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded StorytellingTest
	 * @see ubicomp.ketdiary.data.structure.StorytellingTest
	 */
	public void setStorytellingTestUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE StorytellingTest SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// UserVoiceRecord

	/**
	 * Get the latest UserVoiceRecord (Voice recorded by user)
	 * 
	 * @return UserVoiceRecord. If there are no UserVoiceRecord, return a dummy
	 *         data.
	 * @see ubicomp.ketdiary.data.structure.UserVoiceRecord
	 */
	public UserVoiceRecord getLatestUserVoiceRecord() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM UserVoiceRecord ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(0);
				return new UserVoiceRecord(0, cal.get(Calendar.YEAR),
						cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH), 0);
			}
			long ts = cursor.getLong(4);
			int rYear = cursor.getInt(7);
			int rMonth = cursor.getInt(8);
			int rDay = cursor.getInt(9);
			int score = cursor.getInt(10);
			return new UserVoiceRecord(ts, rYear, rMonth, rDay, score);
		}
	}

	/**
	 * Insert an UserVoiceRecord
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            inserted UserVoiceRecord
	 * @see ubicomp.ketdiary.data.structure.UserVoiceRecord
	 */
	public int insertUserVoiceRecord(UserVoiceRecord data) {
		synchronized (sqlLock) {
			UserVoiceRecord prev_data = getLatestUserVoiceRecord();
			int addScore = 0;
			if (!prev_data.getTv().isSameTimeBlock(data.getTv()))
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;

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
			content.put("score", prev_data.getScore() + addScore);
			db.insert("UserVoiceRecord", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all UserVoiceRecord which are not uploaded to the server
	 * 
	 * @return An array of UserVoiceRecord. If there are no UserVoiceRecord,
	 *         return null.
	 * @see ubicomp.ketdiary.data.structure.UserVoiceRecord
	 */
	public UserVoiceRecord[] getNotUploadedUserVoiceRecord() {
		synchronized (sqlLock) {
			UserVoiceRecord[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM UserVoiceRecord WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new UserVoiceRecord[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int rYear = cursor.getInt(7);
				int rMonth = cursor.getInt(8);
				int rDay = cursor.getInt(9);
				int score = cursor.getInt(10);
				data[i] = new UserVoiceRecord(ts, rYear, rMonth, rDay, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the UserVoiceRecord result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded UserVoiceRecord
	 * @see ubicomp.ketdiary.data.structure.UserVoiceRecord
	 */
	public void setUserVoiceRecordUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE UserVoiceRecord SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * Check if there are UserVoiceRecord at the date
	 * 
	 * @param tv
	 *            TimeValue of the date
	 * @return true if there are UserVoiceRecord exist
	 * @see ubicomp.ketdiary.data.structure.TimeValue
	 */
	public boolean hasUserVoiceRecord(TimeValue tv) {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM UserVoiceRecord WHERE" + " recordYear ="
					+ tv.getYear() + " AND recordMonth=" + tv.getMonth()
					+ " AND recordDay =" + tv.getDay();
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			cursor.close();
			db.close();
			return count > 0;
		}
	}

	/**
	 * Check if there are UserVoiceRecord or EmotionManagement at the dates
	 * 
	 * @param tvs
	 *            Array of timeValue of the date (ascend order)
	 * @return array of booleans represents if there exists any VoiceRecords or
	 *         EmotionManagements at dates
	 * @see ubicomp.ketdiary.data.structure.TimeValue
	 * @see ubicomp.ketdiary.data.structure.EmotionManagement
	 * @see ubicomp.ketdiary.data.structure.VoiceRecord
	 */
	public boolean[] hasUserVoiceRecordOrEmotionManagement(TimeValue[] tvs) {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursorVoice, cursorEmotionManagement;

			sql = "SELECT recordYear, recordMonth, recordDay FROM UserVoiceRecord GROUP BY recordYear, recordMonth, recordDay ORDER BY recordYear ASC, recordMonth ASC, recordDay ASC";
			cursorVoice = db.rawQuery(sql, null);
			sql = "SELECT recordYear, recordMonth, recordDay FROM EmotionManagement GROUP BY recordYear, recordMonth, recordDay ORDER BY recordYear ASC, recordMonth ASC, recordDay ASC";
			cursorEmotionManagement = db.rawQuery(sql, null);

			boolean[] results = new boolean[tvs.length];

			int countVoice = cursorVoice.getCount();
			int countEmotionManagement = cursorEmotionManagement.getCount();

			if (countVoice + countEmotionManagement > 0) {
				int idxVoice = 0;
				int idxEmotionManagement = 0;
				for (int i = 0; i < results.length; ++i) {
					results[i] = false;
					while (idxVoice < countVoice) {
						cursorVoice.moveToPosition(idxVoice);
						int year = cursorVoice.getInt(0);
						int month = cursorVoice.getInt(1);
						int day = cursorVoice.getInt(2);
						int diff = tvs[i].beforeAfter(year, month, day);
						results[i] |= (diff == 0);
						if (diff >= 0)
							break;
						++idxVoice;
					}
					while (idxEmotionManagement < countEmotionManagement) {
						cursorEmotionManagement
								.moveToPosition(idxEmotionManagement);
						int year = cursorEmotionManagement.getInt(0);
						int month = cursorEmotionManagement.getInt(1);
						int day = cursorEmotionManagement.getInt(2);
						int diff = tvs[i].beforeAfter(year, month, day);
						results[i] |= (diff == 0);
						if (diff >= 0)
							break;
						++idxEmotionManagement;
					}
				}
			} else {
				for (int i = 0; i < results.length; ++i)
					results[i] = false;
			}

			cursorVoice.close();
			cursorEmotionManagement.close();
			db.close();
			return results;
		}
	}

	// StorytellingRead

	/**
	 * Get the latest StorytellingRead
	 * 
	 * @return StorytellingRead. If there are no StorytellingRead, return a
	 *         dummy data.
	 * @see ubicomp.ketdiary.data.structure.StorytellingRead
	 */
	public StorytellingRead getLatestStorytellingRead() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM  StorytellingRead WHERE addedScore = 1 ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new StorytellingRead(0, false, 0, 0);
			}
			long ts = cursor.getLong(4);
			boolean addedScore = (cursor.getInt(7) == 1);
			int page = cursor.getInt(8);
			int score = cursor.getInt(9);
			return new StorytellingRead(ts, addedScore, page, score);
		}
	}

	/**
	 * Insert a StorytellingRead result
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            inserted StorytellingRead
	 * @see ubicomp.ketdiary.data.structure.StorytellingRead
	 */
	public int insertStorytellingRead(StorytellingRead data) {
		synchronized (sqlLock) {
			StorytellingRead prev_data = getLatestStorytellingRead();
			int addScore = 0;
			int addedScore = 0;
			if (data.getTv().afterADay(prev_data.getTv())) {
				addScore = 3;
				addedScore = 1;
			}
			if (!StartDateCheck.afterStartDate()) {
				addScore = 0;
				addedScore = 0;
			}

			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("year", data.getTv().getYear());
			content.put("month", data.getTv().getMonth());
			content.put("day", data.getTv().getDay());
			content.put("ts", data.getTv().getTimestamp());
			content.put("week", data.getTv().getWeek());
			content.put("timeSlot", data.getTv().getTimeslot());
			content.put("addedScore", addedScore);
			content.put("page", data.getPage());
			content.put("score", prev_data.getScore() + addScore);
			db.insert("StorytellingRead", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all StorytellingRead results which are not uploaded to the server
	 * 
	 * @return An array of StorytellingRead. If there are no StorytellingRead,
	 *         return null.
	 * @see ubicomp.ketdiary.data.structure.StorytellingRead
	 */
	public StorytellingRead[] getNotUploadedStorytellingRead() {
		synchronized (sqlLock) {
			StorytellingRead[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM StorytellingRead WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new StorytellingRead[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				boolean addedScore = (cursor.getInt(7) == 1);
				int page = cursor.getInt(8);
				int score = cursor.getInt(9);
				data[i] = new StorytellingRead(ts, addedScore, page, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the StorytellingRead result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded StorytellingRead
	 * @see ubicomp.ketdiary.data.structure.StorytellingRead
	 */
	public void setStorytellingReadUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE StorytellingRead SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// GCMRead

	/**
	 * Get the GCMRead message by timestamp
	 * 
	 * @return GCMRead. If there are no GCMRead, return null.
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public GCMRead getGCMRead(long timestamp) {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql = "SELECT * FROM GCMRead WHERE ts=" + timestamp
					+ " LIMIT 1";
			Cursor cursor = db.rawQuery(sql, null);
			GCMRead data = null;
			if (cursor.moveToFirst()) {
				long ts = cursor.getLong(1);
				long readTs = cursor.getLong(2);
				String message = cursor.getString(3);
				data = new GCMRead(ts, readTs, message, false);
			}
			cursor.close();
			db.close();
			return data;
		}
	}

	/**
	 * Insert a GCMRead received from the server
	 * 
	 * @param data
	 *            received GCMRead
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public void insertGCMRead(GCMRead data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();

			ContentValues content = new ContentValues();
			content.put("ts", data.getTv().getTimestamp());
			content.put("message", data.getMessage());
			content.put("read", data.isRead() ? 1 : 0);
			db.insert("GCMRead", null, content);
			db.close();
		}
	}

	/**
	 * Set a GCMRead is read by the user
	 * 
	 * @param data
	 *            GCMRead read by the user
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public void readGCMRead(GCMRead data) {
		synchronized (sqlLock) {
			String sql;
			db = dbHelper.getWritableDatabase();
			sql = "UPDATE GCMRead SET  read = 1, readTs ="
					+ data.getReadTv().getTimestamp() + " WHERE ts="
					+ data.getTv().getTimestamp();
			db.execSQL(sql);
			db.close();
		}
	}

	/**
	 * Get all GCMRead results which are not read by the user
	 * 
	 * @return An array of GCMRead. If there are no GCMRead, return null.
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public GCMRead[] getNotReadGCMRead() {
		synchronized (sqlLock) {
			GCMRead[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			long ts_lower_bound = System.currentTimeMillis() - 7 * 24 * 60 * 60
					* 1000;
			sql = "SELECT * FROM GCMRead WHERE read = 0 AND ts>"
					+ ts_lower_bound;
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new GCMRead[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(1);
				long readTs = cursor.getLong(2);
				String message = cursor.getString(3);
				data[i] = new GCMRead(ts, readTs, message, false);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Get all GCMRead results which are read by the user but not uploaded to
	 * the server
	 * 
	 * @return An array of GCMRead. If there are no GCMRead, return null.
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public GCMRead[] getNotUploadedGCMRead() {
		synchronized (sqlLock) {
			GCMRead[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM GCMRead WHERE read = 1 AND upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new GCMRead[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(1);
				long readTs = cursor.getLong(2);
				String message = cursor.getString(3);
				data[i] = new GCMRead(ts, readTs, message, false);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the GCMRead uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded GCMRead
	 * @see ubicomp.ketdiary.data.structure.GCMRead
	 */
	public void setGCMReadUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE GCMRead SET upload = 1 WHERE ts = " + ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// FacebookInfo

	/**
	 * Get the latest FacebookInfo result
	 * 
	 * @return FacebookInfo. If there are no FacebookInfo, return a dummy data.
	 * @see ubicomp.ketdiary.data.structure.FacebookInfo
	 */
	public FacebookInfo getLatestFacebookInfo() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM  FacebookInfo WHERE addedScore = 1 ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new FacebookInfo(0, 0, 0, "", false, false, 0, 0);
			}
			long ts = cursor.getLong(4);
			int pageWeek = cursor.getInt(7);
			int pageLevel = cursor.getInt(8);
			String text = cursor.getString(9);
			boolean addedScore = (cursor.getInt(10) == 1);
			boolean uploadSuccess = (cursor.getInt(11) == 1);
			int privacy = cursor.getInt(12);
			int score = cursor.getInt(13);
			return new FacebookInfo(ts, pageWeek, pageLevel, text, addedScore,
					uploadSuccess, privacy, score);
		}
	}

	/**
	 * Insert a FacebookInfo result
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            inserted FacebookInfo
	 * @see ubicomp.ketdiary.data.structure.FacebookInfo
	 */
	public int insertFacebookInfo(FacebookInfo data) {
		synchronized (sqlLock) {
			FacebookInfo prev_data = getLatestFacebookInfo();
			int addScore = 0;
			if (data.getTv().afterAWeek(prev_data.getTv())
					&& data.isUploadSuccess())
				addScore = 1;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;

			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("year", data.getTv().getYear());
			content.put("month", data.getTv().getMonth());
			content.put("day", data.getTv().getDay());
			content.put("ts", data.getTv().getTimestamp());
			content.put("week", data.getTv().getWeek());
			content.put("timeslot", data.getTv().getTimeslot());
			content.put("pageWeek", data.getPageWeek());
			content.put("pageLevel", data.getPageLevel());
			content.put("text", data.getText());
			content.put("addedScore", addScore);
			content.put("uploadSuccess", data.isUploadSuccess() ? 1 : 0);
			content.put("privacy", data.getPrivacy());
			content.put("score", prev_data.getScore() + addScore);
			db.insert("FacebookInfo", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all FacebookInfo results which are not uploaded to the server
	 * 
	 * @return An array of FacebookInfo. If there are no FacebookInfo, return
	 *         null.
	 * @see ubicomp.ketdiary.data.structure.FacebookInfo
	 */
	public FacebookInfo[] getNotUploadedFacebookInfo() {
		synchronized (sqlLock) {
			FacebookInfo[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM FacebookInfo WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new FacebookInfo[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				int pageWeek = cursor.getInt(7);
				int pageLevel = cursor.getInt(8);
				String text = cursor.getString(9);
				boolean addedScore = (cursor.getInt(10) == 1);
				boolean uploadSuccess = (cursor.getInt(11) == 1);
				int privacy = cursor.getInt(12);
				int score = cursor.getInt(13);
				data[i] = new FacebookInfo(ts, pageWeek, pageLevel, text,
						addedScore, uploadSuccess, privacy, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the FacebookInfo result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded FacebookInfo
	 * @see ubicomp.ketdiary.data.structure.FacebookInfo
	 */
	public void setFacebookInfoUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE FacebookInfo SET upload = 1 WHERE ts = " + ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// Additional Questionnaire

	/**
	 * Get the latest AdditionalQuestionnaire result
	 * 
	 * @return AdditionalQuestionnaire. If there are no AdditionalQuestionnaire,
	 *         return a dummy data.
	 * @see ubicomp.ketdiary.data.structure.AdditionalQuestionnaire
	 */
	public AdditionalQuestionnaire getLatestAdditionalQuestionnaire() {
		synchronized (sqlLock) {
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM  AdditionalQuestionnaire ORDER BY ts DESC LIMIT 1";
			cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				cursor.close();
				db.close();
				return new AdditionalQuestionnaire(0, false, 0, 0, 0);
			}
			long ts = cursor.getLong(4);
			boolean addedScore = (cursor.getInt(7) == 1);
			int emotion = cursor.getInt(8);
			int craving = cursor.getInt(9);
			int score = cursor.getInt(10);
			return new AdditionalQuestionnaire(ts, addedScore, emotion,
					craving, score);
		}
	}

	/**
	 * Insert a AdditionalQuestionnaire result
	 * 
	 * @return # of credits got by the user
	 * @param data
	 *            inserted AdditionalQuestionnaire
	 * @see ubicomp.ketdiary.data.structure.AdditionalQuestionnaire
	 */
	public int insertAdditionalQuestionnaire(AdditionalQuestionnaire data) {
		synchronized (sqlLock) {
			AdditionalQuestionnaire prev_data = getLatestAdditionalQuestionnaire();
			int addScore = data.isAddedScore() ? 1 : 0;
			if (!StartDateCheck.afterStartDate())
				addScore = 0;
			if (prev_data.getTv().isSameDay(data.getTv()))
				addScore = 0;

			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("year", data.getTv().getYear());
			content.put("month", data.getTv().getMonth());
			content.put("day", data.getTv().getDay());
			content.put("ts", data.getTv().getTimestamp());
			content.put("week", data.getTv().getWeek());
			content.put("timeSlot", data.getTv().getTimeslot());
			content.put("addedScore", addScore);
			content.put("emotion", data.getEmotion());
			content.put("craving", data.getCraving());
			content.put("score", prev_data.getScore() + addScore);
			db.insert("AdditionalQuestionnaire", null, content);
			db.close();
			return addScore;
		}
	}

	/**
	 * Get all AdditionalQuestionnaire results which are not uploaded to the
	 * server
	 * 
	 * @return An array of AdditionalQuestionnaire. If there are no
	 *         AdditionalQuestionnaire, return null.
	 * @see ubicomp.ketdiary.data.structure.AdditionalQuestionnaire
	 */
	public AdditionalQuestionnaire[] getNotUploadedAdditionalQuestionnaire() {
		synchronized (sqlLock) {
			AdditionalQuestionnaire[] data = null;

			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;

			sql = "SELECT * FROM AdditionalQuestionnaire WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new AdditionalQuestionnaire[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(4);
				boolean addedScore = (cursor.getInt(7) == 1);
				int emotion = cursor.getInt(8);
				int craving = cursor.getInt(9);
				int score = cursor.getInt(10);
				data[i] = new AdditionalQuestionnaire(ts, addedScore, emotion,
						craving, score);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the AdditionalQuestionnaire result uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded AdditionalQuestionnaire
	 * @see ubicomp.ketdiary.data.structure.AdditionalQuestionnaire
	 */
	public void setAdditionalQuestionnaireUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE AdditionalQuestionnaire SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// UserVoiceFeedback

	/**
	 * Insert a UserVoiceFeedback result collected when user retry BrAC test
	 * 
	 * @param data
	 *            inserted UserVoiceFeedback
	 * @see ubicomp.ketdiary.data.structure.UserVoiceFeedback
	 */
	public void insertUserVoiceFeedback(UserVoiceFeedback data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("ts", data.getTv().getTimestamp());
			content.put("detectionTs", data.getDetectionTv().getTimestamp());
			content.put("testSuccess", data.isTestSuccess() ? 1 : 0);
			content.put("hasData", data.hasData() ? 1 : 0);
			db.insert("UserVoiceFeedback", null, content);
			db.close();
		}
	}

	/**
	 * Get all UserVoiceFeedback which are not uploaded to the server
	 * 
	 * @return An array of UserVoiceFeedback. If there are no UserVoiceFeedback,
	 *         return null.
	 * @see ubicomp.ketdiary.data.structure.UserVoiceFeedback
	 */
	public UserVoiceFeedback[] getNotUploadedUserVoiceFeedback() {
		synchronized (sqlLock) {
			UserVoiceFeedback[] data = null;
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			long cur_ts = System.currentTimeMillis();
			long gps_ts = PreferenceControl.getGPSStartTime()
					+ GPSService.GPS_TOTAL_TIME;

			if (cur_ts <= gps_ts) {
				long gps_detection_ts = PreferenceControl
						.getDetectionTimestamp();
				sql = "SELECT * FROM UserVoiceFeedback WHERE upload = 0 AND detectionTs <> "
						+ gps_detection_ts + " ORDER BY ts ASC";
			} else {
				sql = "SELECT * FROM UserVoiceFeedback WHERE upload = 0";
			}
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new UserVoiceFeedback[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(1);
				long detectionTs = cursor.getLong(2);
				boolean testSuccess = cursor.getInt(cursor
						.getColumnIndex("testSuccess")) == 1;
				boolean hasData = cursor.getInt(cursor
						.getColumnIndex("hasData")) == 1;
				data[i] = new UserVoiceFeedback(ts, detectionTs, testSuccess,
						hasData);
			}

			cursor.close();
			db.close();

			return data;
		}
	}

	/**
	 * Label the UserVoiceFeedback uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded UserVoiceFeedback
	 * @see ubicomp.ketdiary.data.structure.UserVoiceFeedback
	 */
	public void setUserVoiceFeedbackUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE UserVoiceFeedback SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// ExchangeHistory

	/**
	 * Insert a ExchangeHistory when the user exchanges credits for coupons
	 * 
	 * @param data
	 *            inserted ExchangeHistory
	 * @see ubicomp.ketdiary.data.structure.ExchangeHistory
	 */
	public void insertExchangeHistory(ExchangeHistory data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			ContentValues content = new ContentValues();
			content.put("ts", data.getTv().getTimestamp());
			content.put("exchangeCounter", data.getExchangeNum());
			db.insert("ExchangeHistory", null, content);
			db.close();
		}
	}

	/**
	 * Get all ExchangeHistory which are not uploaded to the server
	 * 
	 * @return An array of ExchangeHistory. If there are no ExchangeHistory,
	 *         return null.
	 * @see ubicomp.ketdiary.data.structure.ExchangeHistory
	 */
	public ExchangeHistory[] getNotUploadedExchangeHistory() {
		synchronized (sqlLock) {
			ExchangeHistory[] data = null;
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM ExchangeHistory WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new ExchangeHistory[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(1);
				int exchangeCounter = cursor.getInt(2);
				data[i] = new ExchangeHistory(ts, exchangeCounter);
			}
			cursor.close();
			db.close();
			return data;
		}
	}

	/**
	 * Label the ExchangeHistory uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded ExchangeHistory
	 * @see ubicomp.ketdiary.data.structure.ExchangeHistory
	 */
	public void setExchangeHistoryUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE ExchangeHistory SET upload = 1 WHERE ts = "
					+ ts;
			db.execSQL(sql);
			db.close();
		}
	}

	// BreathDetail

	/**
	 * Insert a BreathDetail recorded detailed information of breath condition
	 * when the user takes BrAC tests
	 * 
	 * @param data
	 *            inserted BreathDetail
	 * @see ubicomp.ketdiary.data.structure.BreathDetail
	 */
	public void insertBreathDetail(BreathDetail data) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();

			String sql = "SELECT * FROM BreathDetail WHERE ts ="
					+ data.getTv().getTimestamp();
			Cursor cursor = db.rawQuery(sql, null);
			if (!cursor.moveToFirst()) {
				ContentValues content = new ContentValues();
				content.put("ts", data.getTv().getTimestamp());
				content.put("blowStartTimes", data.getBlowStartTimes());
				content.put("blowBreakTimes", data.getBlowBreakTimes());
				content.put("pressureDiffMax", data.getPressureDiffMax());
				content.put("pressureMin", data.getPressureMin());
				content.put("pressureAverage", data.getPressureAverage());
				content.put("voltageInit", data.getVoltageInit());
				content.put("disconnectionMillis",
						data.getDisconnectionMillis());
				content.put("serialDiffMax", data.getSerialDiffMax());
				content.put("serialDiffAverage", data.getSerialDiffAverage());
				content.put("sensorId", data.getSensorId());
				db.insert("BreathDetail", null, content);
			}
			cursor.close();
			db.close();
		}
	}

	/**
	 * Get all BreathDetail which are not uploaded to the server
	 * 
	 * @return An array of BreathDetail. If there are no BreathDetail, return
	 *         null.
	 * @see ubicomp.ketdiary.data.structure.BreathDetail
	 */
	public BreathDetail[] getNotUploadedBreathDetail() {
		synchronized (sqlLock) {
			BreathDetail[] data = null;
			db = dbHelper.getReadableDatabase();
			String sql;
			Cursor cursor;
			sql = "SELECT * FROM BreathDetail WHERE upload = 0";
			cursor = db.rawQuery(sql, null);
			int count = cursor.getCount();
			if (count == 0) {
				cursor.close();
				db.close();
				return null;
			}

			data = new BreathDetail[count];

			for (int i = 0; i < count; ++i) {
				cursor.moveToPosition(i);
				long ts = cursor.getLong(1);
				int blowStartTimes = cursor.getInt(2);
				int blowBreakTimes = cursor.getInt(3);
				float pressureDiffMax = cursor.getFloat(4);
				float pressureMin = cursor.getFloat(5);
				float pressureAverage = cursor.getFloat(6);
				int voltageInit = cursor.getInt(7);
				long disconnectionMillis = cursor.getLong(8);
				int serialDiffMax = cursor.getInt(9);
				float serialDiffAverage = cursor.getFloat(10);
				String sensorId = cursor.getString(cursor
						.getColumnIndex("sensorId"));
				data[i] = new BreathDetail(ts, blowStartTimes, blowBreakTimes,
						pressureDiffMax, pressureMin, pressureAverage,
						voltageInit, disconnectionMillis, serialDiffMax,
						serialDiffAverage, sensorId);
			}
			cursor.close();
			db.close();
			return data;
		}
	}

	/**
	 * Label the BreathDetail uploaded
	 * 
	 * @param ts
	 *            Timestamp of the uploaded BreathDetail
	 * @see ubicomp.ketdiary.data.structure.BreathDetail
	 */
	public void setBreathDetailUploaded(long ts) {
		synchronized (sqlLock) {
			db = dbHelper.getWritableDatabase();
			String sql = "UPDATE BreathDetail SET upload = 1 WHERE ts = " + ts;
			db.execSQL(sql);
			db.close();
		}
	}
}
