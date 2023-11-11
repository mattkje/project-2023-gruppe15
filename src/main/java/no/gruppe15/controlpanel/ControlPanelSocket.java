package no.gruppe15.controlpanel;

import static no.gruppe15.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.gruppe15.run.ControlPanelStarter.SERVER_HOST;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javafx.scene.Scene;
import no.gruppe15.gui.controlpanel.CommandLineControlPanel;
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

    updateSensorData();
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

  public void openCommandLine(){
    open();
    Scanner input = new Scanner(System.in);
    Thread commandThread = new Thread(() -> startCommandControl(input));
    commandThread.start();
  }

  private void startCommandControl(Scanner input) {
    CommandLineControlPanel controlPanel = new CommandLineControlPanel(this);
    controlPanel.startCommandControl(input);
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
      logic.onNodeAdded(logic.createSensorNodeInfoFrom(node));

    }
    logic.sensorStringSplitter(sensors);
    Logger.info("Nodes loaded");
  }

  /**
   * This method should update the sensors continually.
   */
  public void updateSensorData(){
    socketWriter.println("updateSensor");
    String sensors;
    try {
      sensors = socketReader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    logic.sensorStringSplitter(sensors);
  }

}
