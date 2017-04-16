package edu.stanford.me202.lw_me202;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText nameEntry;
    private EditText pwEntry;
    private Button loginButton;
    private static String CORRECT_NAME = "lukeweit";
    private static String CORRECT_PW = "123abc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         //set the correct layout
        setContentView(R.layout.activity_login);
         //pull in the views
        nameEntry = (EditText) findViewById(R.id.loginNameEntry);
        pwEntry = (EditText) findViewById(R.id.loginPWEntry);
        loginButton = (Button) findViewById(R.id.loginButton);
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

                 //if the username and password are correct
                if(name.equals(CORRECT_NAME) && pw.equals(CORRECT_PW)){
                     //change to control activity
                    Intent controlIntent = new Intent(getApplicationContext(), ControlActivity.class);
                    startActivity(controlIntent);
                    finish();
                }
                 //if they are incorrect
                else{
                     //put up a warning toast & clear the password contents
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.incorrectWarning_text, Toast.LENGTH_SHORT);
                    toast.show();
                    pwEntry.setText("");
                }
            }
        });
    }
}
