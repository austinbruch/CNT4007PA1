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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientConnection implements Runnable {

	private static String CRLF = "\r\n";
	private static final Logger LOGGER = Logger.getLogger(ClientConnection.class.getName());

	private Server server;
	private Socket socket;
	private InputStream inputStream;
	private BufferedReader bufferedReader;
	private DataOutputStream dataOutputStream;

	public ClientConnection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		
		try {
			FileHandler handler = new FileHandler("../log/ClientConnection.log", false);
			handler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(handler);
			handler.setLevel(Level.ALL);
			LOGGER.setLevel(Level.ALL);
			LOGGER.setUseParentHandlers(false);
		} catch (IOException ioe) {
			System.out.println("Couldn't setup the File Handler for the ClientConnection Logger");
		}
		
	}

	@Override
	public void run() {
		String methodName = "run";
		LOGGER.entering(getClass().getName(), methodName);
		
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
			if(operation.terminateServer()) {
				this.server.terminate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LOGGER.exiting(getClass().getName(), methodName);
	}

	private void init() throws Exception {
		String methodName = "init";
		LOGGER.entering(getClass().getName(), methodName);

		this.inputStream = this.socket.getInputStream();
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
		this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());

		LOGGER.exiting(getClass().getName(), methodName);
	}

	private void breakDown() throws Exception {
		String methodName = "breakDown";
		LOGGER.entering(getClass().getName(), methodName);

		this.bufferedReader.close();
		this.inputStream.close();
		this.dataOutputStream.close();
		this.socket.close();

		LOGGER.exiting(getClass().getName(), methodName);
	}

	private void sayHello() throws Exception {
		String methodName = "sayHello";
		LOGGER.entering(getClass().getName(), methodName);

		writeToClient("Hello!");

		LOGGER.exiting(getClass().getName(), methodName);
	}

	/**
	 * Builds the Operation object to represent the desired operation
	 * It is assumed that the input has been checked for errors
	 * @param input String input that will be parsed to build the Operation object
	 * @return
	 */
	private Operation buildOperation(String input) {
		String methodName = "buildOperation";
		LOGGER.entering(getClass().getName(), methodName);

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

		LOGGER.exiting(getClass().getName(), methodName);
		return toReturn;
	}

	private int processRequest(Operation operation) {
		String methodName = "processRequest";
		LOGGER.entering(getClass().getName(), methodName);

		int toReturn = 0;
		toReturn = operation.doOperation();

		LOGGER.exiting(getClass().getName(), methodName);
		return toReturn;
	}

	private void writeToClient(String data) throws IOException {
		String methodName = "writeToClient";
		LOGGER.entering(getClass().getName(), methodName);

		this.dataOutputStream.writeBytes(data + CRLF);

		LOGGER.exiting(getClass().getName(), methodName);
	}

	public void terminateConnection() throws Exception {
		String methodName = "terminateConnection";
		LOGGER.entering(getClass().getName(), methodName);

		writeToClient("-5");

		LOGGER.exiting(getClass().getName(), methodName);
	}
}