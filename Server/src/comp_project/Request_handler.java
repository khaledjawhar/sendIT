package comp_project;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class Request_handler implements Runnable{
	
	private Socket usr_socket = null;
	/*
	 * socket: Specify the socket used to communicate with a client
	 */
	private Server parent_server = null;
	/*
	 * parent_server: Specify the parent server that spawned this handler
	 */
	public final static int OP_FLAG_IDX = 1;
	/*
	 * OP_FLAG_IDX : specify the index of the flag reflecting the
	 * 				 operation desired (connect/request/ack).
	 */	
	private String current_user = null;
	/*
	 * current_user: Specify the user id/name that is requesting a service
	 * 				 for this server
	 */
	private ConnRegistry usr_registry = null;
	/*
	 * registry: user registry passed by the client.
	 */
	
	
	
	public Request_handler (Socket clientsocket, Server server, ConnRegistry registry) {
		usr_socket = clientsocket;
		parent_server = server;
		usr_registry = registry;
	}
	
	private static boolean is_valid_req_msg (byte[] buffer,int buff_size) {
		int expected_num_args = 2;
			/*
			 * expected_num_args: Specify the number of arguments expected in the message
			 */
		
		if (buff_size > 5 && buffer[0] == 2) {
			if (buffer[buff_size-1] == 0) {
				expected_num_args--;
				buff_size -= 2;
				
				while (buff_size > 0 ) {	
					if (buffer[buff_size] == 0) {
						expected_num_args--;
						if (expected_num_args < 0)
							return false;
					} 
					buff_size--;
				}
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean is_valid_message(byte[] buffer,int buff_size) {
		int expected_num_args = 3;
		/*
		 * expected_num_args: Specify the number of arguments expected in the message
		 */
		
		if (buff_size > 5 && (buffer[0] == 1 || buffer[0] == 2 || buffer[0] == 3)) {
			expected_num_args = 3;
			if (buffer[buff_size-1] == 0) {
				expected_num_args--;
				buff_size -= 2;
				
				while (buff_size > 0 ) {
					
					if (buffer[buff_size] == 0) {
						expected_num_args--;
						if (expected_num_args < 0)
							return false;
					} 
					buff_size--;
				}
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * get_usr_id() : extract the user id/name from the client request and return it.
	 */
	private String get_usr_id (byte[] buffer) {
		byte[] user_id = null;
			/*
			 * user_id : Specify the byte array that will store the user name.
			 */
		int buff_idx = 0;
			/*
			 * buff_idx: specify the index of the current dealing byte in the buffer.
			 */

		user_id = new byte[1024];
		buff_idx = OP_FLAG_IDX;

		/* Get to the end of the user id */
		while ( buff_idx < buffer.length && buffer[buff_idx] != 0){
			buff_idx++;
		};

		System.arraycopy(buffer, OP_FLAG_IDX , user_id, 0, buff_idx - OP_FLAG_IDX );
		user_id = Arrays.copyOf(user_id, buff_idx - OP_FLAG_IDX);

		return new String(user_id);	
	}
	 
	
	/*
	 * get_usr_password() : extract the user id/name from the client request and return it.
	 */
	public static String get_second_arg (byte[] buffer) {
		byte[] user_pass = null;
			/*
			 * user_pass : Specify the byte array that will store the user password.
		 	 */
		int buff_idx = 0;
			/*
			 * buff_idx: specify the index of the current dealing byte in the buffer.
			 */
		int num_hit = 0;
			/*
			 * Specify the number of times a 0 is hit
			 */

		user_pass = new byte[1024];
		buff_idx = OP_FLAG_IDX + 1;
		
		int tmp_idx = 0;

		/* Get to the end of the file name */
		while ( buff_idx < buffer.length && num_hit != 2){
			buff_idx++;
			if (buffer[buff_idx] == 0) {
				if (num_hit == 0)
					tmp_idx = buff_idx;
				num_hit++;
			}
		};

		System.arraycopy(buffer, OP_FLAG_IDX + tmp_idx, user_pass, 0, buff_idx - OP_FLAG_IDX - 1);
		user_pass = Arrays.copyOf(user_pass, buff_idx - OP_FLAG_IDX - tmp_idx);

		return new String(user_pass);	
	}
	
	/*
	 * get_usr_password() : extract the user id/name from the client request and return it.
	 */
	public static String get_third_arg (byte[] buffer) {
		byte[] argument = null;
			/*
			 * user_pass : Specify the byte array that will store the user password.
		 	 */
		int buff_idx = 0;
			/*
			 * buff_idx: specify the index of the current dealing byte in the buffer.
			 */
		int num_hit = 0;
			/*
			 * Specify the number of times a 0 is hit
			 */

		argument = new byte[1024];
		buff_idx = OP_FLAG_IDX + 1;
		
		int tmp_idx = 0;

		/* Get to the end of the file name */
		while ( buff_idx < buffer.length && num_hit !=3){
			buff_idx++;
			if (buffer[buff_idx] == 0) {
				if (num_hit == 1)
					tmp_idx = buff_idx;
				num_hit++;
			}
		};

		System.arraycopy(buffer, OP_FLAG_IDX + tmp_idx, argument, 0, buff_idx - OP_FLAG_IDX - 1);
		argument = Arrays.copyOf(argument, buff_idx - OP_FLAG_IDX - tmp_idx);

		return new String(argument);	
	}
	
	private boolean send_reply_msg (String username, OutputStream out_stream, byte[] buffer, boolean isEOK) {
		if ( out_stream != null & buffer != null) {
			try {
				buffer[0] = 0;
				buffer[1] = (byte) ((isEOK)?1:2);
			
				out_stream.write(buffer, 0, ServerCommons.STATE_MSG_LEN);
				out_stream.flush();
				return true;
			} catch (IOException e) {
				System.out.println("Failed to send EOK to: " + username);
			}
		}
		return false;
	}

	@Override
	public void run() {
		
		InputStream in_stream = null;
		/*
		 * in_stream: specify the InputStream that will be used to read
		 * 			 from "req_file" in case of a read request.
		 */
		OutputStream  out_stream = null;
		/*
		 * writer: specify the PrintWriter used to write to the the client_socket
		 */
		byte[] rcv_buffer = new byte[1024];
		/*
		 * rcv_buffer : Specify the content of the socket message.
		 */
		
		int buffer_size = 0;
		/* 
		 * buffer_size: Specify the number of bytes received/sent from/to user
		 */
		String user_passwd = null;
		/* 
		 * user_passwd: Specify the user password sent for an authentification request.
		 */
		int num_bad_attemps = 0;
		/*
		 * 3 bad attemps consequent and we kick out the users
		 */
		String user_ip = null;
		
		try {
			System.out.println("User ip is: " + usr_socket.getRemoteSocketAddress() +":" + usr_socket.getPort());
			in_stream = usr_socket.getInputStream();
			out_stream = usr_socket.getOutputStream();
			
			
			buffer_size = in_stream.read(rcv_buffer);
			
			if (!is_valid_message(rcv_buffer, buffer_size)) {
				System.out.println("Received invalid message: ");
				out_stream.write(ServerCommons.INVALID_MSG);
				out_stream.flush();
			}
			
			

			if (rcv_buffer[0] == 1) {
				/*
				 * Dealing with a user connect request
				 */
				current_user =  get_usr_id(rcv_buffer);
				user_passwd = get_second_arg(rcv_buffer);
				user_ip = get_third_arg(rcv_buffer);
				
				System.out.println("username/password received: "+ current_user + "//" +get_second_arg(rcv_buffer));
				
				
				if (usr_registry.authentificate(current_user, user_passwd)) {
					usr_registry.add_connected(current_user, usr_socket, user_ip);
					send_reply_msg(current_user, out_stream, rcv_buffer, true);
				} else {
					send_reply_msg(current_user, out_stream, rcv_buffer, false);
					usr_socket.close();
					return;
				}
				
			} else if (rcv_buffer[0] == 3) {
				/*
				 * Dealing with a register request
				 */
				current_user =  get_usr_id(rcv_buffer);
				user_passwd = get_second_arg(rcv_buffer);
				
				send_reply_msg(current_user, out_stream, rcv_buffer, 
						usr_registry.register_user(current_user, user_passwd));
				usr_socket.close();
				return;
			} else {
				send_reply_msg(current_user, out_stream, rcv_buffer, false);
			}
			
			while (true) {
				byte[] host_ip = null;
				int wrt_buff_idx = 0;
				
				buffer_size = in_stream.read(rcv_buffer);
				if (usr_socket.isClosed()) {
					throw new IOException("TODO");
				}
				System.out.println("Received querry from user: " + current_user);
				
				if (is_valid_req_msg(rcv_buffer, buffer_size)) {
					num_bad_attemps = 0;
					wrt_buff_idx = 0;
					String looked_for_usr_name = get_second_arg(rcv_buffer);
					host_ip = usr_registry.get_user_ip(looked_for_usr_name);
					rcv_buffer[wrt_buff_idx++] = 0;
					
					if (host_ip == null) {
						rcv_buffer[wrt_buff_idx++] = 2;
					} else {
						rcv_buffer[wrt_buff_idx++] = 1;
						System.arraycopy(host_ip, 0, rcv_buffer, wrt_buff_idx, host_ip.length);
						wrt_buff_idx += host_ip.length;
						rcv_buffer[wrt_buff_idx++] = 0;
					}
					
					out_stream.write(rcv_buffer, 0 , wrt_buff_idx);
					out_stream.flush();
				}else {
					System.out.println("Received wrong message ignoring...");
					num_bad_attemps++;
				}
				
//				try{
//					InetAddress ia = usr_socket.getInetAddress();
//					int port = usr_socket.getPort();
//					Socket s = new Socket(ia, port);
//					OutputStream a = s.getOutputStream();
//					a.write("aaaaaaaaa".getBytes());
//				} catch (IOException e ) {
//					e.printStackTrace();
//				}
				if (num_bad_attemps >=3) {
					throw new IOException("TODO");
				}
				
			}
		
	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} finally {
			try {
				in_stream.close();
				out_stream.close();
				usr_socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("user: " + current_user + "  disconnected!");
			usr_registry.remove_disconnected_user(current_user);
					
		}
		
		
		
		
	}
	

}
