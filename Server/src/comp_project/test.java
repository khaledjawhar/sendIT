package comp_project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class test {
	
	public static void main (String[] args) throws InterruptedException {
		System.out.println("Running the test");
		Socket sockt = null;
		Socket sockt2 = null;
		InputStream in_stream;
		BufferedReader in = null;
		InputStream in2 = null;
		OutputStream out = null, out2 = null;
		byte[] rcv_msgs = null;
		byte[] rcv_msgs2 = null;
		byte[] register_msg = "adefault/defaultalola".getBytes();
		byte[] connect_msg =  "adefault/defaulta192.168.0.1:5555a".getBytes();
		byte[] connect_msg2 =  "aamin/defaulta192.192.192.192:4444a".getBytes();
		byte[] request_msg2 =  "aamin/defaulta".getBytes();
		connect_msg[0] = 1;
		connect_msg[8] = 0;
		connect_msg[16] = 0;
		connect_msg[33] = 0;
		
		register_msg[0] = 3;
		register_msg[8] = 0;
		register_msg[16] = 0;
		register_msg[20] = 0;
		
		request_msg2[0] = 2;
		request_msg2[5] = 0;
		request_msg2[request_msg2.length-1] = 0;
		
		connect_msg2[0] = 1;
		connect_msg2[5] = 0;
		connect_msg2[13] = 0;
		connect_msg2[connect_msg2.length-1] = 0;
		
		try {
			//sockt = new Socket(InetAddress.getByName("184.144.101.130"), 5054);
			/*
			 * Register
			 */
			sockt = new Socket("localhost",5053);

			in = new BufferedReader(
			            new InputStreamReader(sockt.getInputStream()));
			
			out = sockt.getOutputStream();
			
			out.write(register_msg);
			out.flush();
			
			rcv_msgs = in.readLine().getBytes();
			System.out.println("C1:received: " + ((rcv_msgs[1] == 1)?"OK":"NOT OK"));
			
			in.close();
			out.close();
			sockt.close();
			/*
			 * Authentificate
			 */
			sockt = new Socket("localhost",5053);
			rcv_msgs = new byte[5];
			in_stream = sockt.getInputStream();
			
			out = sockt.getOutputStream();
			
			out.write(connect_msg);
			out.flush();
			in_stream.read(rcv_msgs);
			//rcv_msgs = in.readLine().getBytes();
			System.out.println("C1:received: " + ((rcv_msgs[1] == 1)?"OK":"NOT OK"));
			
			
			/*
			 * Request MSG testing
			 */
			sockt2 =  new Socket("localhost",5053);
			sockt2.setReuseAddress(true);
	
			rcv_msgs2 = new byte[20];
			in2 = sockt2.getInputStream();
		
			out2 = sockt2.getOutputStream();
			
			out2.write(connect_msg2);
			out2.flush();
			in2.read(rcv_msgs2);
			System.out.println("C2: received: " + ((rcv_msgs2[1] == 1)?"OK":"NOT OK"));
			
			out2.write(request_msg2);
			out2.flush();
			in2.read(rcv_msgs2);
			System.out.println("C2: received: " + ((rcv_msgs2[1] == 1)?"OK ":"NOT OK ") + new String(rcv_msgs2));
			in2.read(rcv_msgs2);
			System.out.println("C2: received: " + ((rcv_msgs2[1] == 1)?"OK ":"NOT OK ") + new String(rcv_msgs2));
			
			out2.write("congratsssssssssssss".getBytes());
			out2.flush();
			
			Thread.sleep(100000);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (in!=null) {
					in.close();
					out.close();
				}
				sockt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}

}
