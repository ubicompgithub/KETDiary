package ubicomp.ketdiary.data.database;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ubicomp.ketdiary.data.file.MainStorage;
import ubicomp.ketdiary.data.structure.AdditionalQuestionnaire;
import ubicomp.ketdiary.data.structure.Detection;
import ubicomp.ketdiary.data.structure.EmotionDIY;
import ubicomp.ketdiary.data.structure.EmotionManagement;
import ubicomp.ketdiary.data.structure.FacebookInfo;
import ubicomp.ketdiary.data.structure.Questionnaire;
import ubicomp.ketdiary.data.structure.StorytellingRead;
import ubicomp.ketdiary.data.structure.StorytellingTest;
import ubicomp.ketdiary.data.structure.UserVoiceRecord;
import ubicomp.ketdiary.main.PreSettingActivity;
import ubicomp.ketdiary.system.config.PreferenceControl;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class is an AsyncTask for handling Database restore procedure
 * 
 * @author Stanley Wang
 * @see ubicomp.ketdiary.data.database.DatabaseRestoreControl
 */
public class DatabaseRestore extends AsyncTask<Void, Void, Void> {

	private String uid;
	private File dir;
	private File zipFile;
	private Context context;

	private boolean hasFile = false;
	private DatabaseRestoreControl db = new DatabaseRestoreControl();

	private static final String TAG = "RESTORE";
	private ProgressDialog dialog = null;

	/**
	 * Constructor
	 * 
	 * @param uid
	 *            UserId
	 * @param context
	 *            Context of the Activity
	 */
	public DatabaseRestore(String uid, Context context) {
		this.uid = uid;
		this.context = context;

		dir = MainStorage.getMainStorageDirectory();
		zipFile = new File(dir, uid + ".zip");
		hasFile = zipFile.exists();
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setMessage("Please Wait...");
		dialog.setCancelable(false);
		dialog.show();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		if (hasFile) {
			unzip();
			db.deleteAll();

			restoreAlcoholic();
			restoreDetection();

			restoreEmotionDIY();
			restoreQuestionnaire();

			restoreEmotionManagement();
			restoreUserVoiceRecord();
			restoreAdditionalQuestionnaire();

			restoreStorytellingRead();
			restoreStorytellingTest();
			restoreFacebookInfo();

		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (dialog != null)
			dialog.dismiss();
		Intent intent = new Intent(context, PreSettingActivity.class);
		context.startActivity(intent);
	}

	/** Unzip the backup file */
	private void unzip() {
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.isDirectory()) {
					File d = new File(dir + "/" + ze.getName());
					d.mkdirs();
				} else {
					File outFile = new File(dir, ze.getName());
					FileOutputStream fout = new FileOutputStream(outFile);
					for (int c = zin.read(); c != -1; c = zin.read())
						fout.write(c);
					zin.closeEntry();
					fout.close();
				}
			}
			zin.close();
		} catch (Exception e) {
			Log.d(TAG, "EXECEPTION: " + e.getMessage());
		}
	}

	/**
	 * Restore Table Alcoholic related data Set user ID (UID), start date, and
	 * the # of self-help counters exchanged for coupons
	 */
	private void restoreAlcoholic() {
		String filename = "alcoholic";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No Alcoholic");
				else {

					PreferenceControl.setUID(uid);

					str = reader.readLine();
					String[] data = str.split(",");

					String[] dateInfo = data[1].split("-");
					int year = Integer.valueOf(dateInfo[0]);
					int month = Integer.valueOf(dateInfo[1]) - 1;
					int day = Integer.valueOf(dateInfo[2]);

					PreferenceControl.setStartDate(year, month, day);

					int usedScore = Integer.valueOf(data[2]);
					PreferenceControl.setUsedCounter(usedScore);
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/** Restore from the table Detection */
	private void restoreDetection() {
		String filename = "detection";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);
						float brac = Float.valueOf(data[1]);
						int emotion = Integer.valueOf(data[2]);
						int craving = Integer.valueOf(data[3]);
						boolean isPrime = Integer.valueOf(data[4]) == 1;
						int weeklyScore = Integer.valueOf(data[5]);
						int score = Integer.valueOf(data[6]);

						Detection detection = new Detection(brac, timestamp, emotion, craving, isPrime, weeklyScore,
								score);
						db.restoreDetection(detection);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table Emotion DIY (Only restored # of self-help counters
	 * got by the user)
	 */
	private void restoreEmotionDIY() {
		String filename = "emotiondiy";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);
						int score = Integer.valueOf(data[1]);
						EmotionDIY emotionDIY = new EmotionDIY(timestamp, -1, "", score);
						db.restoreEmotionDIY(emotionDIY);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table Questionnaire (Only restore the # self-help
	 * counters got by the user)
	 */
	private void restoreQuestionnaire() {
		String filename = "questionnaire";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);
						int score = Integer.valueOf(data[1]);
						Questionnaire questionnaire = new Questionnaire(timestamp, 0, "", score);
						db.restoreQuestionnaire(questionnaire);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/** Restore from the table Emotion Management */
	private void restoreEmotionManagement() {
		String filename = "emotionmanage";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						String[] dateInfo = data[1].split("-");
						int year = Integer.valueOf(dateInfo[0]);
						int month = Integer.valueOf(dateInfo[1]) - 1;
						int day = Integer.valueOf(dateInfo[2]);

						int emotion = Integer.valueOf(data[2]);
						int reasonType = Integer.valueOf(data[3]);
						int score = Integer.valueOf(data[4]);

						StringBuilder sb = new StringBuilder();

						sb.append(data[5]);
						for (int i = 6; i < data.length; ++i) {
							sb.append(",");
							sb.append(data[i]);
						}
						String reason = sb.toString();

						EmotionManagement emotionManagement = new EmotionManagement(timestamp, year, month, day,
								emotion, reasonType, reason, score);
						db.restoreEmotionManagement(emotionManagement);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table UserVoiceRecord and copy the audio files into the
	 * corresponded directory
	 */
	private void restoreUserVoiceRecord() {
		String filename = "storyrecord";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						String[] dateInfo = data[1].split("-");
						int year = Integer.valueOf(dateInfo[0]);
						int month = Integer.valueOf(dateInfo[1]) - 1;
						int day = Integer.valueOf(dateInfo[2]);

						int score = Integer.valueOf(data[2]);

						UserVoiceRecord uvr = new UserVoiceRecord(timestamp, year, month, day, score);
						db.restoreUserVoiceRecord(uvr);

						File src = new File(dir + "/" + uid + "/audio_records/" + uvr.getRecordTv().toFileString()
								+ ".3gp");
						File audio_dir = new File(dir + "/audio_records");
						if (!audio_dir.exists())
							audio_dir.mkdirs();
						File dst = new File(audio_dir + "/" + uvr.getRecordTv().toFileString() + ".3gp");
						moveFiles(src, dst);

					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}

	}

	private void moveFiles(File src, File dst) {
		InputStream in;
		try {
			in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0)
				out.write(buf, 0, len);
			in.close();
			out.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Restore from the table AdditionalQuestionnaire (Only restore the # of
	 * self-help counters got by the user)
	 */
	private void restoreAdditionalQuestionnaire() {
		String filename = "additional";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						int score = Integer.valueOf(data[1]);

						AdditionalQuestionnaire aq = new AdditionalQuestionnaire(timestamp, true, 0, 0, score);
						db.restoreAdditionalQuestionnaire(aq);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table Storytelling Read (Only restore the # of self-help
	 * counters got by the user)
	 */
	private void restoreStorytellingRead() {
		String filename = "storyread";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						int score = Integer.valueOf(data[1]);

						StorytellingRead sr = new StorytellingRead(timestamp, true, 0, score);
						db.restoreStorytellingRead(sr);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table Storytelling Test (Only restore the # of self-help
	 * counters got by the user)
	 */
	private void restoreStorytellingTest() {
		String filename = "storytest";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						int score = Integer.valueOf(data[1]);

						StorytellingTest st = new StorytellingTest(timestamp, 0, true, "", 0, score);
						db.restoreStorytellingTest(st);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}

	/**
	 * Restore from the table Facebook (Only restore the # of self-help
	 * counters got by the user)
	 */
	private void restoreFacebookInfo() {
		String filename = "facebook";
		File f = new File(dir + "/" + uid + "/" + filename + ".restore");
		if (f.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(
						new FileInputStream(f))));
				String str = reader.readLine();
				if (str == null)
					Log.d(TAG, "No " + filename);
				else {
					while ((str = reader.readLine()) != null) {
						String[] data = str.split(",");
						long timestamp = Long.valueOf(data[0]);

						int score = Integer.valueOf(data[1]);

						FacebookInfo fb = new FacebookInfo(timestamp, 0, 0, "", true, false, 0, score);
						db.restoreFacebookInfo(fb);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "NO " + filename);
			} catch (IOException e) {
				Log.d(TAG, "READ FAIL " + filename);
			}
		}
	}
}
