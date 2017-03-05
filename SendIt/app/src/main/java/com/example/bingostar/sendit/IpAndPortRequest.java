package com.example.bingostar.sendit;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import android.util.Log;

/**
 * Created by bingostar on 2016-10-19.
 */
public class IpAndPortRequest extends AsyncTask<Void, Void, Void> {
    public RequestResponse delegate = null;

    private int serverPort;
    private String serverIp;
    private Socket sendReceiveSocket;
    private OutputStream out;
    private InputStream in;

    private String senderNum;
    private String receiverNum;
    private String receiverIp;

    private String response;

    public IpAndPortRequest(int sp, String si, String sn, String rn) {
        serverPort = sp;
        serverIp = si;
        senderNum = sn;
        receiverNum = rn;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Task you want to do on UIThread after completing Network operation
        //onPostExecute is called after doInBackground finishes its task.
        if(receiverIp != null) {

            delegate.processIp(receiverIp);
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            sendReceiveSocket = new Socket(serverIp, serverPort);

            out = sendReceiveSocket.getOutputStream();
            in = sendReceiveSocket.getInputStream();

            //create a log in request
            byte[] sendByteArray = PacketManager.createIPRequestPacket(senderNum, receiverNum);

            if(sendReceiveSocket != null) {
                out.write(sendByteArray);
            }

            byte[] receivedByteArray = new byte[100];
            in.read(receivedByteArray);

            sendReceiveSocket.close();

            try {
                receiverIp = PacketManager.validateAndParseIPRequestResponse(receivedByteArray);


            } catch(InvalidPacketDetected e){
                //return false

            } catch(UserNotInSystem e) {
                //return false

            } catch(InvalidIPAddress e) {
                //return false

            } catch(UnsupportedEncodingException e) {
                //return false

            }


        } catch(IOException e){
            //return false
        }
        return null;
    }


}
