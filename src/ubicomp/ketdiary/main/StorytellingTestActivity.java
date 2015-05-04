package ubicomp.ketdiary.main;

import java.util.Random;

import ubicomp.ketdiary.data.database.DatabaseControl;
import ubicomp.ketdiary.data.structure.StorytellingTest;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.BarButtonGenerator;
import ubicomp.ketdiary.main.ui.ScreenSize;
import ubicomp.ketdiary.main.ui.Typefaces;
import ubicomp.ketdiary.main.ui.toast.CustomToast;
import ubicomp.ketdiary.main.ui.toast.CustomToastSmall;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.config.PreferenceControl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;

/**
 * Activity for storytelling test function
 * 
 * @author Stanley Wang
 */
public class StorytellingTestActivity extends Activity {

	private LinearLayout inputLayout;

	private LayoutInflater inflater;

	private TextView titleText;
	private Typeface wordTypefaceBold;

	private int imageWeek;

	private String question = "";
	private String answer = "";
	private String selectedAnswer = "";

	private RadioButton[] selections = new RadioButton[3];

	private SeekBar agreementSeekbar;
	private TextView agreementText;
	private String[] agreementLevel;

	private static final String[] labels = { "A.", "B.", "C." };

	private static final int MIN_BARS = ScreenSize.getMinBars();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_storytelling_test);

		Bundle data = this.getIntent().getExtras();
		imageWeek = data.getInt("image_week", 0);

		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		wordTypefaceBold = Typefaces.getWordTypefaceBold();

		titleText = (TextView) this.findViewById(R.id.st_title);
		inputLayout = (LinearLayout) this.findViewById(R.id.st_input_layout);
		titleText.setTypeface(wordTypefaceBold);

		View messageView = BarButtonGenerator.createTextView(R.string.storytelling_test_question);
		inputLayout.addView(messageView);
		String[] selections = settingQuestion();

		View questionView = BarButtonGenerator.createQuoteQuestionView(question);
		inputLayout.addView(questionView);

		View selectionView = createSelectionView(selections);
		inputLayout.addView(selectionView);

		View agreeTextView = BarButtonGenerator.createTextView(R.string.storytelling_test_agreement);
		inputLayout.addView(agreeTextView);

		View agreementView = createSeekBarView();
		inputLayout.addView(agreementView);

		View submitView = BarButtonGenerator.createTwoButtonView(R.string.story_test_cancel, R.string.story_test_ok,
				new CancelOnClickListener(), new SubmitOnClickListener());

		inputLayout.addView(submitView);

		int from = inputLayout.getChildCount();
		for (int i = from; i < MIN_BARS; ++i) {
			View v = BarButtonGenerator.createBlankView();
			inputLayout.addView(v);
		}
	}

	private String[] settingQuestion() {
		String[] questions = null;
		String[] answers = null;
		Resources r = App.getContext().getResources();
		int questionWeek = imageWeek % 12;
		switch (questionWeek) {
		case 0:
			questions = r.getStringArray(R.array.quote_question_0);
			answers = r.getStringArray(R.array.quote_answer_0);
			break;
		case 1:
			questions = r.getStringArray(R.array.quote_question_1);
			answers = r.getStringArray(R.array.quote_answer_1);
			break;
		case 2:
			questions = r.getStringArray(R.array.quote_question_2);
			answers = r.getStringArray(R.array.quote_answer_2);
			break;
		case 3:
			questions = r.getStringArray(R.array.quote_question_3);
			answers = r.getStringArray(R.array.quote_answer_3);
			break;
		case 4:
			questions = r.getStringArray(R.array.quote_question_4);
			answers = r.getStringArray(R.array.quote_answer_4);
			break;
		case 5:
			questions = r.getStringArray(R.array.quote_question_5);
			answers = r.getStringArray(R.array.quote_answer_5);
			break;
		case 6:
			questions = r.getStringArray(R.array.quote_question_6);
			answers = r.getStringArray(R.array.quote_answer_6);
			break;
		case 7:
			questions = r.getStringArray(R.array.quote_question_7);
			answers = r.getStringArray(R.array.quote_answer_7);
			break;
		case 8:
			questions = r.getStringArray(R.array.quote_question_8);
			answers = r.getStringArray(R.array.quote_answer_8);
			break;
		case 9:
			questions = r.getStringArray(R.array.quote_question_9);
			answers = r.getStringArray(R.array.quote_answer_9);
			break;
		case 10:
			questions = r.getStringArray(R.array.quote_question_10);
			answers = r.getStringArray(R.array.quote_answer_10);
			break;
		case 11:
			questions = r.getStringArray(R.array.quote_question_11);
			answers = r.getStringArray(R.array.quote_answer_11);
			break;
		default:
			questions = r.getStringArray(R.array.quote_question_0);
			answers = r.getStringArray(R.array.quote_answer_0);
			break;
		}

		Random rand = new Random();
		int qid = rand.nextInt(3);
		question = questions[qid];
		answer = new String(answers[qid * 5]);

		String[] tempSelection = new String[4];
		for (int i = 0; i < tempSelection.length; ++i)
			tempSelection[i] = answers[qid * 5 + i + 1];
		shuffleArray(tempSelection);
		String[] selectAns = new String[3];
		for (int i = 0; i < selectAns.length; ++i)
			selectAns[i] = tempSelection[i];

		int ans_id = rand.nextInt(selectAns.length);
		selectAns[ans_id] = answer;

		return selectAns;
	}

	private static void shuffleArray(String[] ar) {
		Random rnd = new Random();
		for (int i = ar.length - 1; i > 0; --i) {
			int index = rnd.nextInt(i + 1);
			String a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	private View createSelectionView(String[] selectionStrs) {
		FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.bar_multi_select_item, null);
		selections[0] = (RadioButton) layout.findViewById(R.id.question_select0);
		selections[1] = (RadioButton) layout.findViewById(R.id.question_select1);
		selections[2] = (RadioButton) layout.findViewById(R.id.question_select2);

		for (int i = 0; i < selectionStrs.length; ++i) {
			selections[i].setText(labels[i] + selectionStrs[i]);
			selections[i].setOnCheckedChangeListener(new SelectionChangeListener());
			selections[i].setTypeface(wordTypefaceBold);
		}

		return layout;
	}

	private boolean agreementChange = false;

	private View createSeekBarView() {
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.bar_seekbar_item, null);
		agreementSeekbar = (SeekBar) layout.findViewById(R.id.question_seek_bar);
		agreementText = (TextView) layout.findViewById(R.id.question_seekbar_message);
		agreementLevel = App.getContext().getResources().getStringArray(R.array.agreement);
		agreementText.setText(agreementLevel[2]);
		agreementText.setTypeface(wordTypefaceBold);
		agreementSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				agreementText.setText(agreementLevel[arg1]);
				agreementText.invalidate();
				agreementChange = true;
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});
		return layout;
	}

	/*
	 * private class SelectionChangeListener implements
	 * RadioGroup.OnCheckedChangeListener {
	 * 
	 * @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
	 * ClickLog.Log(ClickLogId.STORYTELLING_TEST_SELECT); TextView tv =
	 * (TextView) group.findViewById(checkedId); selectedAnswer =
	 * tv.getText().toString().substring(2); } }
	 */

	private class SelectionChangeListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				ClickLog.Log(ClickLogId.STORYTELLING_TEST_SELECT);
				for (int i = 0; i < selections.length; ++i)
					if (selections[i].getId() != buttonView.getId())
						selections[i].setChecked(false);
				selectedAnswer = buttonView.getText().toString().substring(2);
			}
		}
	}

	private class CancelOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			ClickLog.Log(ClickLogId.STORYTELLING_TEST_CANCEL);
			finish();
		}
	}

	private class SubmitOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (selectedAnswer == null || selectedAnswer.length() == 0)
				CustomToastSmall.generateToast(R.string.storytelling_test_toast);
			else {
				boolean isCorrect = false;
				int agreement = agreementSeekbar.getProgress();
				if (selectedAnswer.equals(answer))
					isCorrect = true;
				DatabaseControl db = new DatabaseControl();
				int addScore = db.insertStorytellingTest(new StorytellingTest(System.currentTimeMillis(), imageWeek,
						isCorrect, selectedAnswer, agreement, 0));
				if (!isCorrect)
					CustomToast.generateToast(R.string.storytelling_test_incorrect, -1);
				else {
					if (PreferenceControl.checkCouponChange())
						PreferenceControl.setCouponChange(true);
					CustomToast.generateToast(R.string.storytelling_test_correct, addScore);
				}
				if (agreementChange)
					ClickLog.Log(ClickLogId.STORYTELLING_TEST_SUBMIT);
				else
					ClickLog.Log(ClickLogId.STORYTELLING_TEST_SUBMIT_EMPTY);
				finish();
			}

		}
	}

	@Override
	protected void onResume() {
		agreementChange = false;
		super.onResume();
		ClickLog.Log(ClickLogId.STORYTELLING_TEST_ENTER);
	}

	@Override
	protected void onPause() {
		ClickLog.Log(ClickLogId.STORYTELLING_TEST_LEAVE);
		super.onPause();
	}

}
