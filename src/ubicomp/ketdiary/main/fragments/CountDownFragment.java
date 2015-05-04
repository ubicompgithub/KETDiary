package ubicomp.ketdiary.main.fragments;

import java.util.Timer;
import java.util.TimerTask;

import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CountDownFragment extends Fragment {

	private View view;
	private Timer timerForWaitResult = new Timer();
	private int waitResultCountDown = 10;

	private TextView countDownText;

	private ImageView alarmClock;
	private boolean isAlarmed = true;

	private Button addNoteOKButton, addNoteCancelButton;

	private LinearLayout infoLinearLayout;
	private View subscreenInformation;
	private LinearLayout.LayoutParams linearLayoutParams;

	private Activity mActivity;

	private TimerTask taskWaitResult = new TimerTask(){
		@Override
		public void run() {
			Message message = new Message();
			message.what = 1;
			handlerForWaitResult.sendMessage(message);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler handlerForWaitResult = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if(waitResultCountDown >= 0){
				String countDownTimeStr = (waitResultCountDown%60 >= 10)?
				(int)waitResultCountDown/60 + ":" + waitResultCountDown%60:
				(int)waitResultCountDown/60 + ":0" + waitResultCountDown%60;

				countDownText.setText( countDownTimeStr );
				waitResultCountDown--;
			}
			else{
				Toast toast = Toast.makeText(mActivity, "檢測完成", Toast.LENGTH_SHORT);
				toast.show();
				MainActivity.getMainActivity().changeTab(1);
				MainActivity.getMainActivity().resetTestFragmentAfterCountDown();
				timerForWaitResult.cancel();
			}
		}
	};




	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		view = inflater.inflate(R.layout.fragment_countdown_page, container, false);
		
		countDownText = (TextView) view.findViewById(R.id.countdown_text);
		alarmClock = (ImageView) view.findViewById(R.id.alarm_clock_button);
		alarmClock.setOnClickListener(new AlarmClockOnClickListener());
		timerForWaitResult.scheduleAtFixedRate(taskWaitResult, 0, 1000);

		addNoteOKButton = (Button) view.findViewById(R.id.add_note_ok);
		addNoteOKButton.setOnClickListener(new OKButtonOnClickListener());
		addNoteCancelButton = (Button) view.findViewById(R.id.add_note_cancel);
		addNoteCancelButton.setOnClickListener(new CancelButtonOnClickListener());


		infoLinearLayout = (LinearLayout) view.findViewById(R.id.informationLinearLayout);
		subscreenInformation = inflater.inflate(R.layout.subscreen_information, null);

		return view;
	}
	
	private class AlarmClockOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if(isAlarmed){
				alarmClock.setImageResource(R.drawable.alarm_clock_off);
				isAlarmed = false;
			}
			else{
				alarmClock.setImageResource(R.drawable.alarm_clock_on);
				isAlarmed = true;
			}
			
		}
	}

	private class OKButtonOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switchToInfomation();
			Toast toast = Toast.makeText(mActivity, "完成記事", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private class CancelButtonOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switchToInfomation();
			Toast toast = Toast.makeText(mActivity, "取消記事", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void switchToInfomation(){
		linearLayoutParams
		= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

		infoLinearLayout.removeAllViews();
		infoLinearLayout.addView(subscreenInformation, linearLayoutParams);
	}

}
