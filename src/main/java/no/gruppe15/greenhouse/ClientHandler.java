package no.gruppe15.greenhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import no.gruppe15.tools.Logger;

/**
 * This class represents a client handler.
 *
 * @author Matti Kjellstadli, Håkon Svensen Karlsen, Di Xie, Adrian Faustino Johansen
 * @version 09.11.2023
 */
public class ClientHandler extends Thread {
  private final Socket socket;

  private final GreenhouseSimulator simulator;

  private final BufferedReader socketReader;

  private final PrintWriter socketWriter;

  /**
   * Creates an instance of ClientHandler.
   *
   * @param socket    Socket associated with this client
   * @param simulator Reference to the main TCP server class
   * @throws IOException When something goes wrong with establishing the input or output streams
   */
  public ClientHandler(Socket socket, GreenhouseSimulator simulator) throws IOException {
    this.simulator = simulator;
    this.socket = socket;
    socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    socketWriter = new PrintWriter(socket.getOutputStream(), true);
  }

  /**
   * This method is responsible for handling client requests and executing commands.
   */
  @Override
  public void run() {
    String rawCommand;

    try {
      while ((rawCommand = socketReader.readLine()) != null) {
        processCommand(rawCommand);
      }
    } catch (IOException e) {
      Logger.error("An error occurred while reading from the socket: " + e.getMessage());
    }

    String clientAddress = socket.getRemoteSocketAddress().toString();
    Logger.info("Client at " + clientAddress + " has disconnected.");
    simulator.removeDisconnectedClient(this);
  }

  /**
   * Processes a raw command received from the client, providing feedback accordingly.
   *
   * @param rawCommand The command as a string
   */
  private void processCommand(String rawCommand) {
    handleRawCommand(rawCommand);
    Logger.info("Received a command from the client: " + rawCommand);
  }

  /**
   * Handles the processing of a raw command, taking appropriate actions based on the command's content.
   *
   * @param rawCommand The command as a string
   */
  private void handleRawCommand(String rawCommand) {
    if (rawCommand.equals("getNodes")) {
      handleGetNodesCommand();
      handleUpdateSensorCommand();
    } else {
      processActuatorCommand(rawCommand);
    }
  }

  /**
   * Handles the "getNodes" command, retrieving and sending nodes information to the client.
   */
  private void handleGetNodesCommand() {
    socketWriter.println(simulator.getNodes());
  }

  /**
   * Handles the "getNodes" command, retrieving and sending nodes information to the client.
   */
  private void handleUpdateSensorCommand() {
    socketWriter.println(simulator.updateSensors());
  }

  /**
   * Processes an actuator command, extracting relevant information, and updating the simulator state.
   *
   * @param rawCommand The actuator command as a string
   */
  private void processActuatorCommand(String rawCommand) {
    String[] parts = rawCommand.split(",");
    if (parts.length == 3) {
      int nodeId = Integer.parseInt(parts[0].trim());
      int actuatorId = Integer.parseInt(parts[1].trim());
      int on = Integer.parseInt(parts[2].trim());
      boolean isOn = (on != 0);

      simulator.handleActuator(actuatorId, nodeId, isOn);

      String state = isOn ? "OFF" : "ON";
      socketWriter.println("  >>> Server response: Actuator[" + actuatorId +
          "] on node " + nodeId + " is set to " + state);
    } else {
      Logger.error("Incorrect command format: " + rawCommand);
    }
  }
}
