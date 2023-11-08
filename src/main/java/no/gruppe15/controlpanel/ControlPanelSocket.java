package no.gruppe15.controlpanel;

import static no.gruppe15.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.gruppe15.run.ControlPanelStarter.SERVER_HOST;
import static no.gruppe15.tools.Parser.parseIntegerOrError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import no.gruppe15.command.Command;
import no.gruppe15.command.ToggleActuatorCommand;
import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.message.MessageSerializer;
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


  @Override
  public void sendActuatorChange(int nodeId, int actuatorId, boolean isOn) {
    sendCommand(new ToggleActuatorCommand());
    /*
    Logger.info("Successfully selected actuator with id: " + actuatorId
        + " on node " + nodeId);
     */
  }

  /**
   * This method opens a new socket.
   *
   * @return
   */
  @Override
  public boolean open() {
    try {
      socket = new Socket(SERVER_HOST, PORT_NUMBER);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);
      //Debug nodes
      spawnNode("1;1_window");
      spawnNode("2;2_window");
      spawnNode("3;1_heater");
      spawnNode("4;1_window");
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

  public void sendCommand(Command command) {
    if (socketWriter != null && socketReader != null) {
      try {
        socketWriter.println(MessageSerializer.toString(command));
        System.out.println("Sending command: " + MessageSerializer.toString(command));
        String serverResponse;
        try {
          serverResponse = socketReader.readLine();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Server response: " + serverResponse);
      } catch (Exception e) {
        System.err.println("Could not send a command: " + e.getMessage());
      }
    }
  }

  public void spawnNode(String spawn) {
    logic.onNodeAdded(createSensorNodeInfoFrom(spawn));
  }

  private void parseActuators(String actuatorSpecification, SensorActuatorNodeInfo info) {
    String[] parts = actuatorSpecification.split(" ");
    for (String part : parts) {
      parseActuatorInfo(part, info);
    }
  }

  private void parseActuatorInfo(String s, SensorActuatorNodeInfo info) {
    String[] actuatorInfo = s.split("_");
    if (actuatorInfo.length != 2) {
      throw new IllegalArgumentException("Invalid actuator info format: " + s);
    }
    int actuatorCount = parseIntegerOrError(actuatorInfo[0],
        "Invalid actuator count: " + actuatorInfo[0]);
    String actuatorType = actuatorInfo[1];
    for (int i = 0; i < actuatorCount; ++i) {
      Actuator actuator = new Actuator(actuatorType, info.getId());
      actuator.setListener(logic);
      info.addActuator(actuator);
    }
  }

  private SensorActuatorNodeInfo createSensorNodeInfoFrom(String specification) {
    if (specification == null || specification.isEmpty()) {
      throw new IllegalArgumentException("Node specification can't be empty");
    }
    String[] parts = specification.split(";");
    if (parts.length > 3) {
      throw new IllegalArgumentException("Incorrect specification format");
    }
    int nodeId = parseIntegerOrError(parts[0], "Invalid node ID:" + parts[0]);
    SensorActuatorNodeInfo info = new SensorActuatorNodeInfo(nodeId);
    if (parts.length == 2) {
      parseActuators(parts[1], info);
    }
    return info;
  }

}
