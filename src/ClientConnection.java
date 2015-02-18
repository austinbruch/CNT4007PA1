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

	private static String CRLF = "\r\n";

	private Server server;
	private Socket socket;
	private InputStream inputStream;
	private BufferedReader bufferedReader;
	private DataOutputStream dataOutputStream;

	public ClientConnection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	@Override
	public void run() {
		
		Operation operation = null;

		try {
			init();
			System.out.println("get connection from " + this.socket.getInetAddress().getHostAddress() + ":" + Integer.toString(this.socket.getPort()));

			sayHello();

			String inputFromClient = null;
			while ((inputFromClient = this.bufferedReader.readLine()) != null) {
				int result = 0;

				ErrorChecker errorChecker = new ErrorChecker();
				int errorCode = errorChecker.checkForErrors(inputFromClient);
				if(errorCode < 0) {
					result = errorCode;
				} else {
					operation = buildOperation(inputFromClient);
					result = processRequest(operation);
				}

				System.out.println("get: " + inputFromClient + ", return: " + Integer.toString(result));

				writeToClient(Integer.toString(result));

				if(operation != null) {
					if(operation.disconnectClient() || operation.terminateServer()) {
						this.server.removeClientConnection(this);
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			breakDown();
			if(operation != null) {
				if(operation.terminateServer()) {
					this.server.terminate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void init() throws Exception {

		this.inputStream = this.socket.getInputStream();
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
		this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());

	}

	private void breakDown() throws Exception {

//		this.bufferedReader.close();
//		this.inputStream.close();
//		this.dataOutputStream.close();
		this.socket.close();
	}

	private void sayHello() throws Exception {

		writeToClient("Hello!");

	}

	/**
	 * Builds the Operation object to represent the desired operation
	 * It is assumed that the input has been checked for errors
	 * @param input String input that will be parsed to build the Operation object
	 * @return
	 */
	private Operation buildOperation(String input) {

		Operation toReturn = new Operation();

		String operator = null;
		ArrayList<Integer> ints = new ArrayList<Integer>();

		StringTokenizer tokenizer = new StringTokenizer(input, " ");
		operator = tokenizer.nextToken();

		String tmp = null;
		while(tokenizer.hasMoreTokens()) {
			tmp = tokenizer.nextToken();
			ints.add(Integer.parseInt(tmp));
		}

		toReturn.setOperator(operator);
		toReturn.setIntegers(ints);

		return toReturn;
	}

	private int processRequest(Operation operation) {

		int toReturn = 0;
		toReturn = operation.doOperation();

		return toReturn;
	}

	private void writeToClient(String data) throws IOException {

		this.dataOutputStream.writeBytes(data + CRLF);

	}

	public void terminateConnection() throws Exception {

		writeToClient("-5"); // indicates terminated server instead of regular quit
		
	}
}