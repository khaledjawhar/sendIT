package comp_project;


public class ServerCommons {
	
	public static final int RCV_PORT = 5053;
	
	public static final byte[] INVALID_MSG = "Received invalid message. Closing the connection\0".getBytes();
	
	public static final int STATE_MSG_LEN = 2;
	
	
	
	public static void log(String str) {
		System.out.println(str);
	}
	
	public class InvalidMessageException extends Exception {
		private static final long serialVersionUID = 5335832820698534583L;
		public InvalidMessageException() { super();}		
	}
	

}
