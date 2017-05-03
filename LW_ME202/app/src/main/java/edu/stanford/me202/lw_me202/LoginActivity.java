package edu.stanford.me202.lw_me202;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth bikeAuth;

     //linking views
    @BindView(R.id.loginNameEntry) EditText nameEntry;
    @BindView(R.id.loginPWEntry) EditText pwEntry;
    @BindView(R.id.loginButton) Button loginButton;
    @BindView(R.id.registerButton) Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //set the proper layout
        setContentView(R.layout.activity_login);
         //bind views
        ButterKnife.bind(this);
         //get Firebase instance
        bikeAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
         //check if user is signed in already
        FirebaseUser currentUser = bikeAuth.getCurrentUser();
         //populate text fields with current user creds
        updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();

         //user clicks the "Login" button =>
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                 //attempt to login with entered credentials
                String email = nameEntry.getText().toString();
                String pw = pwEntry.getText().toString();
                logInUser(email, pw);
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
                        //start registration for the user
                        registerUser(newName,newPW);
                        //dismiss the dialog
                        dismiss();
                    }
                    //if an invalid ID
                    else{
                        //populate toast with warning
                        registerToastText = getString(R.string.registerBadToast_text);
                        //show toast
                        Toast toast = Toast.makeText(getApplicationContext(), registerToastText, Toast.LENGTH_SHORT);
                        toast.show();
                    }
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

     //register the user via Firebase
    private void registerUser(String email, String pw){
        bikeAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Successfully registered!",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = bikeAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

     //log in the user via Firebase
    private void logInUser(String email, String pw){
        bikeAuth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if login was successful
                        if(task.isSuccessful()){
                            //update UI for the user
                            FirebaseUser user = bikeAuth.getCurrentUser();
                            updateUI(user);
                            //change to control activity
                            startActivity(new Intent(getApplicationContext(), ControlActivity.class));
                            finish();
                        }
                        //if login failed
                        else{
                            //show a warning toast and clear the password entry
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.incorrectWarning_text, Toast.LENGTH_SHORT);
                            toast.show();
                            pwEntry.setText("");
                        }
                    }
                });
    }

     //update the application UI for the current user
    private void updateUI(FirebaseUser user) {
         //for now, just enter the email into the text entry & clear the pw entry
        if (user != null) {
            nameEntry.setText(user.getEmail());
            pwEntry.setText("");
        }
    }
}
