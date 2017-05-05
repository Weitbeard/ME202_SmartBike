package edu.stanford.me202.lw_me202;

import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static edu.stanford.me202.lw_me202.R.id.unlockDialogEntry;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";
    private BikeMonitorService mBMService;
    private String bikeAddress;
    private String bikeMovement;

    //linking views
    @BindView(R.id.unlockButton) Button unlockButton;
    @BindView(R.id.connStatusText) TextView connStatusText;
    @BindView(R.id.bikeMovementText) TextView bikeMovementText;
    @BindView(R.id.lightModeSwitch) Switch lightModeSwitch;
    @BindView(R.id.lightStateSwitch) Switch lightStateSwitch;
    @BindView(R.id.lightModeText) TextView lightModeText;
    @BindView(R.id.lightStateText) TextView lightStateText;
    @BindView(R.id.historyButton) Button historyButton;

    private final ServiceConnection mBMConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBMService = ((BikeMonitorService.LocalBinder) service).getService();
            if (!mBMService.initialize()) {
                Log.e(TAG, "Unable to initialize BikeMonitor");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBMService.connect(bikeAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBMService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             //get the intent
            final String action = intent.getAction();
             //based on the intent's contents:
            if (BikeMonitorService.ACTION_GATT_CONNECTED.equals(action)) {
                 //change the textview to show device is connected
                connStatusText.setText(getString(R.string.bikeConnected_text)+" "+bikeAddress);
                 //populate toast with selected ID number & dismiss dialog
                String unlockToastText = getString(R.string.unlockGoodToast_text) + bikeAddress;
                 //show toast
                Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                toast.show();
                 //enable light switches
                lightModeSwitch.setEnabled(true);
                lightStateSwitch.setEnabled(true);
            } else if (BikeMonitorService.ACTION_GATT_DISCONNECTED.equals(action)) {
                 //change the textviews to show device is disconnected
                connStatusText.setText(getString(R.string.connStatus_text));
                bikeMovementText.setText(getString(R.string.bikeMovement_default));
                bikeMovement = "";
                 //disable switches
                lightModeSwitch.setEnabled(false);
                lightStateSwitch.setEnabled(false);
            } else if (BikeMonitorService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG,"services discovered intent received");
            } else if (BikeMonitorService.ACTION_DATA_AVAILABLE.equals(action)) {
                 //display the Device ID
                Log.d(TAG,"data available: " + intent.getStringExtra(BikeMonitorService.BIKE_MOVEMENT));
                bikeMovement = intent.getStringExtra(BikeMonitorService.BIKE_MOVEMENT);
                if(bikeMovement.equals("0")){
                    bikeMovementText.setText(getString(R.string.bikeMovement_stationary));
                }
                else{
                    bikeMovementText.setText(getString(R.string.bikeMovement_moving));
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //link the proper layout
        setContentView(R.layout.activity_control);
         //bind views
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReceiver);
        unbindService(mBMConnection);
        mBMService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

         //User clicked unlock button =>
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 //create & show unlocking dialog
                final Dialog dialog = new UnlockDialog(ControlActivity.this);
                dialog.show();
            }
        });

         //user changes the Light Mode switch =>
        lightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                 //if the switch is HIGH
                if (isChecked) {
                    lightModeText.setText(getString(R.string.lightModeBlinking_text));
                    WriteStringBLE("d");
                 //if the switch is LOW
                } else {
                    lightModeText.setText(getString(R.string.lightModeSolid_text));
                    WriteStringBLE("e");
                }
            }
        });

         //user changes the Light State switch =>
        lightStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                 //if the switch is HIGH
                if (isChecked) {
                    lightStateText.setText(getString(R.string.lightStateOn_text));
                    WriteStringBLE("b");
                 //if the switch is LOW
                } else {
                    lightStateText.setText(getString(R.string.lightStateAuto_text));
                    WriteStringBLE("c");
                }
            }
        });

         //user clicks the "Ride History" button =>
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change to ride history activity
                startActivity(new Intent(getApplicationContext(), RideHistoryActivity.class));
            }
        });
    }

    private void WriteStringBLE(String c){
        mBMService.rx.setValue(c);
        mBMService.mBluetoothGatt.writeCharacteristic(mBMService.rx);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BikeMonitorService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BikeMonitorService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BikeMonitorService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BikeMonitorService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BikeMonitorService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BikeMonitorService.ACTION_WRITING_DATA);
        return intentFilter;
    }

    public class UnlockDialog extends Dialog {
         //unlock dialog views
        @BindView(R.id.unlockDialogEntry) EditText unlockDialogEntry;
        @BindView(R.id.unlockEnterButton) Button unlockEnterButton;
        @BindView(R.id.unlockCancelButton) Button unlockCancelButton;

        public UnlockDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(R.string.unlockDialogTitle_text);
            setCancelable(false);
            //link the dialog layout
            setContentView(R.layout.dialog_unlock);
            //bind views
            ButterKnife.bind(this);

            //if user clicks "Enter" button in dialog =>
            unlockEnterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bikeAddress = unlockDialogEntry.getText().toString();
                    //if a valid ID has been entered
                    if(!bikeAddress.equals("")){
                        //start BLE service to connect with monitor
                        Intent bikeMonitorIntent = new Intent(getApplicationContext(), BikeMonitorService.class);
                        bindService(bikeMonitorIntent, mBMConnection, BIND_AUTO_CREATE);
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        //populate toast with warning
                        String unlockToastText = getString(R.string.unlockBadToast_text);
                        //show toast
                        Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            //if user clicks the "Cancel" button in the dialog =>
            unlockCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dismiss the dialog
                    dismiss();
                }
            });
        }
    }
}
