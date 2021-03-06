package com.example.raveh.rscontrol;

/**
 * Created by Raveh on 21/10/2014.
 */

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private Timer mTimer;
    private PingTimerTask mTimerTask;
    private RsProtocol mProtocol;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private class PingTimerTask extends TimerTask
    {
        PingTimerTask(RsProtocol protocol)
        {
            m_Protocol = protocol;
        }

        public void setCharacteristics(ArrayList<ArrayList<BluetoothGattCharacteristic>> characs)
        {
            m_Characteristics = characs;
        }

        public void setService(BluetoothLeService service)
        {
            m_Service = service;
        }

        @Override
        public void run() {
            m_Protocol.sendPing(m_Service, m_Characteristics);
        }

        private RsProtocol m_Protocol;
        private BluetoothLeService m_Service;
        private ArrayList<ArrayList<BluetoothGattCharacteristic>> m_Characteristics;
    };

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
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
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.RECEIVE_INFORMATION.equals(action)) {
                mProtocol.sendConfig(mBluetoothLeService, mGattCharacteristics);
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    /*private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mProtocol.connect(mBluetoothLeService, mGattCharacteristics);
                        }
                        return true;
                    }
                    return false;
                }
            };*/

    private final Button.OnClickListener sendSettingsClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.connect(mBluetoothLeService, mGattCharacteristics);
                    view.setEnabled(false);

                    mTimerTask.setService(mBluetoothLeService);
                    mTimer.schedule(mTimerTask, 0, 50);
                }
            };

    private final Button.OnClickListener takeOffClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.takeOff(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener landClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.land(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener buttonClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {

                    try {
                        mProtocol.button(mBluetoothLeService, mGattCharacteristics);
                        Thread.sleep(25);
                        mProtocol.button1(mBluetoothLeService, mGattCharacteristics);
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

    private final Button.OnClickListener button2ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    try {
                        mProtocol.button4(mBluetoothLeService, mGattCharacteristics);
                        Thread.sleep(100);
                        mProtocol.button5(mBluetoothLeService, mGattCharacteristics);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

    private final Button.OnClickListener button3ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.button3(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener button4ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.button4(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener button5ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.button5(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener button7ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.button7(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private final Button.OnClickListener button6ClickListner =
            new Button.OnClickListener() {

                @Override
                public void onClick(View view) {
                    mProtocol.button6(mBluetoothLeService, mGattCharacteristics);
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);
        ((Button)findViewById(R.id.send_settings_button)).setOnClickListener(sendSettingsClickListner);
        findViewById(R.id.send_settings_button).setEnabled(false);
        ((Button)findViewById(R.id.take_off_button)).setOnClickListener(takeOffClickListner);
        ((Button)findViewById(R.id.land_button)).setOnClickListener(landClickListner);
        ((Button)findViewById(R.id.button)).setOnClickListener(buttonClickListner);
        ((Button)findViewById(R.id.button2)).setOnClickListener(button2ClickListner);
        ((Button)findViewById(R.id.button3)).setOnClickListener(button3ClickListner);
        ((Button)findViewById(R.id.button4)).setOnClickListener(button4ClickListner);
        ((Button)findViewById(R.id.button5)).setOnClickListener(button5ClickListner);
        ((Button)findViewById(R.id.button6)).setOnClickListener(button6ClickListner);
        ((Button)findViewById(R.id.button7)).setOnClickListener(button7ClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mProtocol = new RsProtocol();
        mTimer = new Timer();
        mTimerTask = new PingTimerTask(mProtocol);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
        mTimerTask.setCharacteristics(mGattCharacteristics);
        findViewById(R.id.send_settings_button).setEnabled(true);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.RECEIVE_INFORMATION);
        return intentFilter;
    }
}