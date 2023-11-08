package no.gruppe15.greenhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import no.gruppe15.command.Command;
import no.gruppe15.command.TurnOnCommand;
import no.gruppe15.message.Message;
import no.gruppe15.message.MessageSerializer;
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
    Message response;
    while ((response = executeClientCommand()) != null) {
      simulator.broadcastMessageToAllClients(response);
    }

    String clientAddress = socket.getRemoteSocketAddress().toString();
    System.out.println("Client at " + clientAddress + " has disconnected.");
    simulator.removeDisconnectedClient(this);
  }

  private Message executeClientCommand() {
    Command clientCommand = getClientCommand();
    if (clientCommand == null) {
      return null;
    }

    String commandName = clientCommand.getClass().getSimpleName();
    Logger.info("Received a " + commandName + " from the client.");
    return sendResponseToClient(clientCommand.execute("window", 1));
  }

  private Command getClientCommand() {
    Message clientCommand = null;
    try {
      String rawClientRequest = socketReader.readLine();
      clientCommand = MessageSerializer.fromString(rawClientRequest);
      if (!(clientCommand instanceof Command)) {
        if (clientCommand != null) {
          Logger.error("Received an unexpected message from the client: " + clientCommand);
        }
        clientCommand = null;
      }
    } catch (IOException e) {
      Logger.error("Failed to receive the client request: " + e.getMessage());
    } catch (NullPointerException e1) {
      Logger.info("The client has lost the connection");
    }

    assert clientCommand instanceof Command : "Expected a Command but received: " + clientCommand;
    return (Command) clientCommand;
  }

  public Message sendResponseToClient(Message message) {
    socketWriter.println(message);
    return message;
  }


}
