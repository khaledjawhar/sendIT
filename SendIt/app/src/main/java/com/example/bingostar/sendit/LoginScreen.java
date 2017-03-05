package com.example.bingostar.sendit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.app.Activity;

import java.net.Socket;

public class LoginScreen extends AppCompatActivity implements RequestResponse {

    public static Socket sendReceiveServerSocket;

    private static final String LOG_TAG = "LoginScreen";
    private static final int REGISTER_REQUEST = 1;
    private static final int LOGIN_REQUEST = 2;

    private int serverPort = 5054;
    private String serverIp = "69.158.12.171";

    private String phonenum;
    private String pass;

    private TextView textUser;
    private TextView textPass;

    private String phonenumText;
    private String passwordText;

    private Button signUpButton, logInButton;

    private Intent fileScreen, registerScreen;
    private Bundle userData;

    LogIn lin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        logInButton = (Button) findViewById(R.id.btnLogin);
        signUpButton = (Button) findViewById(R.id.btnSignUp);

        textUser = (TextView) findViewById(R.id.txtPhonenum);
        textPass = (TextView) findViewById(R.id.txtPassword);

        fileScreen = new Intent(LoginScreen.this, FileAndUserSelect.class);
        registerScreen = new Intent(LoginScreen.this, RegisterScreen.class);

        final LoginScreen t = this;

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logInButton.setEnabled(false);
                if(setVariables()) {
                    lin = new LogIn(serverPort, serverIp, phonenum, pass);
                    lin.delegate = t;
                    lin.execute();
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userData = new Bundle();

                userData.putInt("SERVER_PORT", serverPort);
                userData.putString("SERVER_IP", serverIp);

                registerScreen.putExtras(userData);

                startActivityForResult(registerScreen, REGISTER_REQUEST);

            }
        });
    }

    @Override
    public void processLogin(Boolean resp) {
        if(resp){
            Log.d(LoginScreen.LOG_TAG, "SUCCESS");

            userData.putString("USER_PHONE_NUMBER", phonenum);
            userData.putString("SERVER_IP", serverIp);
            userData.putInt("SERVER_PORT", serverPort);

            fileScreen.putExtras(userData);

            startActivity(fileScreen);
        } else {
            Log.d(LoginScreen.LOG_TAG, "FAILURE");
            resetVariables();
        }
        //logInButton.setEnabled(true);
    }

    @Override
    public void processIp(String ip) {}

    @Override
    public void processRegister(Boolean resp) {}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REGISTER_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {
                Bundle idata = data.getExtras();

                textUser.setText(idata.getString("USER_PHONE_NUMBER"));
                textPass.setText(idata.getString("PASSWORD"));
            }
        } else if(requestCode == LOGIN_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {

            }
        }
    }

    /*
	 * Method name : setVariables
	 * Purpose:      Assigns the values the user enters on the login page to two variables
	 *
	 */
    public boolean setVariables(){

        phonenumText = textUser.getText().toString();
        passwordText = textPass.getText().toString();

        if(phonenumText.equals("") || passwordText.equals("")){
            return false;
        }
        else {
            phonenum = phonenumText;
            pass = passwordText;
            return true;
        }
    }

    public void resetVariables() {
        textUser.setText("");
        textPass.setText("");
    }
}
