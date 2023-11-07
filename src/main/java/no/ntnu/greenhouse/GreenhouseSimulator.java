package no.ntnu.greenhouse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.ntnu.listeners.greenhouse.NodeStateListener;
import no.ntnu.message.Message;
import no.ntnu.tools.Logger;

/**
 * Application entrypoint - a simulator for a greenhouse.
 */
public class GreenhouseSimulator {

  public static final int PORT_NUMBER = 1238;


  boolean isServerRunning;

  private final List<ClientHandler> connectedClients = new ArrayList<>();
  private ServerSocket serverSocket;

  private SensorActuatorNode logic;
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();

  /**
   * Initialise the greenhouse but don't start the simulation just yet.
   */
  public void initialize() {

    createNode(1, 2, 1, 0, 0);
    createNode(1, 0, 0, 2, 1);
    createNode(2, 0, 0, 0, 0);
    Logger.info("Greenhouse initialized");
  }

  private void createNode(int temperature, int humidity, int windows, int fans, int heaters) {
    SensorActuatorNode node = DeviceFactory.createNode(
        temperature, humidity, windows, fans, heaters);
    nodes.put(node.getId(), node);
  }

  /**
   * Start a simulation of a greenhouse - all the sensor and actuator nodes inside it.
   */
  public void start() {
    initiateCommunication();
    for (SensorActuatorNode node : nodes.values()) {
      node.start();
    }

    Logger.info("Simulator started");
  }

  private void initiateCommunication() {
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
   * Stop the simulation of the greenhouse - all the nodes in it.
   */
  public void stop() {
    stopCommunication();
    for (SensorActuatorNode node : nodes.values()) {
      node.stop();
    }
  }

  private void stopCommunication(){
    isServerRunning = false;
    try {
      serverSocket.close();
    } catch (IOException e) {
      System.err.println("An error occurred while stopping the server");
    }
    System.out.println("Server stopped.");
  }

  /**
   * Add a listener for notification of node staring and stopping.
   *
   * @param listener The listener which will receive notifications
   */
  public void subscribeToLifecycleUpdates(NodeStateListener listener) {
    for (SensorActuatorNode node : nodes.values()) {
      node.addStateListener(listener);
    }
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
  public void broadcastMessageToAllClients(Message message) {
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


  public SensorActuatorNode getSensorActuatorNode() {
    return logic;
  }
}
