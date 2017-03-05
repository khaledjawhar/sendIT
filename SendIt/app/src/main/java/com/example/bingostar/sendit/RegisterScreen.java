package com.example.bingostar.sendit;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.app.Activity;

/**
 * Created by bingostar on 2016-10-24.
 */
public class RegisterScreen extends Activity implements RequestResponse {

   // private static final String IDENT = "LoginScreen";

    private int serverPort;
    private String serverIp;

    private String phonenum, pass, email;

    private String phonenumText, passwordText, emailText;

    private Button registerButton;

    private TextView textUser;
    private TextView textPass;
    private TextView textEmail;

    private Register reg;

    private Intent resultIntent;

    private Bundle regData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        Intent intent = getIntent();
        Bundle userData = intent.getExtras();
        regData = new Bundle();

        serverPort = userData.getInt("SERVER_PORT");
        serverIp = userData.getString("SERVER_IP");

        registerButton = (Button) findViewById(R.id.btnReg);

        textUser = (TextView) findViewById(R.id.txtPhonenum);
        textPass = (TextView) findViewById(R.id.txtPassword);
        textEmail = (TextView) findViewById(R.id.txtEmail);

        final RegisterScreen t = this;

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //registerButton.setEnabled(false);
                if(setVariables()) {
                    reg = new Register(serverPort, serverIp, phonenum, pass, email);
                    reg.delegate = t;
                    reg.execute();
                }
            }
        });


    }

    @Override
    public void processLogin(Boolean resp) {}

    @Override
    public void processIp(String ip) {}

    @Override
    public void processRegister(Boolean resp) {
        if(resp) {
            resultIntent = new Intent();
            regData.putString("USER_PHONE_NUMBER", phonenum);
            regData.putString("PASSWORD", pass);
            resultIntent.putExtras(regData);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            resetVariables();
        }

    }

    public boolean setVariables(){

        phonenumText = textUser.getText().toString();
        passwordText = textPass.getText().toString();
        emailText = textEmail.getText().toString();

        if(phonenumText.equals("") || passwordText.equals("") || emailText.equals("")){
            return false;
        }
        else {
            phonenum = phonenumText;
            pass = passwordText;
            email = emailText;
            return true;
        }

    }

    public void resetVariables() {
        ((TextView) findViewById(R.id.txtPhonenum)).setText("");
        ((TextView) findViewById(R.id.txtPassword)).setText("");
        ((TextView) findViewById(R.id.txtEmail)).setText("");
    }


}
