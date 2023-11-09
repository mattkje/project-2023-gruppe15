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
import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.greenhouse.ActuatorCollection;
import no.gruppe15.tools.Logger;

/**
 * A fake communication channel. Emulates the node discovery (over the Internet).
 * In practice - spawn some events at specified time (specified delay).
 * Note: this class is used only for debugging, you can remove it in your final project!
 */
public class ControlPanelSocket implements CommunicationChannel {

  private final ControlPanelLogic logic;
  private Socket socket;
  private BufferedReader socketReader;
  private PrintWriter socketWriter;

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
      throw new RuntimeException(e);
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
      //Debug nodes
      getNodes();


      return true;
    } catch (IOException e) {
      Logger.error("Could not connect to server: " + e.getMessage());
      return false;
    }
  }

  public boolean close() {
    try {
      socket.close();
      socketWriter.close();
      socketReader.close();
      Logger.info("Connection with client: " + SERVER_HOST + ":" + PORT_NUMBER
          + " has been closed");
      return true;
    } catch (IOException e) {
      Logger.error("Could not close connection: " + e.getMessage());
      return false;
    }
  }

  /**
   * This method should spawn nodes in the controlPanel.
   * TODO: Remove this as it is just for debugging.
   *
   * @param spawn A node given as string
   */
  public void spawnNode(String spawn) {
    logic.onNodeAdded(createSensorNodeInfoFrom(spawn));
  }


  /**
   * This method should get all nodes from server, and add them to
   * the controlPanel.
   *
   * TODO: implement this
   */
  public void getNodes() {
    socketWriter.println("getNodes");
    String nodes;
    try {
      nodes = socketReader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String[] nodeList = nodes.split("/");
    for (String node : nodeList) {
      logic.onNodeAdded(createSensorNodeInfoFrom(node));
    }

  }

  private ActuatorCollection parseActuators(String actuatorSpecification, int info) {
    String[] parts = actuatorSpecification.split(" ");
    ActuatorCollection actuatorList = new ActuatorCollection();
    for (String part : parts) {
      actuatorList.add(parseActuatorInfo(part, info));
    }


    return actuatorList;
  }

  private Actuator parseActuatorInfo(String s, int info) {
    String[] actuatorInfo = s.split("_");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info format: " + s);
    }
    int actuatorId = parseIntegerOrError(actuatorInfo[0],
        "Invalid actuator count: " + actuatorInfo[0]);
    String actuatorType = actuatorInfo[1];
    Actuator actuator = new Actuator(actuatorId, actuatorType, info);
    //System.out.println(actuator.getId()+"-----------------"+actuator.getType()+"-----------------"+actuator.getNodeId());
    actuator.setListener(logic);
    return actuator;
  }

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
