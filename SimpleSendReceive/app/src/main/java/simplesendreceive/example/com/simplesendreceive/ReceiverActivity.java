package simplesendreceive.example.com.simplesendreceive;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Selasi on 2016-10-23.
 */


public class ReceiverActivity extends Activity {

    private Socket clientSocket;
    private ServerSocket listening;
    private InputStream in;
    private OutputStream out;
    private Button button;
    private TextView textView;
    private byte[] receivedData;
    private int numberOfOKReceived = 0;
    private String fileName = "/cn02bb.pdf";
    public final int DATA_CHUNK_SIZE = 1000;
    private boolean isNewFile;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        textView = (TextView) findViewById(R.id.txtIP);

        button = (Button) findViewById(R.id.btnIP);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePickerDest();
            }
        });
    }

    public void start() {
        try {
            Log.e("ReceiverActivity", "MY IP ADDRESS: " + getLocalIpAddress());
            Log.e("ReceiverActivity", "Connecting...");
            listening = new ServerSocket(8080);
            clientSocket = listening.accept();
            Log.e("ReceiverActivity", "Connected");

            textView.setText("MY IP ADDRESS: " + getLocalIpAddress());

            Log.e("ReceiverActivity", "Receiving...");

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            receivedData = new byte[DATA_CHUNK_SIZE];

            while ((in.read(receivedData)) > 0) {
                try {
                    writeToFile(receivedData);
                    out.write("OK".getBytes());
                    Log.e("ReceiverActivity", "Read " + (numberOfOKReceived++));

                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                Log.e("ReceiverActivity", "Does this work?");
                receivedData = new byte[DATA_CHUNK_SIZE];

            }
            Log.e("ReceiverActivity", "Received.");

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLocalIpAddress() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

    public void writeToFile(byte[] data) throws IOException, SecurityException
    {
        BufferedOutputStream out;
        File check = new File(fileName);

        //check first if the file already exists, so we know whether to write or append to the file
        if(check.exists())
        {
            //if the new file flag is set, we know that this is not a continuation of a file in the middle of being
            //transferred.
            if(isNewFile)
            {
                throw new SecurityException("Cannot write this file since it already exists.");
            }
            //next, check if we have permission to write to the file
            if(check.canWrite())
            {
                //set the output stream to append to the current file
                out = new BufferedOutputStream(new FileOutputStream(fileName, true));
            }
            else
            {
                throw new SecurityException("Do not have write privileges for this file. Please contact your system administrator.");
            }
        }
        else
        {
            //create a new file
            check.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(fileName));
        }

        out.write(data, 0, data.length);
        isNewFile = false;
        out.close();
    }

    private void showFilePickerDest() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(ReceiverActivity.this,properties);
        dialog.setTitle("Select where you want to save the file");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Toast.makeText(getApplicationContext(), files[0], Toast.LENGTH_LONG).show();
                fileName = files[0].substring(0,files[0].lastIndexOf("/")).toString()+"/cn02bb.pdf";
                //files is the array of the paths of files selected by the Application User.
                start();
            }
        });
        dialog.show();
    }
}
