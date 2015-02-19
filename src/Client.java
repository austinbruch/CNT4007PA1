/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * Client class
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private static String CRLF = "\r\n"; // Used for convenience purposes for terminating data sent over the socket

	private Socket socket; // Reference to a Socket used to communicate with the Server

	private InputStream inputStreamFromServer; // InputStream reading from the Server
	private BufferedReader bufferedReaderFromServer; // BufferedReader used to read from the Server
	private DataOutputStream dataOutputStreamToServer; // DataOutputStream used to write data to the Server

	private BufferedReader bufferedReaderInputFromUser; // BufferedReader used to read from the User (Command-Line)

	private String serverAddress; // String containing the IP address (domain name) of the Server to connect to
	private int serverPort; // Port number that the Server is running on

	private FromUserThread fromUserThread; // Thread used to check for input from the user continuously (Command-Line)

	private String mostRecentCommand; // Caches the most recently issued command; used for proper messaging when the Server issues a -5 response

	/**
	 * Constructor
	 * @param serverAddress - String representing the host name (domain) of the server
	 * @param serverPort - Integer representing the port which the server is running on
	 */
	public Client(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.mostRecentCommand = ""; // Needs be initialized to an empty String
	}

	/**
	 * Constructor
	 * @param serverAddress - String representing the host name (domain) of the server
	 * @param serverPort - String representing the port on which the server is running
	 */
	public Client(String serverAddress, String serverPort) {
		this.serverAddress = serverAddress;
		try {
			this.serverPort = Integer.parseInt(serverPort);
		} catch (NumberFormatException nfe) {
			System.out.println("The supplied port number couldn't be parsed as an Integer."); // The port number couldn't be parsed as an integer
			System.exit(0);
		}
		this.mostRecentCommand = ""; // Needs be initialized to an empty String
	}

	/**
	 * Launches the Client, connecting to the Server and getting ready to accept User Input via Command Line
	 */
	public void launch() {
		try {
			this.socket = new Socket(this.serverAddress, this.serverPort);
		} catch (UnknownHostException uhe) {
			System.out.println("The IP address for " + this.serverAddress + " could not be determined."); // Server Address is bad
			System.exit(0);
		} catch (IOException ioe) {
			System.out.println("An I/O error has occurred while creating the socket.");
			System.exit(0);
		}

		try {
			this.inputStreamFromServer = this.socket.getInputStream();
			this.bufferedReaderFromServer = new BufferedReader(new InputStreamReader(this.inputStreamFromServer));
		} catch (IOException ioe) {
			System.out.println("An I/O error has occurred while opening the InputStream to the Socket connected to the Server.");
			System.exit(0);
		}

		try {
			this.dataOutputStreamToServer = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException ioe) {
			System.out.println("An I/O error has occurred while opening the OutputStream to the Socket connected to the Server.");
			System.exit(0);
		}

		this.bufferedReaderInputFromUser = new BufferedReader(new InputStreamReader(System.in));

		String inputFromServer = null; // String holding the input from the Server

		this.fromUserThread = new FromUserThread(this); // Create a thread that listens for input from the User via Command Line
		this.fromUserThread.start(); // Start that thread

		while(true) { // Infinitely listen for input from the Server
			try {
				inputFromServer = this.bufferedReaderFromServer.readLine(); // Block until the Server sends a line of data through the Socket to the Client 
				if(!inputFromServer.equals("Hello!")) {
					String errorMessage = interpretErrorCode(Integer.parseInt(inputFromServer)); // Get the correct error message
					System.out.println(errorMessage); // This will still display the correct result if the input from Server wasn't an error code
				} else {
					System.out.println(inputFromServer); // Directly print Hello! if that's the message, no need for error parsing
				}
				
				if(inputFromServer.equals("-5")) { // If the Server send -5, we need to terminate this client
					this.quit();
				}
			} catch (IOException ioe) {
				System.out.println("An I/O error occurred while attempting to read data from the Server.");
			}

		}
	}

	/**
	 * Sends data via Socket to the server
	 * @param inputFromUser String data to send to the Server
	 * @throws IOException If there is an IOException thrown when writing bytes to the DataOutputStream to the Server
	 */
	private void writeToServer(String inputFromUser) throws IOException {
		this.dataOutputStreamToServer.writeBytes(inputFromUser + CRLF); // Terminate the data being sent with Carriage Return and Line Feed to signify the end of transmission
	}

	/**
	 * Tear down this Client's Socket connection to the Server
	 */
	private void breakDown(){
		try {
			this.socket.close(); // Close the socket; also closes all respective streams to/from this Socket, and their Readers/Writers
		} catch (IOException e) {
			System.out.println("An I/O error occurred while attempting to close this Client's Socket connection to the Server.");
		}
	}

	/**
	 * Terminate this Client
	 */
	private void quit() {
		breakDown();
		System.exit(0);
	}

	/**
	 * Interprets the given error code and returns the corresponding error message to be displayed to the user
	 * @param errorCode integer error code to be interpreted
	 * @return the error message that corresponds to the given error code
	 */
	private String interpretErrorCode(int errorCode) {
		String errorMessage = "";

		switch(errorCode) {
		case -1:
			errorMessage = "incorrect operation command";
			break;
		case -2:
			errorMessage = "number of inputs is less than two";
			break;
		case -3:
			errorMessage = "number of inputs is more than four";
			break;
		case -4:
			errorMessage = "one or more of the inputs contain(s) non-number(s)";
			break;
		case -5:
			if(this.mostRecentCommand.equals("terminate")) {
				errorMessage = "exit"; // If the error code is -5 and this Client most recently issued a terminate command, return the regular exit message
			} else if(this.mostRecentCommand.equals("bye")) {
				errorMessage = "exit"; // If the error code is -5 and this Client most recently issued a bye command, return the regular exit message
			} else {
				errorMessage = "The Server has exited."; // The error code is -5 but this Client didn't most recently issue bye or terminate, so with respect to this Client, the Server is terminating on its own
			}
			break;
		default:
			errorMessage = Integer.toString(errorCode); // If the error code isn't actually an error code, simply return the code as it is a valid result to an operation
			break;
		}

		return errorMessage;

	}

	/**
	 * Called to start running a Client
	 * @param args array of Strings used to parameterize this instance of Client
	 */
	public static void main(String... args) {
		if(args.length == 2) {
			Client client = new Client(args[0], args[1]);
			client.launch();
		} else {
			System.out.println("Usage:\njava Client [serverAddress] [portNumber]");
		}
	}

	/**
	 * Inner class of Client
	 * Implements the Thread that listens to input from the User via Command Line
	 * @author Austin Bruch
	 *
	 */
	class FromUserThread extends Thread {

		private Client client; // Reference to the containing Client

		/**
		 * Constructor
		 * @param client Client that contains this Thread
		 */
		FromUserThread(Client client) {
			this.client = client;
		}

		@Override
		public void run() {
			String inputFromUser = null; // String containing the input from the User via Command Line
			while(true) { // Infinitely loop, waiting for User Input
				try {
					inputFromUser = this.client.bufferedReaderInputFromUser.readLine(); // Block until the User enters a line of data via Command Line
					try  {
						writeToServer(inputFromUser);
						this.client.mostRecentCommand = inputFromUser; // Update the most recently issues command within the containing Client
					} catch (IOException e) {
						System.out.println("An I/O error occurred while attempting to write data to the Server.");
					}
				} catch (IOException ioe) {
					System.out.println("An I/O error occurred while reading input from the User via Command Line");
				}
			}
		}
	}

}