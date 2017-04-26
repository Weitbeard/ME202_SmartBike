package edu.stanford.me202.lw_me202;

import android.app.Dialog;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static edu.stanford.me202.lw_me202.R.id.unlockDialogEntry;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";
    private BikeMonitorService mBMService;

    //linking views
    @BindView(R.id.unlockButton) Button unlockButton;
    @BindView(R.id.connStatusText) TextView connStatusText;
    @BindView(R.id.bikeIDText) TextView bikeIDText;
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
            //mBMService.connect(bikeAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBMService = null;
        }
    };

    private final BroadcastReceiver mBMUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             //get the intent
            final String action = intent.getAction();
             //based on the intent's contents:
            if (BikeMonitorService.INTENT_TEST.equals(action)){
                Log.e(TAG,"Service test intent received");
            }
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
//            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//            }
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
        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mBMConnection);
        mBMService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                 //if the switch is enabled
                if (isChecked) {
                    lightModeText.setText(getString(R.string.lightModeBlinking_text));
                        // TODO
                 //if the switch is disabled
                } else {
                    lightModeText.setText(getString(R.string.lightModeSolid_text));
                        // TODO

                }
            }
        });

         //user changes the Light State switch =>
        lightStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                 //if the switch is enabled
                if (isChecked) {
                    lightStateText.setText(getString(R.string.lightStateOn_text));
                        // TODO
                 //if the switch is disabled
                } else {
                    lightStateText.setText(getString(R.string.lightStateAuto_text));
                        // TODO
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
                    unlockDialogEntry.setText(getString(R.string.defaultBikeAddress));
                    String bikeID = unlockDialogEntry.getText().toString();
                    String unlockToastText;
                    //if a valid ID has been entered
                    if(!bikeID.equals("")){
                        //start BLE service to connect with monitor
                        Intent bikeMonitorIntent = new Intent(getApplicationContext(), BikeMonitorService.class);
                        bindService(bikeMonitorIntent, mBMConnection, BIND_AUTO_CREATE);
                        //populate toast with selected ID number & dismiss dialog
                        unlockToastText = getString(R.string.unlockGoodToast_text) + bikeID;
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        //populate toast with warning
                        unlockToastText = getString(R.string.unlockBadToast_text);
                    }
                    //show toast
                    Toast toast = Toast.makeText(getApplicationContext(), unlockToastText, Toast.LENGTH_SHORT);
                    toast.show();
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
