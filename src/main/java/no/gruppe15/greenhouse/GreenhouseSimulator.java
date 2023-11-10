package no.gruppe15.greenhouse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import no.gruppe15.listeners.greenhouse.NodeStateListener;
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
   * TODO: Add more nodes, add function to add nodes inside the application??
   */
  public void initialize() {
    createNode(1, 2, 1, 0, 0);
    createNode(1, 0, 0, 2, 1);
    createNode(2, 0, 0, 0, 0);
    createNode(2, 0, 0, 1, 0);
    createNode(2, 0, 0, 0, 1);
    createNode(2, 0, 0, 1, 1);

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
      Thread serverThread = new Thread(this::initiateRealCommunication);
      serverThread.start();
    }
  }

  /**
   * Sets up the TCP communication
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
      return new ClientHandler(clientSocket, this);
    } catch (IOException e) {
      Logger.error("Could not accept client connection: " + e.getMessage());
      return null;
    }
  }

  public void handleActuator(int actuatorId, int nodeId, boolean isOn){
    if (!isOn){
      nodes.get(nodeId).getActuators().get(actuatorId).turnOn();
    } else {
      nodes.get(nodeId).getActuators().get(actuatorId).turnOff();
    }
  }

  public String getNodes() {
    Map<Integer, List<Actuator>> actuatorsByNode = new HashMap<>();

    for (SensorActuatorNode node : nodes.values()) {
      for (Actuator actuator : node.getActuators()) {
        actuatorsByNode.computeIfAbsent(actuator.getNodeId(), k -> new ArrayList<>()).add(actuator);
      }
    }

    List<String> commands = new ArrayList<>();
    for (Map.Entry<Integer, List<Actuator>> entry : actuatorsByNode.entrySet()) {
      int nId = entry.getKey();
      List<Actuator> actuators = entry.getValue();

      String actuatorString = actuators.stream()
          .map(a -> a.getId() + "_" + a.getType())
          .collect(Collectors.joining(" "));

      String commandString = nId + ";" + actuatorString;
      commands.add(commandString);
    }

    return String.join("/", commands);
  }

  /**
   * Updates all sensors and generates commands for each sensor node.
   * Work in progress.
   *
   * @return A string containing commands for sensor nodes.
   */
  public String updateSensors() {
    Map<Integer, List<Sensor>> sensorsByNode = new HashMap<>();

    for (SensorActuatorNode node : nodes.values()) {
      for (Sensor sensor : node.getSensors()) {
        sensorsByNode.computeIfAbsent(node.getId(), k -> new ArrayList<>()).add(sensor);
      }
    }

    List<String> commands = new ArrayList<>();

    for (Map.Entry<Integer, List<Sensor>> entry : sensorsByNode.entrySet()) {
      int nodeId = entry.getKey();
      List<Sensor> sensors = entry.getValue();

      String actuatorString = sensors.stream()
          .map(sensor -> String.valueOf(sensor.getReading()))
          .collect(Collectors.joining(" "));

      String commandString = nodeId + ";" + actuatorString;
      commands.add(commandString);
    }

    System.out.println(formatSensorCommand(String.join("/", commands)));
    return formatSensorCommand(String.join("/", commands));
  }

  public String formatSensorCommand(String command){
    return command.replace("{", "").replace("}", "")
        .replace(",", "").replace("   ", ",")
        .replace("type=","").replace(" value", "")
        .replace("unit=", "").replace("; ", ";");
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
   * This method should remove any disconnected clients.
   *
   * @param clientHandler The current client handler.
   */
  public void removeDisconnectedClient(ClientHandler clientHandler) {
    connectedClients.remove(clientHandler);
  }
}
