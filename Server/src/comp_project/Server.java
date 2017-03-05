package comp_project;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
	
	ServerSocket rcv_socket = null;
	/*
	 * rcv_socket : Specify the socket that the server will use
	 * 				to receive requests.
	 */

	private Request_handler request_hdl = null;
	/*
	 * tmp_request_hdl: Specify the temporary request handler used to handle any
	 * 					incoming requests
	 */
	private Shutdown_thread mgmt_thread = null;
	/*
	 * mgmt_thread : specify the management thread responsible for
	 * 				 shutting down the server.
	 */
	private ConnRegistry registry = null;
	/*
	 * register: Specify the DB/register used by this server.
	 */
	
	public Server() {
		try {
			rcv_socket = new ServerSocket(ServerCommons.RCV_PORT);
			mgmt_thread = new Shutdown_thread(Thread.currentThread());
		} catch (IOException e) {
			System.out.println("Enable to create a serverSocket at port: " + ServerCommons.RCV_PORT);
			System.exit(-1);
		}	
		
		registry = new ConnRegistry();
	}
	
	/*
	 * is_shutting_down() : Specify whether the server is currently shurring down or not.
	 */
	public boolean is_shutting_down() {
		return mgmt_thread.is_shutting_down();
	}
	
	
	public void run() {
		Socket client_socket = null;
		/*
		 * client_socket : Specify the client socket returned by the accept() of
		 * 					the serverSocket.
		 */
		Thread thread_ptr = null;
		/*
		 * thread_ptr : Specify a thread pointer.
		 */
		
		thread_ptr = new Thread(mgmt_thread);
		thread_ptr.start();
		
		try {
			System.out.println("Server Running on port " + ServerCommons.RCV_PORT
					+"\nLocal ip: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			System.out.println("Error UnknownHostException");
		}

		while (true) {
			
			try {
				client_socket = rcv_socket.accept();
				request_hdl = new Request_handler(client_socket, this, registry);
				
				thread_ptr = new Thread(request_hdl);
				mgmt_thread.add_thread(thread_ptr);
				thread_ptr.start();
				
			} catch (IOException e) {
				if ( !mgmt_thread.is_shutting_down() )
					System.out.println("Server: An IO error has occurred. Exiting..");
				else
					System.out.print("System is shutting down...");
				break;
			}
			
			
		}
		
		try {
			rcv_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public class Shutdown_thread implements Runnable {
		private Thread main_thread;
		private ArrayList<Thread> thread_pool;
		private volatile boolean is_shutting_down = false;
		public final static String  USR_MSG = "Enter 'q' or 'quit' to shutdown the server.";

		public Shutdown_thread(Thread main_thread ) {
			this.main_thread = main_thread;
			thread_pool = new ArrayList<Thread>();
		}

		/*
		 * is_shutting_down() : Specify if the server is being shutdown 
		 */
		public boolean is_shutting_down () {
			return is_shutting_down;
		}

		/*
		 *
		 */
		public synchronized void add_thread (Thread t) {
			perform_op( true, t);
		}

		public synchronized void remove_thread (Thread t) {
			perform_op( false, t);
		}

		private synchronized void perform_op ( boolean is_add_op, Thread t) {
			if (is_add_op) {
				thread_pool.add(t);
			} else {
				if ( thread_pool.contains(t) )
					thread_pool.remove(t);
			}

		}

		@Override
		public void run() {
			Scanner in = null;
			String user_input = null;			
			in = new Scanner(System.in);

			while ( true ) {
				System.out.println(USR_MSG);
				user_input = in.nextLine();
				if ( user_input.startsWith("q") ) {
					ServerCommons.log("Server: shutting down...");
					is_shutting_down = true;
					in.close();
					break;
				}
			}

			try {
				rcv_socket.close();
				main_thread.join();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for ( Thread t : thread_pool) {
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			ServerCommons.log("Server: is now OFF.");
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
		System.out.println("Server have terminated");
	}

}
