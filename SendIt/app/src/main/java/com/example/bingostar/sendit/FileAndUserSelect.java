package com.example.bingostar.sendit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;

/**
 * Created by bingostar on 2016-10-07.
 */


public class FileAndUserSelect extends AppCompatActivity implements RequestResponse{

    private static final String LOG_TAG = "SendScreen";

    private int serverPort;
    private String serverIp, recIp;

    private String phonenum, recphonenum, pathtofile;

    Button buttonOpenDialog, buttonSend;
    TextView textViewFilePath, receiverNumber;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_and_user_select);

        Intent intent = getIntent();
        Bundle userData = intent.getExtras();

        serverPort = userData.getInt("SERVER_PORT");
        serverIp = userData.getString("SERVER_IP");
        phonenum = userData.getString("USER_PHONE_NUMBER");


        buttonOpenDialog=(Button)findViewById(R.id.openDialog);
        buttonSend = (Button)findViewById(R.id.btnSend);
        textViewFilePath=(TextView)findViewById(R.id.txtFilePath);
        receiverNumber=(TextView)findViewById(R.id.txtRec);

        //buttonSend.setEnabled(false);

        buttonOpenDialog.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showFilePickerSrc();
            }
        });
    }

    @Override
    public void processLogin(Boolean resp) {}

    @Override
    public void processIp(String ip) {

        Log.d(FileAndUserSelect.LOG_TAG, ip);
        recIp = ip;

    }

    @Override
    public void processRegister(Boolean resp) {}

    private void showFilePickerSrc() {
        DialogProperties properties=new DialogProperties();
        properties.selection_mode=DialogConfigs.SINGLE_MODE;
        properties.selection_type=DialogConfigs.FILE_SELECT;
        properties.root=new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions=null;
        FilePickerDialog dialog = new FilePickerDialog(FileAndUserSelect.this,properties);
        dialog.setTitle("Select a File you want to send");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Toast.makeText(getApplicationContext(), files[0], Toast.LENGTH_LONG).show();
                textViewFilePath.setText(files[0].toString());

                //files is the array of the paths of files selected by the Application User.
            }
        });

        dialog.show();
    }

    public void setVariables(){
        String ptfile = ((TextView) findViewById(R.id.txtFilePath)).getText().toString();
        String rphone = ((TextView) findViewById(R.id.txtRec)).getText().toString();

        if(ptfile != "" && rphone != ""){
            pathtofile = ptfile;
            recphonenum = rphone;
        } else {
            return;
        }

    }

    public void resetVariables(){
        ((TextView) findViewById(R.id.txtFilePath)).setText("");
        ((TextView) findViewById(R.id.txtRec)).setText("");
    }


}
