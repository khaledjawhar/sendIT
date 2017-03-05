package newstuff.example.com.usernamepass;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class LoginRedirection extends AppCompatActivity {


    private String message;
    private Intent intent;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_redirection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        message = intent.getStringExtra(MainActivity.AUTH_MESSAGE);
        textView = (TextView) findViewById(R.id.txtResult);
        textView.setTextSize(30);
        textView.setText(message);

    }

}
