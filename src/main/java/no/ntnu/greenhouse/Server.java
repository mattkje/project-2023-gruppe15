package no.ntnu.greenhouse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import no.ntnu.tools.Logger;

/**
 * This class is responsible for managing the TCP server socket(s) for the Greenhouse application.
 * TODO: Implement this class with clientHandler.
 *
 * @author Matti Kjellstadli, Adrian Johansen, Håkon Karlsen, Di Xie
 * @version 06.11.2023
 */
public class Server {

  public static final int PORT_NUMBER = 1238;

  boolean isServerRunning;

  private final List<ClientHandler> connectedClients = new ArrayList<>();
  private ServerSocket serverSocket;


  /**
   * Start TCP server.
   */
  public void startServer() {
    serverSocket = openListeningSocket();

    if (serverSocket == null) {
      System.err.println("Failed to open the listening socket. Server cannot start.");
      return;
    }

    System.out.println("Server is now listening on port " + PORT_NUMBER);

    isServerRunning = true;

    while (isServerRunning) {
      ClientHandler clientHandler = acceptNextClientConnection(serverSocket);

      if (clientHandler != null) {
        connectedClients.add(clientHandler);
        clientHandler.start();
      }
    }
  }


  /**
   * Stops the TCP server and releases associated resources.
   */
  public void stopServer() {
    isServerRunning = false;
    try {
      serverSocket.close();
    } catch (IOException e) {
      System.err.println("An error occurred while stopping the server");
    }
    System.out.println("Server stopped.");
  }


  /**
   * Opens a server socket to listen for incoming client connections on the specified port.
   *
   * @return The opened ServerSocket if successful, or null on failure.
   */
  private ServerSocket openListeningSocket() {
    try {
      return new ServerSocket(PORT_NUMBER);
    } catch (IOException e) {
      System.err.println(
          "Failed to open the server socket on port " + PORT_NUMBER + ": " + e.getMessage());
      return null;
    }
  }

  /**
   * Send a message to all currently connected clients.
   *
   * @param message The message to send
   */
  public void broadcastMessageToAllClients(Logger message) {
    connectedClients.forEach(clientHandler -> clientHandler.sendResponseToClient(message));
  }

  /**
   * This method should remove any disconnected clients.
   *
   * @param clientHandler The current client handler.
   */
  public void removeDisconnectedClient(ClientHandler clientHandler) {
    connectedClients.remove(clientHandler);
  }


  /**
   * Accepts the next client connection from the given ServerSocket and
   * creates a ClientHandler for it.
   *
   * @param listeningSocket The ServerSocket to accept the connection from.
   * @return The ClientHandler for the new client if successful, or null on failure.
   */
  private ClientHandler acceptNextClientConnection(ServerSocket listeningSocket) {
    try {
      Socket clientSocket = listeningSocket.accept();
      System.out.println("New client connected from " + clientSocket.getRemoteSocketAddress());
      return new ClientHandler(clientSocket, this);
    } catch (IOException e) {
      System.err.println("Could not accept client connection: " + e.getMessage());
      return null;
    }
  }


}

