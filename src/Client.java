/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * Client class
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private static String CRLF = "\r\n";

	private Socket socket;

	private InputStream inputStreamFromServer;
	private BufferedReader bufferedReaderFromServer;
	private DataOutputStream dataOutputStreamToServer;

	private BufferedReader bufferedReaderInputFromUser;

	private String serverAddress;
	private int serverPort;
	
	private Thread fromUserThread;

	/**
	 * Constructor
	 * @param serverAddress - String representing the host name (domain) of the server
	 * @param serverPort - integer representing the port which the server is running on
	 */
	public Client(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
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
	}

	public void launch() throws UnknownHostException, IOException {
		try {
			this.socket = new Socket(this.serverAddress, this.serverPort);
		} catch (UnknownHostException uhe) {
			System.out.println("The IP address for " + this.serverAddress + " could not be determined."); // Server Address is bad
			System.exit(0);
		} catch (IOException ioe) {
			System.out.println("An I/O error has occurred while creating the socket.");
			System.exit(0);
		}

		this.inputStreamFromServer = this.socket.getInputStream();
		this.bufferedReaderFromServer = new BufferedReader(new InputStreamReader(this.inputStreamFromServer));

		this.dataOutputStreamToServer = new DataOutputStream(this.socket.getOutputStream());

		this.bufferedReaderInputFromUser = new BufferedReader(new InputStreamReader(System.in));

		String inputFromServer = null;
		String inputFromUser = null;
		
		this.fromUserThread = new FromUserThread(this);
		this.fromUserThread.start();

		while(true) {
			inputFromServer = this.bufferedReaderFromServer.readLine();
			if (inputFromServer.equals("-5")) {
				System.out.println(inputFromServer);
				this.quit("exit");
			} else if(inputFromServer.equals("-6")) {
				inputFromServer = "-5";
				System.out.println(inputFromServer);
				this.quit("The Server has exited.");
			} else {
				System.out.println(inputFromServer);
			}

//			inputFromUser = this.bufferedReaderInputFromUser.readLine();
//
//			try  {
//				writeToServer(inputFromUser);
//			} catch (IOException e) {
//				this.quit("The Server has exited.");
//			}
		}
	}

	private void writeToServer(String inputFromUser) throws IOException {
		this.dataOutputStreamToServer.writeBytes(inputFromUser + CRLF);
	}

	private void breakDown(){
		
		try {
//			System.out.println("a");
//			this.bufferedReaderInputFromUser.close();
//			System.out.println("b");
//			this.dataOutputStreamToServer.close();
//			System.out.println("c");
//			this.bufferedReaderFromServer.close();
//			System.out.println("d");
//			this.inputStreamFromServer.close();
//			System.out.println("e");
			this.socket.close();
//			System.out.println("f");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	void quit(String message) throws IOException {
		System.out.println(message);
		breakDown();
//		System.out.println("breakDown() has been called.");
		System.exit(0);
	}

	public static void main(String... args) throws Exception {
		if(args.length == 2) {
			Client client = new Client(args[0], args[1]);
			client.launch();
		} else {
			System.out.println("Usage:\njava Client [serverAddress] [portNumber]");
		}
	}


	class FromUserThread extends Thread {

		private Client client;

		FromUserThread(Client client) {
			this.client = client;
		}

		@Override
		public void run() {
			String inputFromUser = null;
			try {
				while(true) {
					inputFromUser = this.client.bufferedReaderInputFromUser.readLine();
					try  {
						writeToServer(inputFromUser);
					} catch (IOException e) {
						this.client.quit("The Server has exited.");
					}
				}
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}
}
