package simplesendreceive.example.com.simplesendreceive;

import android.app.Activity;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import static android.R.string.ok;


public class MainActivity extends Activity {

    private Socket sendSocket;
    private InputStream in;
    private OutputStream out;
    private Button button;
    private int fileLoc = 0;
    private int bytesRead = 0;
    private byte[] receivedOK;
    private byte[] blockData;
    private int numberOfChunksReceived = 0;
    private int numberOfOKreceived = 0;
    public final int DATA_BLOCK_SIZE = 1000;
    private String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        button = (Button) findViewById(R.id.btnConnect);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilePickerSrc();
            }
        });
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

    public void send() {

        Log.e("MainActivity", "MY IP ADDRESS: " + getLocalIpAddress());

        try {
            sendSocket = new Socket("172.17.49.220", 8080);
            out = new DataOutputStream(sendSocket.getOutputStream());
            in = new DataInputStream(sendSocket.getInputStream());

            Log.e("MainActivity", "Sending...");
            Log.e("MainActivity", "Path: " + filePath);
            File file = new File(filePath);

            receivedOK = new byte[2];

            while(true){
                blockData = null;
                try {
                    System.out.println("Try " + (numberOfChunksReceived++));
                    blockData = readFromFile(DATA_BLOCK_SIZE);
                    out.write(blockData);
                    out.flush();
                    in.read(receivedOK);
                    numberOfOKreceived++;
                    Log.e("MainActivity", "Received: ");
                    for(int i = 0; i < receivedOK.length; i++){
                        Log.e("MainActivity", "" + (char) receivedOK[i]);
                    }
                    System.out.println("OK count: " + numberOfOKreceived);

                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                    return;
                }

                if(blockData.length < 1000) {
                    break;
                }
            }

            System.out.println();
            in.close();
            out.close();
            out.flush();
            Log.e("MainActivity", "Sent");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showFilePickerSrc() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
        dialog.setTitle("Select a File you want to send");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                Toast.makeText(getApplicationContext(), files[0], Toast.LENGTH_LONG).show();
                filePath = files[0].toString();
                //files is the array of the paths of files selected by the Application User.
                send();
            }
        });

        dialog.show();
    }

    public byte[] readFromFile(int chunkSize) throws FileNotFoundException, IOException, SecurityException
    {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filePath));

        byte[] readData = new byte[chunkSize];

        //First, check if we have permission to read the file, and return an empty array if not
        File check = new File(filePath);

        if(!check.exists()){
            check.createNewFile();
        }

        if(!check.canRead())
        {
            in.close();
            throw new SecurityException("You don't have permission to read this file. Please contact your system administrator.");
        }

        in.skip((long) fileLoc);
        //if there has been data read in from the file, move the marker. If not, reset the marker to 0

        if((bytesRead = in.read(readData)) != -1)
        {
            fileLoc += bytesRead;
        }
        else
        {
            fileLoc = 0;
            bytesRead = 0;
        }

        in.close();

        //trim the data if there's less than the chunk size and return the trimmed byte array
        if(bytesRead < chunkSize)
        {
            System.out.println("Trimming array of read-in data.");
            byte readDataTrim[] = Arrays.copyOf(readData, bytesRead);
            return readDataTrim;
        }

        return readData;
    }

}