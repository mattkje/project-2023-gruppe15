package no.gruppe15.controlpanel;

import static no.gruppe15.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.gruppe15.run.ControlPanelStarter.SERVER_HOST;
import static no.gruppe15.tools.Parser.parseIntegerOrError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.greenhouse.ActuatorCollection;
import no.gruppe15.greenhouse.Sensor;
import no.gruppe15.greenhouse.SensorReading;
import no.gruppe15.tools.Logger;

/**
 * This class represents a TCP - client. It should send and receive messages
 * between the server.
 *
 * @author Matti Kjellstadli, Håkon Svensen Karlsen, Di Xie, Adrian Faustino Johansen
 * @version 09.11.2023
 */
public class ControlPanelSocket implements CommunicationChannel {

  private final ControlPanelLogic logic;
  private Socket socket;
  private BufferedReader socketReader;
  private PrintWriter socketWriter;
  private boolean isConnected = false;

  /**
   * Creates an instance of ControlPanelSocket.
   *
   * @param logic The application logic class.
   */
  public ControlPanelSocket(ControlPanelLogic logic) {
    this.logic = logic;
  }

  /**
   * This method should send a command to a specific actuator
   *
   * @param nodeId     ID of the node to which the actuator is attached
   * @param actuatorId Node-wide unique ID of the actuator
   * @param isOn       When true, actuator must be turned on; off when false.
   */
  @Override
  public void sendActuatorChange(int actuatorId, int nodeId, boolean isOn) {
    Logger.info("Sending command to actuator " + nodeId + " on node " + actuatorId);
    String on = isOn ? "0" : "1";
    String command = actuatorId + ", " + nodeId + ", " + on;

    try {
      socketWriter.println(command);
      String response = socketReader.readLine();
      Logger.info(response);
    } catch (IOException e) {
      Logger.error("Error sending command to actuator " + actuatorId + " on node " + nodeId + ": " +
          e.getMessage());
    }
  }

  /**
   * Opens a TCP socket connection to the specified server.
   *
   * @return true if the socket connection was successfully established, false otherwise.
   */
  @Override
  public boolean open() {
    try {
      socket = new Socket(SERVER_HOST, PORT_NUMBER);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);
      getNodes();

      isConnected = true;
      return true;
    } catch (IOException e) {
      Logger.error("Could not connect to server: " + e.getMessage());
      return false;
    }
  }

  /**
   * This method should close the connection to the server.
   */
  public void close() {
    try {
      if (isConnected) {
        socket.close();
        socketWriter.close();
        socketReader.close();
        Logger.info(
            "Connection with client: " + SERVER_HOST + ":" + PORT_NUMBER + " has been closed");
      }
    } catch (IOException e) {
      Logger.error("Could not close connection: " + e.getMessage());
    }
  }

  /**
   * This method should get all nodes from server, and add them to
   * the controlPanel.
   */
  public void getNodes() {
    socketWriter.println("getNodes");
    Logger.info("Requesting nodes from server...");
    String nodes;
    String sensors;
    try {
      nodes = socketReader.readLine();
      sensors = socketReader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String[] nodeList = nodes.split("/");
    for (String node : nodeList) {
      logic.onNodeAdded(createSensorNodeInfoFrom(node));
      logic.onSensorData(createSensorNodeInfoFrom(node).getId(),parseSensor(sensors));
    }
    Logger.info("Nodes loaded");
  }

  /**
   * This method should create a sensor object from a string.
   *
   * @param input
   * @return
   */
  public List<SensorReading> parseSensor(String input){
    Pattern pattern = Pattern.compile("\\{ type=(\\w+), value=([\\d.]+), unit=(\\S+) }");
    String[] sensorGroups = input.split("/");
    List<SensorReading> sensorReadings = new ArrayList<>();

    for (String sensorGroup : sensorGroups) {
      Matcher matcher = pattern.matcher(sensorGroup);

      while (matcher.find()) {
        String type = matcher.group(1);
        double value = Double.parseDouble(matcher.group(2));
        String unit = matcher.group(3);

        sensorReadings.add(new SensorReading(type, value, unit));
      }
    }
    return sensorReadings;
  }

  /**
   * This method should create a collection of Actuators from a String.
   *
   * @param actuatorSpecification A collection of actuators as String.
   * @param info                  Current nodeId.
   * @return A collection of actuators.
   */
  private ActuatorCollection parseActuators(String actuatorSpecification, int info) {
    String[] parts = actuatorSpecification.split(" ");
    ActuatorCollection actuatorList = new ActuatorCollection();
    for (String part : parts) {
      actuatorList.add(parseActuatorInfo(part, info));
    }
    return actuatorList;
  }

  /**
   * This method should create an actuator object from a string and assigning it a nodeId.
   *
   * @param s    An actuator as String.
   * @param info Current nodeId.
   * @return An actuator object.
   */
  private Actuator parseActuatorInfo(String s, int info) {
    String[] actuatorInfo = s.split("_");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info format: " + s);
    }
    int actuatorId = parseIntegerOrError(actuatorInfo[0],
        "Invalid actuator count: " + actuatorInfo[0]);
    String actuatorType = actuatorInfo[1];
    Actuator actuator = new Actuator(actuatorId, actuatorType, info);
    actuator.setListener(logic);
    return actuator;
  }

  /**
   * This method should create a SensorActuatorNodeInfo object and populate it with actuators.
   *
   * @param specification Current server configuration as a String.
   * @return A populated SensorActuatorNodeInfo object
   */
  private SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
    if (specification.isEmpty()) {
      throw new IllegalArgumentException("Node specification can't be empty");
    }
    String[] parts = specification.split(";");
    if (parts.length > 2) {
      throw new IllegalArgumentException("Incorrect specification format");
    }
    int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
    SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
    if (parts.length == 2) {
      ActuatorCollection actuatorList = parseActuators(parts[1], info.getId());
      info.setActuatorList(actuatorList);
    }
    return info;
  }
}
