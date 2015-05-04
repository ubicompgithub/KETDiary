package ubicomp.ketdiary.main.fragments;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import libsvm.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ubicomp.ketdiary.main.MainActivity;
import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.test.bluetoothle.BluetoothLE;
import ubicomp.ketdiary.test.camera.CameraPreview;
import ubicomp.ketdiary.main.App;
import ubicomp.ketdiary.main.ui.LoadingDialogControl;



public class TestFragment extends Fragment implements SurfaceHolder.Callback{
	
	private View view;
	private ImageView startButton, informationButton, startButtonProgressBar;
	private TextView startText, accumulatedTimeText;
	private TextView testGuidanceUpper, testGuidanceLower;
	
	private Timer timerForPreparingSaliva = new Timer();
	private int prepareSalivaCountDown = 5;

	private Timer timerForTakePicture = new Timer();
	private int takePicturesCountDown = 60;
	private Camera mCamera = null;
	private CameraPreview mCamPreview;
	private FrameLayout cameraLayout;
	private ImageView startButtonProgressBarBg, cameraMask;

	private LinearLayout waitWindow;
	private Timer timerForDetectSaliva = new Timer();
	private int detectSalivaCountDown = 2;

	MainActivity mMainActivity;

	// Blue Zhong
	public static final int STATE_INIT = 0;
	private ImageView face;
	private boolean showCountDown = true;

	private boolean isTakingPictures = false;
	private int takingPicturesState = 0;
	private boolean isStateFD = false;
	private boolean isStateFE = false;
	private boolean isBleOn = false;
	
//	private BracValueFileHandler bracValueFileHandler;


	private TimerTask taskPreparingSaliva = new TimerTask(){
		@Override
		public void run() {
			Message message = new Message();
			message.what = 1;
			handlerForPreparingSaliva.sendMessage(message);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler handlerForPreparingSaliva = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if(prepareSalivaCountDown > 0){
				startText.setText(Integer.toString(prepareSalivaCountDown));
			}
			else{
				timerForPreparingSaliva.cancel();
			}
			if(prepareSalivaCountDown == 1){
				ble.writeProtocolCharacteristic2();
				ble.writeProtocolCharacteristic2();
				ble.writeProtocolCharacteristic2();
				ble.writeProtocolCharacteristic2();
				ble.writeProtocolCharacteristic2();
			}
			prepareSalivaCountDown--;
		}
	};


	private TimerTask taskTakingPictures = new TimerTask(){
		@Override
		public void run() {
			Message message = new Message();
			message.what = 1;
			handlerForTakingPictures.sendMessage(message);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler handlerForTakingPictures = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if( takePicturesCountDown > 0 ){
				if( isStateFD ){
					if(takingPicturesState == 0){
						startButtonProgressBar.setImageResource(R.drawable.test_progress_1);
						takingPicturesState++;
					}
					else if(takingPicturesState == 1){
						startButtonProgressBar.setImageResource(R.drawable.test_progress_2);
						takingPicturesState++;
					}
					else if(takingPicturesState == 2){
						startButtonProgressBar.setImageResource(R.drawable.test_progress_3);
						takingPicturesState++;
					}
					else if(takingPicturesState == 3){
						if( isStateFE ){
							startButtonProgressBar.setImageResource(R.drawable.test_progress_4);
							takingPicturesState++;
						}
					}
					else if(takingPicturesState == 4){
						startButtonProgressBar.setImageResource(R.drawable.test_progress_5);
						takingPicturesState++;

						Toast toast = Toast.makeText(getActivity(), "口水量足夠", Toast.LENGTH_SHORT);
						toast.show();
						// timerForDetectSaliva.scheduleAtFixedRate(taskDetectSaliva, 0, 1000);
						MainActivity.getMainActivity().switchToCountDownFragment();
					}
				}
				takePicturesCountDown--;
			}
			else{
				exceptionTestOvertime();
				timerForTakePicture.cancel();
			}
		}
	};
	
	
// 	private TimerTask taskDetectSaliva = new TimerTask(){
// 		@Override
// 		public void run() {
// 			Message message = new Message();
// 			message.what = 1;
// 			handlerForDetectSaliva.sendMessage(message);
// 		}
// 	};
	
// 	@SuppressLint("HandlerLeak")
// 	private Handler handlerForDetectSaliva = new Handler(){
// 		public void handleMessage(Message msg){
// 			super.handleMessage(msg);
// 			if(detectSalivaCountDown >= 0){
// 				detectSalivaCountDown--;
// 			}
// 			else{
				
// //				mMainActivity.switchToCountDownFragment();
				
// 				timerForDetectSaliva.cancel();
// 			}
// 		}
// 	};


	

	private static BluetoothLE ble = null;
	private boolean isFirstTimeConnect = true;
	private boolean isFirstTimeInsert = true;
	private boolean isWrite02Success = false;

	private TimerTask taskUpdateBleState = new TimerTask(){
		@Override
		public void run() {
			Message message = new Message();
			message.what = 1;
			handlerForUpdateBleState.sendMessage(message);
		}
	};

	private String currentDeviceState;
	private int currentDeviceProtocol = 0;
	private Timer timerForUpdateBleState = new Timer();
	@SuppressLint("HandlerLeak")
	private Handler handlerForUpdateBleState = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			if ( ble.isBleConnected() ){
				isBleOn = true;
				if (isFirstTimeConnect){
					testGuidanceLower.setText("檢測器連線成功");
					isFirstTimeConnect = false;
				}
				else{
					currentDeviceState = ble.getCurrentDeviceState();
					ble.readProtocolCharacteristic();

					if (currentDeviceState != null){
					
						if(currentDeviceState.equals("FA ")){
		                    // No connection
		                    testGuidanceLower.setText("試紙匣尚未插入檢測器");
		                    // Log.i("FORTEST", "## No connection!");
		                }
		                else if(currentDeviceState.equals("FB ")){
		                    // Connected
		                    // Log.i("FORTEST", "## Connected!");
		                    if(isFirstTimeInsert){
		                    	testGuidanceLower.setText("檢測器就緒");
		                    	// ble.writeProtocolCharacteristic2();
		                    	isFirstTimeInsert = false;
		                    	timerForPreparingSaliva.scheduleAtFixedRate(taskPreparingSaliva, 0, 1000);
		                    }
		                    else{
		                    	if( isWrite02Success == false ){
		                    		// ble.writeProtocolCharacteristic2();
		                    	}
		                    }
		                }
		                else if(currentDeviceState.equals("FC ")){
		                	isWrite02Success = true;
		                    // No saliva
		                	// initializeCamera();
		                    testGuidanceLower.setText("請吐入口水");
		                    // Log.i("FORTEST", "## No saliva!");
		                    if(!isTakingPictures){
		                    	takePicturesProcess();
		                    	isTakingPictures = true;
		                    }
		                }
		                else if(currentDeviceState.equals("FD ")){
		                    // 1 pass, 2 not yet
		                    testGuidanceLower.setText("完成第一階段擴散");
		                    isStateFD = true;
		                    // Log.i("FORTEST", "## 1 pass, 2 not yet!");
		                }
		                else if(currentDeviceState.equals("FE ")){
		                    // 1 pass, 2 pass
		                    testGuidanceLower.setText("完成第二階段擴散");
		                    isStateFE = true;
		                    // Log.i("FORTEST", "## 1 pass, 2 pass!");
		                    ble.writeProtocolCharacteristic3();
		                }
		                else if(currentDeviceState.equals("FF ")){
		                    // Color
		                    testGuidanceLower.setText("正在判斷結果中");
		                    // Log.i("FORTEST", "## Color!");
		                }

		            }
				}
			}
			else{
				Log.i("FORTEST", "Wait Connecting");
				testGuidanceLower.setText("等待連線中");
				if(isBleOn){
					ble.bleConnection();
					isBleOn = false;
				}
			}
		}
	};



	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mMainActivity = (MainActivity) getActivity(); 
    }
	
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		view = inflater.inflate(R.layout.fragment_test, container, false);
		startButton = (ImageView) view.findViewById(R.id.start_button);
		startButton.setOnClickListener(new StartOnClickListener());
		
		startText = (TextView) view.findViewById(R.id.start_button_text);
		accumulatedTimeText = (TextView) view.findViewById(R.id.accumulated_time_text);
		informationButton = (ImageView) view.findViewById(R.id.information_button);
		informationButton.setOnClickListener(new InfoButtonOnClickListener());
		testGuidanceUpper = (TextView) view.findViewById(R.id.test_guidance_upper);
		testGuidanceLower = (TextView) view.findViewById(R.id.test_guidance_lower);
		startButtonProgressBar = (ImageView) view.findViewById(R.id.start_button_progress_bar);
		startButtonProgressBarBg = (ImageView) view.findViewById(R.id.start_button_progress_bar_bg);
		cameraMask = (ImageView) view.findViewById(R.id.test_camera_mask);

		cameraLayout = (FrameLayout) view.findViewById(R.id.camera_loc);
		
		waitWindow = (LinearLayout) view.findViewById(R.id.wait_window);

		// TextView testGuidanceUpper = (TextView) view.findViewById(R.id.test_guide_top);
		// TextView testGuidanceLower = (TextView) view.findViewById(R.id.test_guide_bottom);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		LoadingDialogControl.dismiss();
	}
	
	
	
	
	@Override
	public void onPause() {
		if(mCamera != null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		
		super.onPause();
	}




	private class StartOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			startButton.setImageResource(R.drawable.test_start_button_pressed);
			
			// timerForPreparingSaliva.scheduleAtFixedRate(taskPreparingSaliva, 0, 1000);
			accumulatedTimeText.setVisibility(View.INVISIBLE);
			informationButton.setVisibility(View.INVISIBLE);
			testGuidanceUpper.setVisibility(View.VISIBLE);
			testGuidanceUpper.setText(R.string.prepare_saliva_guidance_upper);
			testGuidanceLower.setText(R.string.prepare_saliva_guidance_lower_off);

			
			
			MainActivity.getMainActivity().newBLE();
			ble = MainActivity.getMainActivity().getBLE();
			
			testGuidanceLower.setText("檢測器連線中");
			timerForUpdateBleState.scheduleAtFixedRate(taskUpdateBleState, 0, 2000);

			startButton.setOnClickListener(null);
		}
	}

	private class InfoButtonOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v){
			
			// MainActivity.getMainActivity().changeTab(1);
			
			
			
	 		
		}
	}


	private void takePicturesProcess(){
		accumulatedTimeText.setVisibility(View.INVISIBLE);
		timerForTakePicture.scheduleAtFixedRate(taskTakingPictures, 0, 1000);

		startButton.setVisibility(View.INVISIBLE);
		startText.setVisibility(View.INVISIBLE);
		informationButton.setVisibility(View.INVISIBLE);
		testGuidanceUpper.setText(R.string.camera_guidance_upper);
		testGuidanceLower.setText(R.string.camera_guidance_lower);

		
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				break;
			}
		}

		
		mCamPreview = new CameraPreview(getActivity());
		cameraLayout.addView(mCamPreview);
		Log.i("FORTEST", "cameraId: " + cameraId);
		mCamera = Camera.open(cameraId);
		mCamPreview.set(getActivity(), mCamera);
		
		cameraMask.bringToFront();
		startButtonProgressBarBg.bringToFront();
		startButtonProgressBar.bringToFront();

	}

	private void exceptionTestOvertime(){
		// To do
	}

	private void createWaitWindow(){
		startButton.setOnClickListener(null);
		waitWindow.setVisibility(View.VISIBLE);
		accumulatedTimeText.setVisibility(View.INVISIBLE);
	}



	@Override
	public void surfaceCreated(SurfaceHolder holder) {


	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		//關閉預覽
		mCamera.release();
	}

	void predict(double[] values){
		// Load Model
		svm_model model = null;

		try{
			FileWriter fw = new FileWriter("/storage/sdcard0/output.txt", false);
			BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
			bw.write("svm_type c_svc\nkernel_type rbf\ngamma 0.25\nnr_class 3\ntotal_sv 279\nrho 0.30686 0.357178 0.0522773\nlabel 0 1 2\nnr_sv 83 126 70\nSV\n0.306640625 0.3564453125 1:4608 2:10248 3:8448 4:23296 \n1 1 1:4608 2:9992 3:8448 4:23552 \n1 1 1:4608 2:10248 3:8448 4:23296 \n0.306640625 0.3564453125 1:4608 2:10248 3:8448 4:23552 \n1 1 1:4608 2:10248 3:8448 4:23552 \n0.306640625 0 1:4608 2:9992 3:8448 4:23296 \n1 0.3564453125 1:4608 2:9992 3:8448 4:23296 \n1 1 1:4608 2:9992 3:8448 4:23296 \n1 1 1:4352 2:9736 3:8192 4:22272 \n1 1 1:4608 2:9736 3:8192 4:22784 \n1 1 1:4352 2:9224 3:7680 4:21504 \n0.306640625 0.3564453125 1:4352 2:9224 3:7680 4:21760 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 1 1:4352 2:9224 3:7680 4:21760 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:22016 \n0.306640625 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:22016 \n1 0.3564453125 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 1 1:4352 2:9224 3:7936 4:22016 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0 1:4352 2:9224 3:7936 4:21760 \n1 0.3564453125 1:4352 2:9224 3:7936 4:21760 \n1 1 1:4352 2:9224 3:7936 4:21760 \n1 1 1:5120 2:11016 3:9216 4:25600 \n0.306640625 0.3564453125 1:5120 2:11272 3:9472 4:26112 \n1 1 1:5120 2:11272 3:9472 4:26112 \n0.306640625 0.3564453125 1:5120 2:11272 3:9472 4:25856 \n1 1 1:5120 2:11272 3:9472 4:25856 \n0.306640625 0.3564453125 1:4864 2:10760 3:9216 4:25088 \n1 1 1:4864 2:10760 3:9216 4:25088 \n1 1 1:4864 2:10760 3:8960 4:25088 \n0.3125 0.3564453125 1:4864 2:10760 3:9216 4:25344 \n0.9951171875 1 1:4864 2:10760 3:9216 4:25344 \n0.3125 0.3564453125 1:4864 2:11016 3:9216 4:25344 \n0.9951171875 1 1:4864 2:11016 3:9216 4:25344 \n0.3125 0.3564453125 1:4352 2:9736 3:7936 4:22528 \n0.9951171875 1 1:4352 2:9736 3:7936 4:22528 \n0.3125 0.3564453125 1:4352 2:9736 3:7936 4:22272 \n0.9951171875 1 1:4352 2:9736 3:7936 4:22272 \n0.3125 0.3564453125 1:4352 2:9480 3:7936 4:22016 \n0.3125 0.375 1:4352 2:9480 3:7936 4:22272 \n0.9951171875 0.982421875 1:4352 2:9480 3:7936 4:22272 \n0.3125 0.375 1:4864 2:10504 3:8704 4:24064 \n0.9951171875 0.982421875 1:4864 2:10504 3:8704 4:24064 \n0.3125 0.375 1:4864 2:10504 3:8704 4:24320 \n0.9951171875 0.982421875 1:4864 2:10504 3:8704 4:24320 \n0.3125 0.375 1:4864 2:10248 3:8704 4:24064 \n0.9951171875 0.982421875 1:4864 2:10248 3:8704 4:24064 \n0.3125 0.375 1:4608 2:10248 3:8704 4:24064 \n0.994140625 0.982421875 1:4608 2:10248 3:8704 4:24064 \n0.3125 0.375 1:4608 2:9992 3:8192 4:22784 \n0.994140625 0.982421875 1:4608 2:9992 3:8192 4:22784 \n0.3125 0.375 1:4608 2:9992 3:8448 4:23040 \n0.994140625 0.982421875 1:4608 2:9992 3:8448 4:23040 \n0.3125 0.375 1:4608 2:9992 3:8192 4:23040 \n0.994140625 0.982421875 1:4608 2:9992 3:8192 4:23040 \n0.3125 0.375 1:4352 2:9992 3:8192 4:22784 \n0.994140625 0.982421875 1:4352 2:9992 3:8192 4:22784 \n0.994140625 1 1:4352 2:9480 3:7936 4:22016 \n0.3125 0.375 1:4352 2:9736 3:8192 4:22784 \n0.994140625 0.982421875 1:4352 2:9736 3:8192 4:22784 \n0.3125 0.375 1:4352 2:9736 3:8192 4:22528 \n0.994140625 0.982421875 1:4352 2:9736 3:8192 4:22528 \n0.3125 0.375 1:5120 2:11016 3:9472 4:26368 \n0.994140625 0.982421875 1:5120 2:11016 3:9472 4:26368 \n1 1 1:4864 2:11016 3:9472 4:26112 \n0.3125 0.375 1:5120 2:11016 3:9472 4:26112 \n0.994140625 0.982421875 1:5120 2:11016 3:9472 4:26112 \n0.3125 0.375 1:4864 2:11016 3:9472 4:25856 \n0.994140625 0.982421875 1:4864 2:11016 3:9472 4:25856 \n-0 0.0517578125 1:4096 2:8712 3:7424 4:20224 \n-0.693359375 1 1:4096 2:8712 3:7424 4:20224 \n-0 0.0517578125 1:4352 2:8712 3:7424 4:20480 \n-0 0.0517578125 1:4352 2:8712 3:7424 4:20992 \n-0.693359375 1 1:4352 2:8712 3:7424 4:20992 \n-0.693359375 1 1:4864 2:9736 3:8448 4:23040 \n-0 0.0517578125 1:4864 2:9992 3:8448 4:23808 \n-0.693359375 1 1:4864 2:9992 3:8448 4:23808 \n-0 0.0517578125 1:4864 2:9992 3:8704 4:23808 \n-0 0.0517578125 1:4352 2:9480 3:8192 4:22528 \n-0.693359375 1 1:4352 2:9480 3:8192 4:22528 \n-0 0.0517578125 1:4608 2:9736 3:8704 4:23808 \n-1 1 1:4608 2:9992 3:8448 4:23296 \n-0 0.0517578125 1:4608 2:9736 3:8448 4:23552 \n-0.693359375 1 1:4608 2:9736 3:8448 4:23552 \n-0.693359375 1 1:4864 2:9992 3:8704 4:23808 \n-0 0.0517578125 1:4864 2:9992 3:8704 4:24064 \n-0.693359375 1 1:4864 2:9992 3:8704 4:24064 \n-0 0.0517578125 1:4608 2:9992 3:8704 4:24064 \n-0 0.0517578125 1:4608 2:9736 3:8704 4:23552 \n-0.693359375 1 1:4608 2:9736 3:8704 4:23808 \n-0.693359375 1 1:4608 2:9992 3:8704 4:24064 \n-0 0.0517578125 1:4608 2:9992 3:8704 4:23808 \n-0.693359375 1 1:4608 2:9992 3:8704 4:23808 \n-0.693359375 1 1:4608 2:9736 3:8704 4:23552 \n-0.693359375 1 1:4352 2:9224 3:7936 4:21504 \n-0 0.0517578125 1:4096 2:8712 3:7424 4:20480 \n-0.693359375 1 1:4352 2:8712 3:7424 4:20480 \n-0.693359375 1 1:4096 2:8200 3:7168 4:19456 \n-0 0.0517578125 1:4352 2:8712 3:7424 4:20736 \n-0 0.0517578125 1:4352 2:8968 3:7680 4:20992 \n-0.693359375 1 1:4352 2:8712 3:7424 4:20736 \n-0.693359375 1 1:4352 2:8968 3:7680 4:20992 \n-0.693359375 1 1:5120 2:10248 3:8704 4:24576 \n-0.693359375 1 1:4608 2:9480 3:8192 4:22784 \n-0.693359375 1 1:4864 2:9992 3:8448 4:23552 \n-0.693359375 1 1:5120 2:10504 3:8960 4:24576 \n-0.693359375 1 1:5120 2:10248 3:8960 4:25088 \n-0 0.0517578125 1:5120 2:10504 3:8960 4:25344 \n-0 0.0517578125 1:5120 2:10504 3:8960 4:24832 \n-0.693359375 1 1:5120 2:10504 3:8960 4:25344 \n-0.693359375 1 1:5120 2:10504 3:8960 4:24832 \n-0 0.0517578125 1:5120 2:10248 3:8960 4:24576 \n-0 0.0517578125 1:5120 2:10504 3:8960 4:25088 \n-0.693359375 1 1:5120 2:10504 3:8960 4:25088 \n-0.693359375 1 1:5120 2:10248 3:8960 4:24576 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-1 0 1:4352 2:9224 3:7936 4:22016 \n-0 0.0625 1:4352 2:8968 3:7680 4:21504 \n-1 0.0625 1:4352 2:9224 3:7936 4:22016 \n-1 0.990234375 1:4352 2:9224 3:7936 4:22016 \n-0 0.0625 1:4352 2:8968 3:7936 4:21504 \n-0.693359375 0.990234375 1:4352 2:8968 3:7680 4:21504 \n-1 0 1:4352 2:9224 3:7936 4:21760 \n-0 0.0625 1:4352 2:8968 3:7936 4:21760 \n-1 0.0625 1:4352 2:9224 3:7936 4:21760 \n-1 0.990234375 1:4352 2:9224 3:7936 4:21760 \n-0.693359375 0.990234375 1:4352 2:8968 3:7936 4:21760 \n-0.693359375 0.990234375 1:4352 2:8968 3:7936 4:21504 \n-0 0.0625 1:4352 2:8712 3:7680 4:20992 \n-0.693359375 0.990234375 1:4352 2:8712 3:7680 4:20992 \n-0 0.0625 1:4352 2:8968 3:7680 4:21248 \n-0.693359375 0.990234375 1:4352 2:8968 3:7680 4:21248 \n-0.693359375 1 1:4352 2:8712 3:7680 4:21248 \n-0.693359375 1 1:3840 2:8200 3:7168 4:20480 \n-0.693359375 1 1:4096 2:8712 3:7424 4:20480 \n-0 0.0625 1:3840 2:8456 3:7424 4:19968 \n-0 0.0625 1:3840 2:8456 3:7168 4:19968 \n-0 0.0625 1:3584 2:7944 3:6912 4:18944 \n-0.693359375 0.990234375 1:3840 2:8456 3:7168 4:19968 \n-0.693359375 0.990234375 1:3840 2:8456 3:7424 4:19968 \n-0 0.0625 1:3840 2:8200 3:7168 4:19968 \n-0.693359375 0.990234375 1:3840 2:8200 3:7168 4:19968 \n-0 0.0625 1:3840 2:8456 3:7424 4:20224 \n-0.693359375 0.990234375 1:3840 2:8456 3:7424 4:20224 \n-0 0.0625 1:4096 2:8712 3:7424 4:20736 \n-0.693359375 0.990234375 1:4096 2:8712 3:7424 4:20736 \n-0 0.0625 1:4096 2:8456 3:7424 4:20480 \n-0.693359375 0.990234375 1:4096 2:8456 3:7424 4:20480 \n-0.693359375 1 1:3840 2:8200 3:6912 4:19456 \n-0.693359375 1 1:3840 2:7688 3:6656 4:18432 \n-0.693359375 1 1:4096 2:8200 3:7424 4:20224 \n-0 0.0625 1:3840 2:8200 3:7168 4:19712 \n-0 0.0625 1:4096 2:8456 3:7424 4:20224 \n-0 0.0625 1:4096 2:8456 3:7168 4:19968 \n-0 0.0625 1:4096 2:8200 3:7168 4:19712 \n-0.693359375 0.990234375 1:4096 2:8456 3:7424 4:20224 \n-0.693359375 0.990234375 1:3840 2:8200 3:7168 4:19712 \n-0 0.0625 1:4096 2:8456 3:7168 4:20224 \n-0.693359375 0.990234375 1:4096 2:8456 3:7168 4:19968 \n-0.693359375 0.990234375 1:4096 2:8456 3:7168 4:20224 \n-0 0.0625 1:3840 2:8200 3:7168 4:19456 \n-0.693359375 0.990234375 1:4096 2:8200 3:7168 4:19712 \n-0 0.0625 1:4096 2:8200 3:7168 4:19968 \n-0.693359375 0.990234375 1:3840 2:8200 3:7168 4:19456 \n-0.693359375 0.990234375 1:4096 2:8200 3:7168 4:19968 \n-0.693359375 0.990234375 1:3584 2:7944 3:6912 4:18944 \n-0 0.0625 1:3328 2:7176 3:6144 4:16896 \n-0.693359375 1 1:3584 2:7688 3:6912 4:18688 \n-0 0.0625 1:3584 2:7944 3:6912 4:18688 \n-0.693359375 0.990234375 1:3584 2:7944 3:6912 4:18688 \n-0.6923828125 0.990234375 1:3328 2:7176 3:6144 4:16896 \n-0 0.0625 1:3584 2:7688 3:6656 4:18176 \n-0 0.0625 1:3584 2:7944 3:6656 4:18688 \n-0.6923828125 1 1:3328 2:7176 3:6144 4:16640 \n-0.6923828125 0.990234375 1:3584 2:7944 3:6656 4:18688 \n-0.6923828125 1 1:3328 2:7432 3:6400 4:17920 \n-0 0.0625 1:3328 2:7688 3:6400 4:17920 \n-0.6923828125 1 1:3584 2:7688 3:6656 4:18432 \n-0.6923828125 0.990234375 1:3584 2:7688 3:6656 4:18176 \n-0.6923828125 1 1:3584 2:7944 3:6656 4:18432 \n-0.6923828125 0.990234375 1:3328 2:7688 3:6400 4:17920 \n-0.642578125 -0.947265625 1:2560 2:5384 3:4352 4:12544 \n-0.642578125 -0.947265625 1:2304 2:4872 3:4096 4:11264 \n-0.642578125 -0.947265625 1:2304 2:4872 3:4096 4:11520 \n-0.642578125 -0.947265625 1:2304 2:5384 3:4352 4:12288 \n-0.642578125 -0.947265625 1:2304 2:5384 3:4352 4:12544 \n-0.642578125 -0.947265625 1:2304 2:5384 3:4352 4:12032 \n-0.642578125 -0.947265625 1:2304 2:5128 3:4352 4:12544 \n-0.642578125 -0.947265625 1:2304 2:5128 3:4352 4:12288 \n-0.642578125 -0.947265625 1:2816 2:5640 3:4864 4:13312 \n-0.642578125 -0.947265625 1:2304 2:5128 3:4352 4:12032 \n-0.642578125 -0.947265625 1:2304 2:5128 3:4352 4:11776 \n-0.642578125 -0.947265625 1:2560 2:5640 3:4864 4:13312 \n-0.642578125 -0.947265625 1:2560 2:5640 3:4608 4:13312 \n-0.642578125 -0.9482421875 1:2560 2:5640 3:4608 4:13056 \n-0.642578125 -0.9482421875 1:2560 2:5384 3:4608 4:12544 \n-0.642578125 -0.9482421875 1:2560 2:5384 3:4608 4:12800 \n-0.642578125 -0.9482421875 1:2560 2:5384 3:4608 4:13056 \n-0.642578125 -0.9482421875 1:2304 2:4616 3:3840 4:11264 \n-0.642578125 -0.9482421875 1:2048 2:4360 3:3840 4:10752 \n-0.642578125 -0.9482421875 1:2048 2:4360 3:3840 4:10496 \n-0.642578125 -0.9482421875 1:2304 2:4360 3:3840 4:11008 \n-0.642578125 -0.9482421875 1:2048 2:4360 3:3584 4:10240 \n-0.642578125 -0.9482421875 1:2304 2:4616 3:3840 4:11008 \n-0.642578125 -0.9482421875 1:2048 2:4360 3:3584 4:10496 \n-0.642578125 -0.9482421875 1:2304 2:4616 3:3840 4:10752 \n-0.642578125 -0.9482421875 1:2304 2:4360 3:3840 4:10752 \n-0.642578125 -0.9482421875 1:2304 2:4104 3:3328 4:9728 \n-0.642578125 -0.947265625 1:1792 2:3336 3:2816 4:8448 \n-0.642578125 -0.947265625 1:2304 2:4104 3:3328 4:9984 \n-0.642578125 -0.947265625 1:2304 2:4104 3:3328 4:9472 \n-0.642578125 -0.947265625 1:2048 2:3848 3:3328 4:9728 \n-0.642578125 -0.947265625 1:2048 2:4104 3:3328 4:9984 \n-0.642578125 -0.947265625 1:2048 2:4104 3:3328 4:9728 \n-0.642578125 -0.947265625 1:2048 2:3848 3:3328 4:9472 \n-0.642578125 -0.947265625 1:2048 2:4104 3:2816 4:8448 \n-0.642578125 -0.947265625 1:2048 2:4104 3:3328 4:9472 \n-0.642578125 -0.947265625 1:2048 2:3848 3:3072 4:9472 \n-0.642578125 -0.9482421875 1:1792 2:3336 3:2560 4:7936 \n-0.642578125 -0.9482421875 1:2048 2:3592 3:3072 4:9216 \n-0.642578125 -0.9482421875 1:2048 2:3848 3:3072 4:9216 \n-0.642578125 -0.9482421875 1:2048 2:3848 3:3072 4:8960 \n-0.642578125 -0.9482421875 1:2048 2:3592 3:3072 4:8960 \n-0.642578125 -0.9482421875 1:2048 2:3592 3:3072 4:8704 \n-0.642578125 -0.9482421875 1:1792 2:3592 3:2816 4:8704 \n-0.642578125 -0.9482421875 1:1792 2:3336 3:2816 4:8192 \n-0.642578125 -0.9482421875 1:1792 2:3336 3:2816 4:8704 \n-0.642578125 -0.9482421875 1:1792 2:3592 3:2816 4:8448 \n-0.642578125 -0.9482421875 1:1536 2:3080 3:2560 4:7168 \n-0.642578125 -0.9482421875 1:1280 2:3080 3:2560 4:6912 \n-0.642578125 -0.9482421875 1:1536 2:3336 3:2816 4:7936 \n-0.642578125 -0.9482421875 1:1792 2:3848 3:3328 4:9472 \n-0.642578125 -0.9482421875 1:1792 2:3592 3:3072 4:9216 \n-0.642578125 -0.9482421875 1:1792 2:3848 3:3328 4:8960 \n-0.642578125 -0.9482421875 1:1792 2:3848 3:3328 4:9216 \n-0.642578125 -0.9482421875 1:1792 2:3848 3:3328 4:8704 \n-0.642578125 -0.9482421875 1:1792 2:3592 3:3072 4:8960 \n-0.6435546875 -0.9482421875 1:1792 2:3592 3:3072 4:8448 \n-0.6435546875 -0.9482421875 1:1792 2:3592 3:3072 4:8704 \n-0.6435546875 -0.9482421875 1:1536 2:3336 3:3072 4:8192 \n-0.6435546875 -0.9482421875 1:1280 2:2568 3:2048 4:6400 \n-0.6435546875 -0.9482421875 1:1280 2:2568 3:2048 4:6144 \n-0.6435546875 -0.9482421875 1:1280 2:3080 3:2560 4:7424 \n-0.6435546875 -0.9482421875 1:1536 2:3080 3:2560 4:7424 \n-0.6435546875 -0.9482421875 1:1280 2:2824 3:2560 4:7168 \n-0.6435546875 -0.9482421875 1:1280 2:3080 3:2560 4:7168 \n-0.6435546875 -0.9482421875 1:1280 2:2824 3:2560 4:6912 \n-0.6435546875 -0.9482421875 1:1280 2:2824 3:2304 4:6912 \n-0.6435546875 -0.9482421875 1:1280 2:2824 3:2304 4:6400 \n-0.6435546875 -0.9482421875 1:1280 2:2824 3:2304 4:6656 \n-0.6435546875 -0.9482421875 1:1280 2:2568 3:2304 4:6400 \n");
			bw.newLine();
			bw.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		try {
			model = svm.svm_load_model( "/storage/sdcard0/output.txt" );
		} catch (IOException e) {
			e.printStackTrace();
		}


		// Start Predict
		int M_LENGTH = 4;
		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);

		svm_node[] x = new svm_node[M_LENGTH];
		for(int j=0;j<M_LENGTH;j++)
		{
			x[j] = new svm_node();
			x[j].index = j+1;
			x[j].value = values[j];
		}

		double predictResult = svm.svm_predict(model, x);
		Log.i("Libsvm", (int)predictResult + " ");
	}

	public void enableStartButton(boolean enable) {
		startButton.setEnabled(enable);
	}

	public void setState(int state) {
		switch (state) {
		case STATE_INIT:
			MainActivity.getMainActivity().enableTabAndClick(true);
//			testCircle.setImageDrawable(null);
			setGuideMessage(R.string.test_guide_start_top, R.string.test_guide_start_bottom);
//			messageView.setText("");
			startButton.setOnClickListener(new StartOnClickListener());
			startButton.setEnabled(true);
			startButton.setVisibility(View.VISIBLE);
			startText.setVisibility(View.VISIBLE);
			startText.setText(R.string.start);
//			helpButton.setOnClickListener(new TutorialOnClickListener());
			face.setVisibility(View.INVISIBLE);
			break;
		}
	}

	public void setGuideMessage(int str_id_top, int str_id_bottom) {
		if (testGuidanceUpper != null)
			testGuidanceUpper.setText(str_id_top);
		if (testGuidanceLower != null)
			testGuidanceLower.setText(str_id_bottom);
	}

	public boolean getShowCountDown(){
		return showCountDown;
	}

	public void setStartButtonText(int str_id) {
		startText.setText(str_id);
	}

}


