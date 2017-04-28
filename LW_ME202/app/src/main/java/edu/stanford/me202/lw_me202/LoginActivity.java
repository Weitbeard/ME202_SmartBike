package edu.stanford.me202.lw_me202;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

     //linking views
    @BindView(R.id.loginNameEntry) EditText nameEntry;
    @BindView(R.id.loginPWEntry) EditText pwEntry;
    @BindView(R.id.loginButton) Button loginButton;
    @BindView(R.id.registerButton) Button registerButton;

     //constants for dummy authentication
    private final static String CORRECT_NAME = "lukeweit";
    private final static String CORRECT_PW = "123abc";
    private ArrayList<UserRecord> UserRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //set the proper layout
        setContentView(R.layout.activity_login);
         //bind views
        ButterKnife.bind(this);
         //populate registered users with default user
        UserRecords.add(new UserRecord(CORRECT_NAME,CORRECT_PW));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

         //user clicks the "Login" button =>
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String name = nameEntry.getText().toString();
                String pw = pwEntry.getText().toString();

                 //for each record in UserRecords
                for(int i = 0; i<UserRecords.size(); i++) {
                    //if the username and password are correct
                    if (name.equals(UserRecords.get(i).getName()) && pw.equals(UserRecords.get(i).getPw())) {
                        //change to control activity
                        startActivity(new Intent(getApplicationContext(), ControlActivity.class));
                        finish();
                        return;
                    }
                }
                 //if no user found put up a warning toast & clear the password contents
                Toast toast = Toast.makeText(getApplicationContext(), R.string.incorrectWarning_text, Toast.LENGTH_SHORT);
                toast.show();
                pwEntry.setText("");
            }
        });

        //user clicks the "Login" button =>
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //create & show unlocking dialog
                final Dialog dialog = new RegisterDialog(LoginActivity.this);
                dialog.show();
            }
        });
    }

    public class RegisterDialog extends Dialog {
        //unlock dialog views
        @BindView(R.id.registerNameEntry) EditText registerNameEntry;
        @BindView(R.id.registerPWEntry) EditText registerPWEntry;
        @BindView(R.id.registerEnterButton) Button registerEnterButton;
        @BindView(R.id.registerCancelButton) Button registerCancelButton;

        public RegisterDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle(R.string.registerDialogTitle_text);
            setCancelable(false);
            //link the dialog layout
            setContentView(R.layout.dialog_register);
            //bind views
            ButterKnife.bind(this);

            //if user clicks "Enter" button in dialog =>
            registerEnterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String registerToastText;
                    String newName = registerNameEntry.getText().toString();
                    String newPW = registerPWEntry.getText().toString();
                    //if a valid ID has been entered
                    if(!newName.equals("") && !newPW.equals("")){
                        //start BLE service to connect with monitor
                        UserRecords.add(new UserRecord(registerNameEntry.getText().toString(), registerPWEntry.getText().toString()));
                        //populate toast with registration affirmation
                        registerToastText = getString(R.string.registerGoodToast_text);
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        //populate toast with warning
                        registerToastText = getString(R.string.registerBadToast_text);
                    }
                    //show toast
                    Toast toast = Toast.makeText(getApplicationContext(), registerToastText, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

            //if user clicks the "Cancel" button in the dialog =>
            registerCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //dismiss the dialog
                    dismiss();
                }
            });
        }
    }

    public class UserRecord{
        private String name;
        private String pw;

        public UserRecord(String name, String pw) {
            this.name = name;
            this.pw = pw;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPw() {
            return pw;
        }

        public void setPw(String pw) {
            this.pw = pw;
        }
    }
}
