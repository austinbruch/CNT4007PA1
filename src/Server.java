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

	// Used to store references to all ClientConnections that are spawned by the server
	private ArrayList<ClientConnection> clientConnections;

	// The ServerSocket that is used to initiate connections to clients
	private ServerSocket listenSocket;

	/**
	 * Initializes the Server on the specified port and starts listening for Client connections
	 * @param args - array of Strings, used to specify the port number
	 * @throws IOException
	 */
	public void launch(String...args) throws IOException {
		
		this.clientConnections = new ArrayList<ClientConnection>();
		
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException nfe) {
			System.out.println("Failed to parse the specified port number: " + args[0]);
			System.exit(0);
		}

		this.listenSocket = new ServerSocket(port); // Create the listening socket to accept client connections
		System.out.println("Server started on port " + Integer.toString(port));
		
		while (true) {
			Socket clientSocket = this.listenSocket.accept(); // Block until a new client connection arrives

			ClientConnection clientConnection = new ClientConnection(this, clientSocket); // Create a ClientConnection objects to handle the new Client

			Thread clientThread = new Thread(clientConnection); // We want to run this ClientConnection in a separate thread (one thread per client)

			this.clientConnections.add(clientConnection); // Add a reference to the ClientConnection to the list of ClientConnections for bookkeeping purposes

			clientThread.start(); // Start this thread
		}
	}
	
	/**
	 * Called to begin running the Server program
	 * @param args - parameters provided to the Server program
	 * @throws Exception
	 */
	public static void main(String... args) throws Exception {
		if(args.length != 1) {
			System.out.println("Usage:\njava Server [portNumber]");
		} else {
			Server server = new Server();
			server.launch(args);
		}
	}

	/**
	 * Called when this server is commanded to terminate execution
	 */
	public void terminate() {
		try {
			this.terminateAllClientConnections();
			System.out.println("The server is now exiting gracefully.");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to remove a ClientConnection reference from the List of references, when the Client is terminated
	 * @param clientConnection
	 */
	public void removeClientConnection(ClientConnection clientConnection) {
		this.clientConnections.remove(clientConnection);
	}

	/**
	 * Used to terminate all Client Connections when the server is to be terminated
	 * @throws Exception
	 */
	private void terminateAllClientConnections() throws Exception {

		ArrayList<ClientConnection> toRemove = new ArrayList<ClientConnection>(); // Maintain a list of all ClientConnections to remove
		for(ClientConnection cc : this.clientConnections) {
			cc.terminateConnection(); // For every ClientConnection that the server has, terminate it
			toRemove.add(cc); // Add each ClientConnection to the list to remove
		}
		this.clientConnections.removeAll(toRemove); // Remove all of those ClientConnections
	}
}