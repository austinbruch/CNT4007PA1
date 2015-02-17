/**
 * @author Austin Bruch
 * CNT4007C Spring 2015
 * Programming Assignment 1
 * 
 * Server class
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	private ArrayList<ClientConnection> clientConnections;

	private ServerSocket listenSocket;

	public void launch(String...args) throws IOException {
		clientConnections = new ArrayList<ClientConnection>();

		int port = Integer.parseInt(args[0]);

		// establish the listen socket
		listenSocket = new ServerSocket(port);

		System.out.println("Server started on port " + Integer.toString(port));
		while (true) {
			Socket clientSocket = listenSocket.accept();

			ClientConnection clientConnection = new ClientConnection(this, clientSocket);

			// Create a new thread to process the request.
			Thread thread = new Thread(clientConnection);

			clientConnections.add(clientConnection);

			// Start the thread.
			thread.start();

		}
	}

	public static void main(String... args) throws Exception {
		Server server = new Server();
		server.launch(args);
	}

	public void terminate() {
		try {
			this.terminateAllClientConnections();
			System.out.println("The server is now exiting gracefully.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeClientConnection(ClientConnection clientConnection) {
		clientConnections.remove(clientConnection);
	}

	private void terminateAllClientConnections() throws Exception {

		ArrayList<ClientConnection> toRemove = new ArrayList<ClientConnection>();
		for(ClientConnection cc : clientConnections) {
			cc.terminateConnection();
			toRemove.add(cc);
		}
		clientConnections.removeAll(toRemove);
	}
}