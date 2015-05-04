package ubicomp.ketdiary.main.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;

import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.main.ui.LoadingDialogControl;
import ubicomp.ketdiary.system.clicklog.ClickLogId;
import ubicomp.ketdiary.system.clicklog.ClickLog;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StatisticFragment extends Fragment {

	private View view;
	private Activity activity;

	// Test Time
	private TextView dateOfTestTime;
	private TextView timeOfTestTime;

	private TextView showResult;
	private ImageView showResultBG;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = getActivity();
		
		// Dismiss the progress circle
		LoadingDialogControl.dismiss();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_statistic, container, false);

		dateOfTestTime = (TextView) view.findViewById(R.id.test_time_date);
		SimpleDateFormat formatterDate = new SimpleDateFormat("M月d日");
		Date curDate = new Date(System.currentTimeMillis());
		dateOfTestTime.setText( formatterDate.format(curDate) );
		
		timeOfTestTime = (TextView) view.findViewById(R.id.test_time_time);
		SimpleDateFormat formatterTime = new SimpleDateFormat("h:mm a");
		Date curTime = new Date(System.currentTimeMillis());
		timeOfTestTime.setText( formatterTime.format(curTime) );
		
		

		showResult = (TextView) view.findViewById(R.id.show_result);
		showResultBG = (ImageView) view.findViewById(R.id.show_result_bg);
		
		

		return view;
	}

	public void onResume() {
		super.onResume();
		
		ClickLog.Log(ClickLogId.STATISTIC_ENTER);
		
		LoadingDialogControl.dismiss();

		if(MainActivity.getMainActivity().getTestResult() != null)
			showResult.setText( MainActivity.getMainActivity().getTestResult() );
		if(showResult.getText().equals("陰性")){
			showResultBG.setImageResource(R.drawable.statistic_day_main_circle_pass);
		}
	}

	public void onPause() {
		
		ClickLog.Log(ClickLogId.STATISTIC_LEAVE);
		super.onPause();
	}

	

	

//	@SuppressLint("HandlerLeak")
//	private class LoadingHandler extends Handler {
//		public void handleMessage(Message msg) {
//			MainActivity.getMainActivity().enableTabAndClick(false);
//			statisticView.setAdapter(statisticViewAdapter);
//			statisticView.setOnPageChangeListener(new StatisticOnPageChangeListener());
//			statisticView.setSelected(true);
//			analysisLayout.removeAllViews();
//
//			questionButton.setOnClickListener(new QuestionOnClickListener());
//			for (int i = 0; i < analysisViews.length; ++i)
//				if (analysisViews[i] != null)
//					analysisLayout.addView(analysisViews[i].getView());
//
//			statisticViewAdapter.load();
//			for (int i = 0; i < analysisViews.length; ++i)
//				if (analysisViews[i] != null)
//					analysisViews[i].load();
//
//			statisticView.setCurrentItem(0);
//
//			for (int i = 0; i < 3; ++i)
//				dots[i].setImageDrawable(dot_off);
//			dots[0].setImageDrawable(dot_on);
//
//			if (msgBox != null)
//				msgBox.initialize();
//
//			questionAnimation = new AlphaAnimation(1.0F, 0.0F);
//			questionAnimation.setDuration(200);
//			questionAnimation.setRepeatCount(Animation.INFINITE);
//			questionAnimation.setRepeatMode(Animation.REVERSE);
//
//			setQuestionAnimation();
//
//			MainActivity.getMainActivity().enableTabAndClick(true);
//			LoadingDialogControl.dismiss();
//			
//			if (notify_action == MainActivity.ACTION_QUESTIONNAIRE){
//				openQuestionnaire();
//				notify_action = 0;
//			}
//		}
//	}

	
	
	


}
