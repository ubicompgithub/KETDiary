package ubicomp.ketdiary.test.bluetoothle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import ubicomp.ketdiary.main.R;
import ubicomp.ketdiary.test.bluetoothle.DeviceListActivity.ViewHolder;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;




import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;


@SuppressLint("NewApi")
public class BluetoothLE {
    private static final String TAG = "BluetoothLE";
    
    //Notifiation UUID
    private static final UUID SERVICE4 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE4_CONFIG_CHAR = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    private static final UUID SERVICE4_SIMPLE_PROFILE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    private Activity activity = null;
    private BluetoothAdapter mBluetoothAdapter;
    
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic mProtocolCharacteristic;
    
    private long steps = 0;

    private int finalResult = -1;
    
    private String currentDeviceState = null;
    private String currentVoltage = null;
    private int currentDeviceProtocol = 0;

    private Handler mHandler;
    private BluetoothDevice myDevice;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 10000;
    
     
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                activity.finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
    
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Toast.makeText(activity, "BLE connected", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Toast.makeText(activity, "BLE disconnected!", Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            	List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
            	mNotifyCharacteristic = gattServices.get(3).getCharacteristic(SERVICE4_CONFIG_CHAR);
            	mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);

                
//                Toast.makeText(activity, "ACTION_GATT_SERVICES_DISCOVERED", Toast.LENGTH_SHORT).show(); 
            } else if (BluetoothLeService.ACTION_PROTOCOL_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                // Log.i( "FORTEST", "Protocol Read Data: " + data[0] );
                currentDeviceProtocol = data[0];


            } else if (BluetoothLeService.ACTION_WRITE_PROTOCOL_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                // Log.i( "FORTEST", "Protocol Write Data: " + data[0] );


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);


                // Debug Protocol
                currentDeviceState = String.format("%02X ", data[0]);

                if(currentDeviceState.equals("FC ") || currentDeviceState.equals("FD ")){
                    currentVoltage = String.format("%02X ", data[1]);
                    Log.i( "FORTEST", "##data[0]: " + currentDeviceState + "data[1]: " + currentVoltage );
                }
                else if( currentDeviceState.equals("FB ") ){
                    Log.i( "FORTEST", "##data[0]: " + currentDeviceState + "data[6]: " + data[6] );
                }
                else{
                    Log.i( "FORTEST", "##data[0]: " + currentDeviceState);
                }
                

                if(currentDeviceState.equals("FA ")){
                    // No connection
                    // Log.i("FORTEST", "## No connection!");
                }
                else if(currentDeviceState.equals("FB ")){
                    // Connected
                    // Log.i("FORTEST", "## Connected!");
                }
                else if(currentDeviceState.equals("FC ")){
                    // No saliva
                    // Log.i("FORTEST", "## No saliva!");
                }
                else if(currentDeviceState.equals("FD ")){
                    // 1 pass, 2 not yet
                    // Log.i("FORTEST", "## 1 pass, 2 not yet!");
                }
                else if(currentDeviceState.equals("FE ")){
                    // 1 pass, 2 pass
                    // Log.i("FORTEST", "## 1 pass, 2 pass!");
                }
                else if(currentDeviceState.equals("FF ")){
                    // Color
                    // Log.i("FORTEST", "## Color!");

                    int color_sensor0[] = new int[4];
                    int color_sensor1[] = new int[4];
                    for(int i=0; i<4; i++) {
                        color_sensor0[i] = data[(i*2)+2]*256 + data[i*2+1];
                        color_sensor1[i] = data[(i*2)+10]*256 + data[i*2+9];
                    }

                    predict(color_sensor0, color_sensor1);

                    Log.i("FORTEST", " "+color_sensor0[0]+" "+color_sensor0[1]+" "+color_sensor0[2]+" "+color_sensor0[3]+" "+color_sensor1[0]+" "+color_sensor1[1]+" "+color_sensor1[2]+" "+color_sensor1[3]);
                }
                

            }
        }
    };
    
    public BluetoothLE(Activity activity) {
        this.activity = activity;

        // timerForUpdate.scheduleAtFixedRate(task, 0, 1000);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity, "BLE not supported!", Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        mHandler = new Handler();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth is not supported!", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
    }
    
    public boolean bleConnection() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        else {
            Intent serverIntent = new Intent(activity, DeviceListActivity.class);
            activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
        
        return false;
    }
    
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_PROTOCOL_DATA_AVAILABLE);  // Blue Zhong
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_PROTOCOL_DATA_AVAILABLE);  // Blue Zhong
        return intentFilter;
    }
    
    public void onBleActivityResult(int requestCode, int resultCode, Intent data) {
//      Toast.makeText(activity.getApplicationContext(), "onBleActivityResult", Toast.LENGTH_SHORT).show();
        
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            
            // Get the device MAC address
            mDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            Toast.makeText(activity, "Bluetooth address: " + mDeviceAddress, Toast.LENGTH_SHORT).show();
            
            Intent gattServiceIntent = new Intent(activity, BluetoothLeService.class);
            activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            
            activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled
                Intent serverIntent = new Intent(activity, DeviceListActivity.class);
                activity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            } else{
                // User did not enable Bluetooth or an error occured
                Toast.makeText(activity, "Bluetooth did not enable!", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            break;
        }
        
    }
    
    void predict(int []colorSensor1, int []colorSensor2){
		// Load Model
		svm_model model1 = null;
		svm_model model2 = null;
	
		try{
			FileWriter fw1 = new FileWriter("/storage/sdcard0/model1.txt", false);
			BufferedWriter bw1 = new BufferedWriter(fw1); //將BufferedWeiter與FileWrite物件做連結
			bw1.write("svm_type c_svc\nkernel_type polynomial\ndegree 3\ngamma 0.25\ncoef0 0\nnr_class 2\ntotal_sv 2\nrho -3.91442\nlabel 0 1\nnr_sv 1 1\nSV\n4.286751187782065e-019 1:768 2:1032 3:768 4:2560 \n-4.286751187782065e-019 1:768 2:1288 3:768 4:3072 \n");
			bw1.newLine();
			bw1.close();
			
			FileWriter fw2 = new FileWriter("/storage/sdcard0/model2.txt", false);
			BufferedWriter bw2 = new BufferedWriter(fw2); //將BufferedWeiter與FileWrite物件做連結
			bw2.write("svm_type c_svc\nkernel_type polynomial\ndegree 3\ngamma 0.25\ncoef0 0\nnr_class 2\ntotal_sv 2\nrho -3.77756\nlabel 0 1\nnr_sv 1 1\nSV\n1.380649886196357e-019 1:768 2:1288 3:1024 4:3072 \n-1.380649886196357e-019 1:768 2:1800 3:1024 4:3584 \n");
			bw2.newLine();
			bw2.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	
		try {
			model1 = svm.svm_load_model( "/storage/sdcard0/model1.txt" );
			model2 = svm.svm_load_model( "/storage/sdcard0/model2.txt" );
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
		// Start Predict
		int M_LENGTH = 4;
		int svm_type1=svm.svm_get_svm_type(model1);
		int nr_class1=svm.svm_get_nr_class(model1);
	
		svm_node[] x1 = new svm_node[M_LENGTH];
		for(int j=0;j<M_LENGTH;j++)
		{
			x1[j] = new svm_node();
			x1[j].index = j+1;
			x1[j].value = colorSensor1[j];
		}
	
		double predictResult1 = svm.svm_predict(model1, x1);
	
		int svm_type2=svm.svm_get_svm_type(model2);
		int nr_class2=svm.svm_get_nr_class(model2);
	
		svm_node[] x2 = new svm_node[M_LENGTH];
		for(int j=0;j<M_LENGTH;j++)
		{
			x2[j] = new svm_node();
			x2[j].index = j+1;
			x2[j].value = colorSensor2[j];
		}
	
		double predictResult2 = svm.svm_predict(model2, x2);
	
		Log.i("FORTEST", "Predict: " + (int)predictResult1 + ", " + (int)predictResult2);


        
        if(predictResult1 == 1 && predictResult2 == 1){
            finalResult = 0;
        }
        else if(predictResult1 == 0 && predictResult2 == 0){
            finalResult = 3;
        }
	}

    public int getFinalResult(){
        return finalResult;
    }

    
    





    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;  // Blue Zhong
    private String LIST_NAME = "list_name";
    private String LIST_UUID = "list_uuid";



    BluetoothGattCharacteristic protocolGattCharacteristic;


    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    protocolGattCharacteristic = gattCharacteristic;
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
    }



    private void readGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    mBluetoothLeService.readCharacteristic(gattCharacteristic);
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
    }


    private boolean writeGattServices1(List<BluetoothGattService> gattServices) {
        boolean isWriteSuccess = false;
        
        if (gattServices == null) return false;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    // BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v.getTag();
                    gattCharacteristic.setValue(new byte[] { (byte) 0x01 });
                    mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                    isWriteSuccess = true;
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

            
         }
        return isWriteSuccess;
    }

    private boolean writeGattServices2(List<BluetoothGattService> gattServices) {
        boolean isWriteSuccess = false;

        if (gattServices == null) return false;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    gattCharacteristic.setValue(new byte[] { (byte) 0x02 });
                    mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                    isWriteSuccess = true;
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

            
         }
        return isWriteSuccess;
    }



    private boolean writeGattServices3(List<BluetoothGattService> gattServices) {
        boolean isWriteSuccess = false;

        if (gattServices == null) return false;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    // BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v.getTag();
                    gattCharacteristic.setValue(new byte[] { (byte) 0x03 });
                    mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                    isWriteSuccess = true;
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

            
         }
        return isWriteSuccess;
    }


    private boolean writeGattServices4(List<BluetoothGattService> gattServices) {
        boolean isWriteSuccess = false;

        if (gattServices == null) return false;
        String uuid = null;
        String unknownServiceString = activity.getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = activity.getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                // Blue Zhong
                if( uuid.equals(BleUuid.CHAR_BLE_PROTOCOL) ){
                    // BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v.getTag();
                    gattCharacteristic.setValue(new byte[] { (byte) 0x04 });
                    mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                    isWriteSuccess = true;
                }

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

            
         }
        return isWriteSuccess;
    }


    public String getCurrentDeviceState(){
        return currentDeviceState;
    }

    public boolean isBleConnected(){
        return mConnected;
    }

    public void readProtocolCharacteristic(){
        readGattServices(mBluetoothLeService.getSupportedGattServices());
    }

    public int getProtocolCharacteristic(){
        return currentDeviceProtocol;
    }

    public boolean writeProtocolCharacteristic1(){
        return writeGattServices1(mBluetoothLeService.getSupportedGattServices());
    }
    public boolean writeProtocolCharacteristic2(){
    	return writeGattServices2(mBluetoothLeService.getSupportedGattServices());
    }
    public boolean writeProtocolCharacteristic3(){
    	return writeGattServices3(mBluetoothLeService.getSupportedGattServices());
    }
    public boolean writeProtocolCharacteristic4(){
        return writeGattServices4(mBluetoothLeService.getSupportedGattServices());
    }



    public void autoConnectBLE(){
        Log.i("FORTEST", "autoConnectBLE");
        scanLeDevice(true);
        Log.i("FORTEST", "myDevice: " + myDevice.getName() );
        // device = mLeDeviceListAdapter.getDevice(position);
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
    //                    Log.i("BLE", "thread run");
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }



    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myDevice = device;
                    // mLeDeviceListAdapter.addDevice(device);
                    // mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    // final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);




}
