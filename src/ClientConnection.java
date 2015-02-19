/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * ClientConnection class
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ClientConnection implements Runnable {

	private static String CRLF = "\r\n"; // Used for convenience purposes for terminating data sent over the socket

	private Server server; // Reference to the Server which created this ClientConnection
	private Socket socket; // Reference to the Socket used to communicate to the Client with
	private InputStream inputStream; // Socket's InputStream 
	private BufferedReader bufferedReader; // BufferedReader for the Socket's InputStream
	private DataOutputStream dataOutputStream; // DataOutputStream for the Socket

	/**
	 * Constructor 
	 * Creates a ClientConnection object given a reference to the Server and Socket that are to be used
	 * @param server - Server reference
	 * @param socket - Socket reference
	 */
	public ClientConnection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		
		Operation operation = null; // Operation object used to perform the calculations

		try {
			init(); // Initialize the ClientConnection
			
			// Prints out the IP address and port number of the incoming Client connection
			System.out.println("get connection from " + this.socket.getInetAddress().getHostAddress() + ":" + Integer.toString(this.socket.getPort()));  

			sayHello(); // Initially say hello to the Client

			String inputFromClient = null; // String variable used to hold input from the Client
			while ((inputFromClient = this.bufferedReader.readLine()) != null) { // Continuously read one line at a time from the Client
				int result = 0;

				ErrorChecker errorChecker = new ErrorChecker(); // Create an ErrorChecker instance
				int errorCode = errorChecker.checkForErrors(inputFromClient); // Have the ErrorChecker check for errors in the Client's input
				if(errorCode < 0) { // If the error code is negative, that implies the ErrorChecker found an error
					result = errorCode; // Set the result to the error code found by the ErrorChecker
				} else {
					operation = buildOperation(inputFromClient); // The ErrorChecker didn't find any errors, so let's build the Operation object
					result = processRequest(operation); // Set the result equal to the evaluation of the Operation object
				}

				// Print out what the Server received as input and what it will return to the Client
				System.out.println("get: " + inputFromClient + ", return: " + Integer.toString(result));

				writeToClient(Integer.toString(result)); // Write the result to the Client

				if(operation != null) { // As long as we have had an Operation thus far 
					if(operation.disconnectClient() || operation.terminateServer()) { // If the Operation told us to disconnect the Client or terminate the Server
						this.server.removeClientConnection(this); // Remove this ClientConection from the Server's list of ClientConnections
						break; // Break out of the listening while loop
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			breakDown(); // Once we have broken out of the while loop, we must need to break down the ClientConnection
			if(operation != null) { // As long as we have had an Operation 
				if(operation.terminateServer()) { // If the Operation is telling us to terminate the Server
					this.server.terminate(); // Pass that message to the Server itself
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initializes this ClientConnection object to be ready to read and write from and to its Socket
	 * @throws Exception
	 */
	private void init() throws Exception {

		this.inputStream = this.socket.getInputStream(); // Obtain reference to the Socket's InputStream 
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream)); // Create the BufferedReader for the Socket's InputStream
		this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream()); // Create the DataOutputStream for the Socket

	}

	/**
	 * Gracefully destroys the Socket for this ClientConnection
	 * @throws Exception
	 */
	private void breakDown() throws Exception {

		this.socket.close(); // Close the Socket, which closes all streams in and out, as well as their respective Readers and Writers
	}

	/**
	 * Sends the initial "Hello!" message to the Client
	 * @throws Exception
	 */
	private void sayHello() throws Exception {

		writeToClient("Hello!");

	}

	/**
	 * Builds the Operation object to represent the desired operation
	 * It is assumed that the input has been checked for errors
	 * @param input String input that will be parsed to build the Operation object
	 * @return toReturn - Operation object representing the specified Operation to be performed
	 */
	private Operation buildOperation(String input) {

		Operation toReturn = new Operation(); // Create a new Operation

		String operator = null; // String variable used to hold the operator for the Operation
		ArrayList<Integer> ints = new ArrayList<Integer>(); // ArrayList of Integers used to hold the Integer arguments to be used for the Operation

		StringTokenizer tokenizer = new StringTokenizer(input, " "); // Tokenizer used to grab all tokens from the input string
		operator = tokenizer.nextToken(); // Determine the operator for the Operation

		String tmp = null;
		while(tokenizer.hasMoreTokens()) { // While there are more arguments to read
			tmp = tokenizer.nextToken(); // Grab the next argument
			ints.add(Integer.parseInt(tmp)); // Create an Integer from the argument and add it to the list of Integers
		}

		toReturn.setOperator(operator); // Set the operator for the Operation
		toReturn.setIntegers(ints); // Set the ArrayList of Integers for the Operation

		return toReturn; // Return the Operation
	}

	/**
	 * Has the given Operation perform its operation and returns the result
	 * @param operation - Operation object that is to be executed
	 * @return toReturn - the Integer return value from the Operation object
	 */
	private int processRequest(Operation operation) {

		int toReturn = 0;
		toReturn = operation.doOperation(); // Have the Operation invoke itself and return the result

		return toReturn;
	}

	/**
	 * Writes data to the Socket, effectively transmitting data to the Client 
	 * @param data - String that is to be transmitted to the client
	 * @throws IOException
	 */
	private void writeToClient(String data) throws IOException {

		this.dataOutputStream.writeBytes(data + CRLF); // Terminate the data being sent with Carriage Return and Line Feed to signify the end of transmission

	}

	/**
	 * Logically terminates the connection with the Client by sending the -5 error code to it 
	 * @throws Exception
	 */
	public void terminateConnection() throws IOException {

		writeToClient("-5");
		
	}
	
}