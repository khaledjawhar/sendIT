package com.example.bingostar.sendit; /**
 * Created by bingostar on 2016-09-30.
 *
 *
 */

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.UnsupportedEncodingException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/*
Register request packet (from client): [3, phone-number-as-bytes, 0, password-as-bytes, 0, email-address-as-bytes, 0]
Register request response packet (from server): success -> [0, 1] failure -> [0,2]

Login is the same as register except in the request packet the first byte is 1 not 3.

Send request packet (from client): [2, phone-number-of-sender-as-bytes, 0, phone-number-of-receiver-as-bytes, 0]
Send request response packet (from server): success -> [0, 1, phone-number-of-receiver-as-bytes, 0, ip:port-of-receiver-as-bytes, 0] failure -> [0,2]
*/

public class PacketManager {

    private static final byte zero = 0; //zero byte
    private static final byte loginCode = 1;
    private static final byte connectionCode = 2; //leading byte for a connection request
    private static final byte registerCode = 3; //leading byte for an ack message
    private static final byte sendCode = 4; //leading byte for a data message


    public static byte[] createRequestPacket(byte id, String number, String passornum) {

        List<Byte> p = new ArrayList<Byte>();
        p.add(id);
        byte[] bytes = number.getBytes();
        for (Byte b : bytes) {
            p.add(b);
        }
        p.add(zero);
        bytes = passornum.getBytes();
        for (Byte b : bytes) {
            p.add(b);
        }
        p.add(zero);
        Byte[] Bytes = p.toArray(new Byte[p.size()]);
        return ArrayUtils.toPrimitive(Bytes);
    }

    public static byte[] createRequestPacket(byte id, String number, String passornum, String email) {
        List<Byte> p = new ArrayList<Byte>();
        p.add(id);
        byte[] bytes = number.getBytes();
        for (Byte b : bytes) {
            p.add(b);
        }
        p.add(zero);
        bytes = passornum.getBytes();
        for (Byte b : bytes) {
            p.add(b);
        }
        p.add(zero);
        bytes = email.getBytes();
        for (Byte b : bytes) {
            p.add(b);
        }
        p.add(zero);
        Byte[] Bytes = p.toArray(new Byte[p.size()]);
        return ArrayUtils.toPrimitive(Bytes);

    }

    /*
+    * Method name : createLoginRequestPacket
+    * Purpose:      returns the byte array storing a log-in request
+    * In:           phone number(String) and password(String)
+    * Out:          byte array of the form [1][phone number][0][password][0]
+    */
    public static byte[] createLoginRequestPacket(String phoneNumber, String password) {

       return createRequestPacket(loginCode, phoneNumber, password);

    }

    /*
+    * Method name : createIPRequestPacket
+    * Purpose:      returns the byte array storing a file-send request
+    * In:           phone number of sender(String) and phone number of receiver(String)
+    * Out:          byte array of the form [2][receiver phone number][0]
+    */

    public static byte[] createIPRequestPacket(String phoneNumberOfSender, String phoneNumberOfReceiver) {

        return createRequestPacket(connectionCode, phoneNumberOfSender, phoneNumberOfReceiver);

    }

    public static byte[] createRegisterRequestPacket(String phoneNumber, String password, String email) {
        return createRequestPacket(registerCode, phoneNumber, password, email);
    }

    /*
+    * Method name : validateLoginRequestResponse
+    * Purpose:      to validate a response fom the server after sending a request to log in
+    * In:           response(byte[]), valid if either one of the following forms:
                            1) [1][0][0] //log in successful
                            2) [1][1][0] //log in failure
+    * Out:          true if successful, false if failure
     * Throws: InvalidPacketDetected
+    */
    public static boolean validateLoginOrRegisterRequestResponse(byte[] response)
            throws InvalidPacketDetected{

        if(response[0] != 0) { throw new InvalidPacketDetected(); }
        else if(response.length < 2) { throw new InvalidPacketDetected(); }
        else if(response[1] < 0) { throw new InvalidPacketDetected(); }
        else if (response[1] == 2) { return false; }
        else {
            return true;
        }

    }

    /*
+    * Method name : validateAndParseIPRequestResponse
+    * Purpose:      to validate a response fom the server after sending a request to see if it is
                     possible to connect to the desired user
+    * In:           response(byte[]), valid if either one of the following forms:
                            1) [2][0][ip address of receiver in bytes][0] //desired user in the system
                            2) [2][1][0] //desired user not in the system
+    * Out:          ip address as a string
     * Throws: InvalidPacketDetected, UserNotInSystem, InvalidIPAddress
+    */
    public static String validateAndParseIPRequestResponse(byte[] response)
            throws InvalidPacketDetected, UserNotInSystem, InvalidIPAddress, UnsupportedEncodingException {

        if(response[0] != connectionCode) { throw new InvalidPacketDetected(); }
        else if(response.length < 2) { throw new InvalidPacketDetected(); }
        else if(response[1] < 0 || response[1] > 1) { throw new InvalidPacketDetected(); }
        else if (response[1] == 1) { throw new UserNotInSystem();}
        else { //the packet is presumed to be valid so far and the user is in the system
            byte[] ipBytes = Arrays.copyOfRange(response, 2, response.length-1); //get the ip address from the packet
            try {
                String ip = new String(ipBytes, "UTF-8"); //store the ip address in a string
                if(InetAddressValidator.getInstance().isValid(ip)){ //check if ip is a valid ip address
                    return ip;
                } else {
                    throw new InvalidIPAddress();
                }
            } catch(UnsupportedEncodingException e) { throw e; } //error with encoding (not ascii)
        }
    }

    /*
     * TO BE IMPLEMENTED
     */
    public byte[] createSendPacket() {
        byte[] packet = {};
        return packet;
    }

    /*
     * TO BE IMPLEMENTED
     */
    public byte[] createReceivePacket() {
        byte[] packet = {};
        return packet;
    }

}

//register 3 phone number 0 password 0 email 0

//login and register success from server 0 1 failure 0 2
// send request  success 0 1 receiver phone number 0 ip:port 0 failure 0 2
//port forwarding