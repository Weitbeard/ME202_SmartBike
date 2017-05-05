package edu.stanford.me202.lw_me202;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class BikeMonitorService extends Service {

    private static final String TAG = "BikeMonitorService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public BluetoothGatt mBluetoothGatt;
    public BluetoothGattCharacteristic tx;
    public BluetoothGattCharacteristic rx;

     //intent actions to handle
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String ACTION_WRITING_DATA =
            "ACTION_WRITING_DATA";
    public final static String BIKE_MOVEMENT =
            "BIKE_MOVEMENT";

     //UUID defines
    public final static UUID UART_UUID =
            UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID RX_UUID =
            UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID TX_UUID =
            UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID CLIENT_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public BikeMonitorService() { }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
             //if the change was a new connection
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //broadcast a new intent relaying the new connection
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            //else if the change was a disconnection
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //broadcast a new intent relaying the lost connection
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
         //when new services are discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt,status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                 //get the new services and characteristics
                 //UART service
                BluetoothGattService RxService = mBluetoothGatt.getService(UART_UUID);
                if(RxService == null) {return;}
                 //transmit characteristic
                tx = RxService.getCharacteristic(TX_UUID);
                if(tx == null) {return;}
                 //receive characteristic
                rx = RxService.getCharacteristic(RX_UUID);
                if(rx == null) {return;}
                 //enable notifications from the transmit characteristic (data sent to phone)
                mBluetoothGatt.setCharacteristicNotification(tx, true);
                BluetoothGattDescriptor desc = tx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(desc);
                 //broadcast a new intent relaying that services were discovered
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

         //when a characteristic is read
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG,"characteristicRead: "+characteristic);
             //broadcast an associated intent with the characteristic as data (if the read was successful)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

         //when a characteristic is written (not used in this application currently)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
             //broadcast an associated intent with the characteristic as data (if the write was successful)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_WRITING_DATA, characteristic);
            }
        }

        //broadcast an associated intent with the characteristic as data
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
             //broadcast an associated intent with the characteristic as data
            Log.d(TAG,"characteristicChange: "+characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    /**************** Intent Broadcasting **************************/
     //relay basic intents without data to the activities
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.d(TAG,"sending intent for action: "+action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //relay intents WITH data to the activities
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
         //if the callback was for a characteristic change or read
        if(ACTION_DATA_AVAILABLE.equals(action)) {
             //get the changed characteristic data and add it to the intent
            final byte[] receivedData = characteristic.getValue();
            if (receivedData != null && receivedData.length > 0) {
                 //assuming transmissions of a single byte from rx containing the requested Device ID
                String moving = new String(receivedData);
                Log.d(TAG, "movement state: "+ moving);
                intent.putExtra(BIKE_MOVEMENT, moving);
            }
        }
         //broadcast the modified intent
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**************** Service Binding **************************/
    public class LocalBinder extends Binder {
        BikeMonitorService getService() {
            return BikeMonitorService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        //dummy test code to check service beginning
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        close();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**************** BLE Connection Lifecycle **************************/
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
        tx = null;
        rx = null;
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        tx = null;
        rx = null;
    }
}
