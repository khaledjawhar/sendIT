package com.example.bingostar.sendit;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by bingostar on 2016-10-24.
 */
public class Register extends AsyncTask<Void, Void, Void> {

    public RequestResponse delegate = null;

    private int serverPort;
    private String serverIp;
    private Socket sendReceiveSocket;
    private OutputStream out;
    private InputStream in;

    private String phonenum, pass, email;

    private Boolean response;

    public Register(int sp, String si, String pn, String pw, String em) {
        serverPort = sp;
        serverIp = si;
        phonenum = pn;
        pass = pw;
        email = em;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Task you want to do on UIThread after completing Network operation
        //onPostExecute is called after doInBackground finishes its task.
        delegate.processRegister(response);
    }

    @Override
    protected Void doInBackground(Void... params) {


        try {

            sendReceiveSocket = new Socket(serverIp, serverPort);

            out = sendReceiveSocket.getOutputStream();
            in = sendReceiveSocket.getInputStream();

            //create a log in request
            byte[] sendByteArray = PacketManager.createRegisterRequestPacket(phonenum, pass, email);

            if(sendReceiveSocket != null) {
                out.write(sendByteArray);
            } else {
                response = false;
                return null;
            }

            byte[] receivedByteArray = new byte[2];
            in.read(receivedByteArray);

            System.out.println(receivedByteArray[0] + " " + receivedByteArray[1]);
            sendReceiveSocket.close();

            try {
                if (!PacketManager.validateLoginOrRegisterRequestResponse(receivedByteArray)) {
                    //return false
                    System.out.println("1");
                    response = false;
                    return null;
                }
            } catch(InvalidPacketDetected e){
                //return false
                System.out.println("2");
                response = false;
                return null;
            }


        } catch(IOException e){
            //return false
            System.out.println("3");
            response = false;
            return null;
        }

        response = true;
        return null;
    }
}
