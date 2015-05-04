package ubicomp.ketdiary.main;

import java.util.ArrayList;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.EmotionManagement;
import ubicomp.ketdiary.data.structure.TimeValue;
import ubicomp.ketdiary.main.ui.BarButtonGenerator;
import ubicomp.ketdiary.main.ui.ScreenSize;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

/**
 * Activity for showing the history of Emotion Management inserted by the user
 * 
 * @author Stanley Wang
 */
public class EmotionManageHistoryActivity extends Activity {

	private ArrayList<EmotionManagement> list;

	private LinearLayout titleLayout;
	private LinearLayout main;

	private static final int[] EMOTION_DRAWABLE_ID = { R.drawable.emotion_type_0, R.drawable.emotion_type_1,
			R.drawable.emotion_type_2, R.drawable.emotion_type_3, R.drawable.emotion_type_4, R.drawable.emotion_type_5,
			R.drawable.emotion_type_6, R.drawable.emotion_type_7, R.drawable.emotion_type_8, R.drawable.emotion_type_9, };

	private static final int[] EMOTION_VER1_ID = { R.drawable.emotion_ver1_0, R.drawable.emotion_ver1_1,
			R.drawable.emotion_ver1_2, R.drawable.emotion_ver1_3, R.drawable.emotion_ver1_4, R.drawable.emotion_ver1_5,
			R.drawable.emotion_ver1_6, };

	private DatabaseControl db;

	private long timeInMillis;
	private TimeValue curTV;
	private int prevPosition = -1;

	private static final int MIN_BARS = ScreenSize.getMinBars() - 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emotion_manage_history);

		timeInMillis = this.getIntent().getLongExtra("timeInMillis", System.currentTimeMillis());
		curTV = TimeValue.generate(timeInMillis);

		db = new DatabaseControl();
		titleLayout = (LinearLayout) this.findViewById(R.id.emotion_manage_title_layout);
		main = (LinearLayout) this.findViewById(R.id.emotion_manage_main_layout);

		View title = BarButtonGenerator.createTitleView(getString(R.string.emotion_manage_history_title) + "��"
				+ curTV.toSimpleDateString());
		titleLayout.addView(title);

	}

	@Override
	protected void onResume() {
		super.onResume();
		ClickLog.Log(ClickLogId.EMOTION_MANAGE_HISTORY_ENTER);
		setQuestionEmotion();
	}

	@Override
	protected void onPause() {
		ClickLog.Log(ClickLogId.EMOTION_MANAGE_HISTORY_LEAVE);
		super.onPause();
	}

	private void setQuestionEmotion() {
		main.removeAllViewsInLayout();

		EmotionManagement[] ems = db.getDayEmotionManagement(curTV.getYear(), curTV.getMonth(), curTV.getDay());

		if (titleLayout.getChildCount() > 1)
			titleLayout.removeViewAt(1);
		View tv = BarButtonGenerator.createTextView(R.string.emotion_manage_history_help);
		titleLayout.addView(tv);

		list = new ArrayList<EmotionManagement>();

		if (ems == null)
			return;
		for (int i = 0; i < ems.length; ++i) {
			list.add(ems[i]);
			main.addView(createItem(ems[i], i));
		}

		int from = main.getChildCount();
		for (int i = from; i < MIN_BARS; ++i) {
			View v = BarButtonGenerator.createBlankView();
			main.addView(v);
		}
	}

	private View createItem(EmotionManagement em, int idx) {
		if (em.getEmotion() < 100)
			return BarButtonGenerator.createIconViewInverse(em.getReason(), EMOTION_DRAWABLE_ID[em.getEmotion()],
					new CustomOnItemSelectListener(idx));
		else
			return BarButtonGenerator.createIconViewInverse(em.getReason(), EMOTION_VER1_ID[em.getEmotion() - 100],
					new CustomOnItemSelectListener(idx));
	}

	private View selectItem(EmotionManagement em) {
		if (em.getEmotion() < 100)
			return BarButtonGenerator.createTextAreaViewInverse(em.getReason(), EMOTION_DRAWABLE_ID[em.getEmotion()]);
		else
			return BarButtonGenerator.createTextAreaViewInverse(em.getReason(), EMOTION_VER1_ID[em.getEmotion() - 100]);
	}

	private class CustomOnItemSelectListener implements OnClickListener {

		private int position;

		public CustomOnItemSelectListener(int pos) {
			this.position = pos;
		}

		@Override
		public void onClick(View v) {
			if (prevPosition == position)
				return;
			ClickLog.Log(ClickLogId.EMOTION_MANAGE_HISTORY_SELECT);
			if (prevPosition > -1) {
				main.removeViewAt(prevPosition);
				main.addView(createItem(list.get(prevPosition), prevPosition), prevPosition);
			}
			main.removeView(v);
			main.addView(selectItem(list.get(position)), position);
			prevPosition = position;
		}
	}
}
