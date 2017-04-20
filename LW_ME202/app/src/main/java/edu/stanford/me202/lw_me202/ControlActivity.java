package edu.stanford.me202.lw_me202;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

     //linking views
    @BindView(R.id.unlockButton) Button unlockButton;
    @BindView(R.id.connStatusText) TextView connStatusText;
    @BindView(R.id.bikeIDText) TextView bikeIDText;
    @BindView(R.id.lightModeSwitch) Switch lightModeSwitch;
    @BindView(R.id.lightStateSwitch) Switch lightStateSwitch;
    @BindView(R.id.lightModeText) TextView lightModeText;
    @BindView(R.id.lightStateText) TextView lightStateText;
    @BindView(R.id.historyButton) Button historyButton;

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
    protected void onResume() {
        super.onResume();

         //User clicked unlock button =>
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 //create unlocking dialog
                final Dialog dialog = new Dialog(ControlActivity.this);
                ButterKnife.bind(this,dialog);
                dialog.setTitle(R.string.unlockDialogTitle_text);
                dialog.setCancelable(false);
                 //link the dialog layout
                dialog.setContentView(R.layout.dialog_unlock);
                 //pull in the views
                final EditText unlockDialogEntry = (EditText) dialog.findViewById(R.id.unlockDialogEntry);
                final Button unlockEnterButton = (Button) dialog.findViewById(R.id.unlockEnterButton);
                final Button unlockCancelButton = (Button) dialog.findViewById(R.id.unlockCancelButton);

                 //if user clicks "Enter" button in dialog =>
                unlockEnterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String bikeID = unlockDialogEntry.getText().toString();
                        String unlockToastText;
                         //if a valid ID has been entered
                        if(!bikeID.equals("")){
                             //populate toast with selected ID number & dismiss dialog
                            unlockToastText = getString(R.string.unlockGoodToast_text) + bikeID;
                            dialog.dismiss();
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
                        dialog.dismiss();
                    }
                });

                 //show the dialog once it is created
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
                Intent rideHistIntent = new Intent(getApplicationContext(), RideHistoryActivity.class);
                startActivity(rideHistIntent);
            }
        });
    }
}
