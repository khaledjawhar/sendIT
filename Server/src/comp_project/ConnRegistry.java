package comp_project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ConnRegistry {
	private final static String registred_users_file = "C:/Users/AminQ/Desktop/shareIt.txt";
	
	private Map<String, UserEntry> connected_users = null;
	private Map<String, String> registred_users = null;
	private BufferedWriter user_writer = null;
		
	public ConnRegistry() {
		connected_users = new HashMap<String, UserEntry>();
		registred_users = new HashMap<String, String>();;
		initBufferedDB();
		
	}
	
	public boolean register_user (String username, String password) {
		if (username.contains(";")) {
			return false;
		}
		
		synchronized (registred_users) {
			if (registred_users.get(username) != null) {
				return false;
			}
			registred_users.put(username, password);
			System.out.println("Password is " + registred_users.get(username));
		}
		
		synchronized (user_writer) {
			try {
				user_writer.append(username+";"+password+"\n");
				user_writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public byte[] get_user_ip (String username) {
		UserEntry entry = null;
		
		synchronized  (connected_users) {
			entry = connected_users.get(username);
		}
		
		if (entry == null)
			return null;
		
		synchronized (entry) {
			return entry.get_socket_address(true);
		}		
	}
	
	public void add_connected(String username, Socket socket, String local_ip) {
		UserEntry entry = new UserEntry(username, socket, local_ip);
		
		synchronized  (connected_users) {
			connected_users.put(username, entry);
		}
	}
	
	public void remove_disconnected_user (String user) {
		synchronized  (connected_users) {
				connected_users.remove(user);
		}
	}
	
	public  boolean authentificate (String username, String password) {
		synchronized  (registred_users) {
			if (password == null || username == null || 
					 registred_users.get(username) == null ||
					!registred_users.get(username).equals(password) ) {
				System.out.println("Auth failed! Password in db " + registred_users.get(username)
							+ ". password rcv : " + password);
				return false;
			}
		}
		return true;
	}
	
	public void initBufferedDB () {
		File userDB = null;
		BufferedReader reader = null;
		String entry = null;
		int delimiter_idx = 0;
		
		userDB = new File(registred_users_file);
		

		try {
			reader = new BufferedReader(new FileReader(userDB));
			user_writer = new BufferedWriter(new FileWriter(userDB, true));
			while ((entry = reader.readLine()) != null) {
				delimiter_idx = entry.indexOf(';');
				registred_users.put(entry.substring(0, delimiter_idx),
					entry.substring(delimiter_idx + 1, entry.length()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public class UserEntry {
		private String _username = null;
		private Socket socket_handler = null;
		private String _local_ip = null;
		
		public UserEntry (String username, Socket socket, String local_ip) {
			socket_handler = socket;
			_username = username;
			_local_ip = local_ip;
		}
		
		public String get_username() {
			return _username;
		}
		
		public String get_local_ip() {
			return _local_ip;
		}
		
		public boolean is_connected () {
			
			return false;
		}
		
		public byte[] get_socket_address(boolean isLocal) {
			if (socket_handler == null || !
					socket_handler.isConnected() ||
					socket_handler.isClosed()) {
				return null;
			}
			if (isLocal) {
				return _local_ip.getBytes();
			}
			
			return socket_handler.getRemoteSocketAddress().toString().getBytes();
			
		}
		
		
		
	}
	
}
