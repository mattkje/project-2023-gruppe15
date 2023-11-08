package no.gruppe15.greenhouse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import no.gruppe15.listeners.greenhouse.NodeStateListener;
import no.gruppe15.message.Message;
import no.gruppe15.tools.Logger;

/**
 * Application entrypoint - a simulator for a greenhouse.
 */
public class GreenhouseSimulator {
  public static final int PORT_NUMBER = 1238;
  private final Map<Integer, SensorActuatorNode> nodes = new HashMap<>();

  private final List<PeriodicSwitch> periodicSwitches = new LinkedList<>();
  private final List<ClientHandler> connectedClients = new ArrayList<>();
  private final boolean fake;
  private ServerSocket serverSocket;
  private boolean isServerRunning;

  /**
   * Create a greenhouse simulator.
   *
   * @param fake When true, simulate a fake periodic events instead of creating
   *             socket communication
   */
  public GreenhouseSimulator(boolean fake) {
    this.fake = fake;
  }

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
    for (PeriodicSwitch periodicSwitch : periodicSwitches) {
      periodicSwitch.start();
    }

    Logger.info("Simulator started");
  }

  private void initiateCommunication() {
    if (fake) {
      initiateFakePeriodicSwitches();
    } else {
      Thread serverThread = new Thread(() ->{
        initiateRealCommunication();
      });
      serverThread.start();
    }
  }

  /**
   * Sets up the TCP communication
   *
   */
  private void initiateRealCommunication() {
    try {
      serverSocket = new ServerSocket(PORT_NUMBER);
      Logger.info("Server is now listening on port " + PORT_NUMBER);
    } catch (IOException e) {
      Logger.error("Could not set up TCP connection: " + e.getMessage());
      return;
    }
    isServerRunning = true;
    while (isServerRunning && !serverSocket.isClosed()) {
      ClientHandler clientHandler = acceptNextClientConnection(serverSocket);

      if (clientHandler != null) {
        connectedClients.add(clientHandler);
        clientHandler.start();
      }
    }
  }

  private ClientHandler acceptNextClientConnection(ServerSocket listeningSocket) {
    try {
      Socket clientSocket = listeningSocket.accept();
      Logger.info("New client connected from " + clientSocket.getRemoteSocketAddress());
      importNodesFromServer();
      return new ClientHandler(clientSocket, this);
    } catch (IOException e) {
      Logger.error("Could not accept client connection: " + e.getMessage());
      return null;
    }
  }

  public void importNodesFromServer(){
    //TODO implement this.
  }

  /**
   * This method is used for debugging
   * //TODO: Should be deleted when done
   */
  private void initiateFakePeriodicSwitches() {
    periodicSwitches.add(new PeriodicSwitch("Window DJ", nodes.get(1), 2, 20000));
    periodicSwitches.add(new PeriodicSwitch("Heater DJ", nodes.get(2), 7, 8000));
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

  /**
   * This method should stop the TCP communication
   * <p>
   * TODO: Remove "fake" when done
   */
  private void stopCommunication() {
    if (fake) {
      for (PeriodicSwitch periodicSwitch : periodicSwitches) {
        periodicSwitch.stop();
      }
    } else {
      try {
        serverSocket.close();
        Logger.info("TCP connection successfully closed");
      } catch (IOException e) {
        Logger.error("An error occurred while stopping communication");
      }
    }
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

  public Actuator getActuator() {
    //Implement this
    return new Actuator("window", 1);
  }
}
