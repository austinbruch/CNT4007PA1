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
import java.util.HashMap;
import java.util.StringTokenizer;

public class ClientConnection implements Runnable {

	private static String CRLF = "\r\n";
	private boolean DEBUG = false;
	
	private Server server;
	private Socket socket;
	private InputStream inputStream;
	private BufferedReader bufferedReader;
	private DataOutputStream dataOutputStream;
	private ArrayList<String> operators;
	
	private boolean terminateFlag, byeFlag;

	public ClientConnection(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		this.operators = new ArrayList<String>();
		this.operators.add("add");
		this.operators.add("subtract");
		this.operators.add("multiply");
		this.terminateFlag = false;
		this.byeFlag = false;
	}
	
	@Override
	public void run() {
		debug("run enter");
		
		try {
			init();
			System.out.println("get connection from " + this.socket.getInetAddress().getHostAddress());

			sayHello();

			String inputFromClient = null;
			while ((inputFromClient = this.bufferedReader.readLine()) != null) {
				int result = 0;
				Object errorResult = checkForErrors(inputFromClient);
				if (errorResult instanceof Operation) {
					result = processRequest((Operation)errorResult); // if checkForErrors returns a Operation object, no errors were found
				} else {
					result = (Integer) errorResult; // checkForErrors returned an integer (error code)
				}

				System.out.println("get: " + inputFromClient + ", return: " + Integer.toString(result));

				writeToClient(Integer.toString(result));
				
				if (byeFlag || terminateFlag) {
					this.server.removeClientConnection(this);
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			breakDown();
			if(terminateFlag) {
				this.server.terminate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		debug("run exit");
	}

	private void init() throws Exception {
		debug("init enter");

		this.inputStream = this.socket.getInputStream();
		this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
		this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());

		debug("init exit");
	}

	private void breakDown() throws Exception {
		debug("breakDown enter");

		this.bufferedReader.close();
		this.inputStream.close();
		this.dataOutputStream.close();
		this.socket.close();

		debug("breakDown exit");
	}

	private void sayHello() throws Exception {
		debug("sayHello enter");

		writeToClient("Hello!");
		
		debug("sayHello exit");

	}


	private Object checkForErrors(String input) {
		debug("checkForErrors enter");
		
		int errorCode = 0;

		HashMap<Integer, Boolean> errorMap = new HashMap<Integer, Boolean>();

		errorMap.put(new Integer(-1), new Boolean(false));
		errorMap.put(new Integer(-2), new Boolean(false));
		errorMap.put(new Integer(-3), new Boolean(false));
		errorMap.put(new Integer(-4), new Boolean(false));
		errorMap.put(new Integer(-5), new Boolean(false));

		StringTokenizer tokenizer = new StringTokenizer(input, " ");

		int tokenCount = tokenizer.countTokens();

		if (tokenCount > 5) {
			errorMap.put(new Integer(-3), new Boolean(true));
		} else if (tokenCount < 3) {
			errorMap.put(new Integer(-2), new Boolean(true));
		}

		ArrayList<String> arguments = new ArrayList<String>();
		ArrayList<Integer> integers = new ArrayList<Integer>();

		int i;
		for (i = 0; i < tokenCount; i++) {
			arguments.add(tokenizer.nextToken());
		}

		String operator = null;

		operator = arguments.get(0);

		if (!operators.contains(operator)) {
			if(operator.equals("bye") && tokenCount == 1) {
				// client process is to exit, but server is to remain running
				errorMap.put(new Integer(-5), new Boolean(true));
				this.byeFlag = true;
			} else if (operator.equals("terminate")  && tokenCount == 1) {
				// both client (all clients) and server must shut down
				errorMap.put(new Integer(-5), new Boolean(true)); // TODO QUIT CLIENTS
				this.terminateFlag = true;
			} else {
				// the operator is not a valid operator
				errorMap.put(new Integer(-1), new Boolean(true));
			}
		} 

		int j;
		for (j = 0; j < 4; j++) {
			try {
				if (j >= tokenCount-1) {
					integers.add(null);
				} else {
					integers.add(Integer.parseInt(arguments.get(j+1)));
				}
			} catch (NumberFormatException nfe) {
				errorMap.put(new Integer(-4), new Boolean(true));
			}
		}

		if (errorMap.get(new Integer(-5))) {
			errorCode = -5;
		} else if (errorMap.get(new Integer(-1))) {
			errorCode = -1;
		} else if (errorMap.get(new Integer(-2))) {
			errorCode = -2;
		} else if (errorMap.get(new Integer(-3))) {
			errorCode = -3;
		} else if (errorMap.get(new Integer(-4))) {
			errorCode = -4;
		}
		
		if (errorCode == 0) {
			// no errors were found
//			Operation operation = new Operation(operator, integers.get(0), integers.get(1), integers.get(2), integers.get(3));
			Operation operation = new Operation(operator, integers);

			debug("checkForErrors exit");
			return operation;
		} else {
			// errors were found
			debug("checkForErrors exit");
			return new Integer(errorCode);
		}

	}

	private int processRequest(Operation operation) {
		debug("processRequest enter");

		int toReturn = 0;
		toReturn = operation.doOperation();

		debug("processRequest exit");

		return toReturn;
	}

	private void writeToClient(String data) throws IOException {
		this.dataOutputStream.writeBytes(data + CRLF);
	}
	
	public void terminateConnection() throws Exception {
		debug("terminateConnection enter");
		
		writeToClient("-5");
		
		debug("terminateConnection exit");
	}
	
	private void debug(String message) {
		if(DEBUG) {
			System.out.println("[DEBUG] " + message);
		}
	}

}
