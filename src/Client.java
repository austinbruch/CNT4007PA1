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

	private static String CRLF = "\r\n";

	private Socket socket;

	private InputStream inputStreamFromServer;
	private BufferedReader bufferedReaderFromServer;
	private DataOutputStream dataOutputStreamToServer;

	private BufferedReader bufferedReaderInputFromUser;

	private String serverAddress;
	private int serverPort;

	//	private ServerListenerThread serverListenerThread;
	//
	//	private UserListenerThread userListenerThread;


	public Client() {

	}

	public Client(String serverAddress, int serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}

	public Client(String serverAddress, String serverPort) {
		this.serverAddress = serverAddress;
		this.serverPort = Integer.parseInt(serverPort);
	}

	public void launch() throws UnknownHostException, IOException {
		this.socket = new Socket(this.serverAddress, this.serverPort);

		this.inputStreamFromServer = this.socket.getInputStream();
		this.bufferedReaderFromServer = new BufferedReader(new InputStreamReader(this.inputStreamFromServer));

		this.dataOutputStreamToServer = new DataOutputStream(this.socket.getOutputStream());

		this.bufferedReaderInputFromUser = new BufferedReader(new InputStreamReader(System.in));

		String inputFromServer = null;
		String inputFromUser = null;

		//		this.serverListenerThread = new ServerListenerThread(this);
		//		serverListenerThread.start();
		//
		//		this.userListenerThread = new UserListenerThread(this);
		//		userListenerThread.start();

		while(true) {
			inputFromServer = this.bufferedReaderFromServer.readLine();
			System.out.println(inputFromServer);
			if (inputFromServer.equals("-5")) {
				this.quit();
			}

			inputFromUser = this.bufferedReaderInputFromUser.readLine();

			try  {
			 	writeToServer(inputFromUser);
			} catch (IOException e) {
				this.quit();
			}

			//			while((inputFromServer = this.bufferedReaderFromServer.readLine()) != null) {
			//				System.out.println(inputFromServer);
			//				if (inputFromServer.equals("-5")) {
			//					this.quit();
			//				}
			//				break;
			//			}
			//			
			//			if (this.socket.isClosed()) {
			//				this.quit();
			//			}
			//			
			//			while((inputFromUser = this.bufferedReaderInputFromUser.readLine()) != null) {
			//				writeToServer(inputFromUser);
			//				break;
			//			}
			//			
			//			if (this.socket.isClosed()) {
			//				this.quit();
			//			}
		}
	}

	private void writeToServer(String inputFromUser) throws IOException {
		this.dataOutputStreamToServer.writeBytes(inputFromUser + CRLF);
	}

	private void breakDown() throws IOException {
		this.bufferedReaderInputFromUser.close();
		this.dataOutputStreamToServer.close();
		this.bufferedReaderFromServer.close();
		this.inputStreamFromServer.close();
		this.socket.close();
	}

	void quit() throws IOException {
		System.out.println("exit");
		breakDown();
		System.exit(0);
	}

	public static void main(String... args) throws Exception {
		Client client = new Client(args[0], args[1]);
		client.launch();
	}

	//	class ServerListenerThread extends Thread {
	//
	//		private Client client;
	//
	//		public ServerListenerThread(Client client) {
	//			this.client = client;
	//		}
	//
	//		@Override
	//		public void run() {
	//			try {
	//				String inputFromServer = null;
	//
	//				while(!this.isInterrupted() && (inputFromServer = this.client.bufferedReaderFromServer.readLine()) != null) {
	//					System.out.println(inputFromServer);
	//					if (inputFromServer.equals("-5")) {
	//						this.client.quit();
	//					}
	//				}
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//	}
	//
	//	class UserListenerThread extends Thread {
	//
	//		private Client client;
	//
	//		public UserListenerThread(Client client) {
	//			this.client = client;
	//		}
	//
	//		@Override
	//		public void run() {
	//			try {
	//				String inputFromUser = null;
	//
	//				while(!this.isInterrupted() && (inputFromUser = this.client.bufferedReaderInputFromUser.readLine()) != null) {
	//
	//					writeToServer(inputFromUser);
	//
	//				}
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}

}
