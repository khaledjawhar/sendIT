package com.example.bingostar.sendit;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/*
 * connection handling needs to be done!!!
 *
 * change log in, figure out connection with the server
 *
 */

/**
 * Created by bingostar on 2016-10-19.
 */

public class LogIn extends AsyncTask<Void, Void, Void>  {

    public RequestResponse delegate = null;

    private int serverPort;
    private String serverIp;

    private OutputStream out;
    private InputStream in;

    private String phonenum;
    private String pass;

    private Boolean response;

    public LogIn(int sp, String si, String pn, String pw) {
        serverPort = sp;
        serverIp = si;
        phonenum = pn;
        pass = pw;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Task you want to do on UIThread after completing Network operation
        //onPostExecute is called after doInBackground finishes its task.
        delegate.processLogin(response);
    }

    @Override
    protected Void doInBackground(Void... params) {


        try {

            LoginScreen.sendReceiveServerSocket = new Socket(serverIp, serverPort);

            System.out.println(LoginScreen.sendReceiveServerSocket.getLocalAddress());

            out = LoginScreen.sendReceiveServerSocket.getOutputStream();
            in = LoginScreen.sendReceiveServerSocket.getInputStream();

            //create a log in request
            byte[] sendByteArray = PacketManager.createLoginRequestPacket(phonenum, pass);

            if(LoginScreen.sendReceiveServerSocket != null) {
                out.write(sendByteArray);
            } else {
                response = false;
                return null;
            }

            byte[] receivedByteArray = new byte[2];
            in.read(receivedByteArray);

            //needs to not be closed and passed back to LogInScreen
            //LoginScreen.sendReceiveServerSocket.close();

            try {
                if (!PacketManager.validateLoginOrRegisterRequestResponse(receivedByteArray)) {
                    //return false
                    response = false;
                    return null;
                }
            } catch(InvalidPacketDetected e){
                //return false
                response = false;
                return null;
            }


        } catch(IOException e){
            //return false
            response = false;
            return null;
        }

        response = true;
        return null;
    }


}
