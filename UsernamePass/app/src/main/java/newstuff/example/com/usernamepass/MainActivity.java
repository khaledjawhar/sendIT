package newstuff.example.com.usernamepass;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {


    private String username;
    private String pass;
    private String ipAddress;

    private TextView textUser;
    private TextView textPass;
    private String usernameText;
    private String passwordText;
    private Button button;

    private byte[] usernameInBytes;
    private byte[] passwordInBytes;
    private byte[] IPinBytes;

    /*
	 * sendByteArray : Specify the request to be sent to the server for login
	 */
    private byte[] sendByteArray;

    /*
	 * receivedByteArray : Specify the ack that the server returns to the phone after login
	 * 				.
	 */
    private byte[] receivedByteArray;

    /*
	 * zeroCount : keeps track of the number of zeros in the request byte array
	 * sizeOfByteArrayReceived:  the size of the byte array received from the server after the initial login request
	 * 				.
	 */
    private int zeroCount = 0, sizeOfByteArrayReceived = 0, serverPort = 5555;

    private String serverName;

    private ServerSocket receiveSocket;
    private Socket sendReceiveSocket;

    private DataOutputStream dout;
    private DataInputStream din;

    private Intent intent;

    /*
	 * message : contains the message to be displayed after login
	 */
    private String message;

    /*
	 * AUTH_MESSAGE : Used in the passing of messages from one intent to the other
	 */
    public final static String AUTH_MESSAGE = "Authentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        username = "";
        pass = "";

        /* not quite sure what these two lines do, but they get rid of an error I was having in the connect function */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btnLogin);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVariables();
                if(connect()) {
                    intent = new Intent(MainActivity.this, LoginRedirection.class);
                    intent.putExtra(AUTH_MESSAGE, message);
                    startActivity(intent);
                }
            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
	 * Method name : setVariables
	 * Purpose:      Assigns the values the user enters on the login page to two variables
	 *
	 */
    public void setVariables(){
        textUser = (TextView) findViewById(R.id.txtUsername);
        textPass = (TextView) findViewById(R.id.txtPassword);
        usernameText = textUser.getText().toString();
        passwordText = textPass.getText().toString();

        if(usernameText != "" && passwordText != ""){
            username = usernameText;
            pass = passwordText;

        }
        else {
            return;
        }
    }

    /*
    * Method name : connect
    * Purpose:      Sends a login request to the server, receives a response from the server
    *
    */
    public boolean connect() {
       try {
            serverName = "172.17.220.221";//server IP
            sendReceiveSocket = new Socket(serverName, serverPort);
            dout = new DataOutputStream(sendReceiveSocket.getOutputStream());

            usernameInBytes = username.getBytes();
            passwordInBytes = pass.getBytes();
            ipAddress = InetAddress.getLocalHost().getHostAddress();

            IPinBytes = ipAddress.getBytes();

            sendByteArray = new byte[usernameInBytes.length + passwordInBytes.length + IPinBytes.length + 4];

            //formation of the request to be sent to the server
            sendByteArray = joinByteArrays(new byte[] {1},
                                    joinByteArrays(usernameInBytes,
                                            joinByteArrays(new byte[] {0},
                                                    joinByteArrays(passwordInBytes,
                                                            joinByteArrays(new byte[] {0},
                                                                    joinByteArrays(IPinBytes, new byte[] {0}))))));

            if(isValidRequest(sendByteArray) && sendReceiveSocket != null) {
                dout.writeInt(sendByteArray.length);//send the length of the byte array first
                dout.write(sendByteArray);
                dout.flush();
                dout.close();
                sendReceiveSocket.close();
            }
            else {
                return false;
            }


           receiveSocket = new ServerSocket(8888);
           sendReceiveSocket = receiveSocket.accept();

           din = new DataInputStream(sendReceiveSocket.getInputStream());

           sizeOfByteArrayReceived = din.readInt();//receive the length of the byte array first

           receivedByteArray = new byte[sizeOfByteArrayReceived];

           din.read(receivedByteArray, 0, sizeOfByteArrayReceived);


           din.close();
           receiveSocket.close();

            if(isValidAck(receivedByteArray)){
                if(receivedByteArray[1]  == 0) {
                    message = "Welcome " + username + "!";
                }
                else {
                    message = "Authentication failed";
                }
            }
            else {
                return false;
            }

        } catch(IOException e){
            e.printStackTrace();
        }
        return true;
    }

    /*
    * Method name : isValidRequest
    * Purpose:      Checks whether the request being sent to the server is in the right format
    * In:           request to be validated
    * Out:          true if it is a valid request, false otherwise
    */
    public boolean isValidRequest(byte[] requestContent) {
        zeroCount = 0;
        if(requestContent == null){
            return false;
        }

        if(requestContent[0] != 1){
            return false;
        }

        if(requestContent[requestContent.length - 1] != 0){
            return false;
        }

        for(int i = 0; i < requestContent.length; i++){
            if(requestContent[i] == 0){
                zeroCount++;
            }
        }

        if(zeroCount < 3){
            return false;
        }
        return true;
    }

    /*
    * Method name : isValidAck
    * Purpose:      Checks whether the response received from the server is in the right format
    * In:           packet to be validated
    * Out:          true if is it value, false otherwise
    */
    public boolean isValidAck(byte[] ack){
        if(ack == null){
            return false;
        }

        if(ack[0] == 1 && ack[1] == 0){
            return true;
        }

        return false;
    }

    /*
    * Method name : joinByteArrays
    * Purpose:      joins two byte arrays together
    * In:           source byte array oo
    * In:           destination byte array
    * Out:          result of merging the two arrays
    *
    */
    public byte[] joinByteArrays(byte[] src, byte[] dest){
        int length = src.length + dest.length;
        byte[] result = new byte[length];
        System.arraycopy(src, 0, result, 0, src.length);
        System.arraycopy(dest, 0, result, src.length, dest.length);

        return result;
    }

}
