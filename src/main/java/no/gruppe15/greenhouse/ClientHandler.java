package no.gruppe15.greenhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import no.gruppe15.tools.Logger;

public class ClientHandler extends Thread {
  private final Socket socket;

  private final GreenhouseSimulator simulator;

  private final BufferedReader socketReader;

  private final PrintWriter socketWriter;

  /**
   * Creates an instance of ClientHandler.
   *
   * @param socket Socket associated with this client
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
      Logger.error("An error occurred while reading from the socket: "+ e.getMessage());
    }

    String clientAddress = socket.getRemoteSocketAddress().toString();
    System.out.println("Client at " + clientAddress + " has disconnected.");
    simulator.removeDisconnectedClient(this);
  }

  private void processCommand(String rawCommand) {
    handleRawCommand(rawCommand);
    Logger.info("Recieved a command from client: " + rawCommand);
    socketWriter.println("Sucessfully initiated actuator " + rawCommand);
  }

  private void handleRawCommand(String rawCommand) {
    if (rawCommand.equals("getNodes")){
      socketWriter.println(simulator.getNodes());
      return;
    }
    String[] parts = rawCommand.split(",");
    if (parts.length == 3) {
      int nodeId = Integer.parseInt(parts[0].trim());
      int actuatorId = Integer.parseInt(parts[1].trim());
      int on = Integer.parseInt(parts[2].trim());
      boolean isOn = (on != 0);
      simulator.handleActuator(actuatorId, nodeId, isOn);
    } else {
      Logger.error("Wrong format!");
    }
  }

}
