package no.ntnu.greenhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import no.ntnu.tools.Logger;

/**
 * This class is responsible for handling the TCP communication for one client.
 * TODO: Change to support this app.
 *
 * @author Matti Kjellstadli, Adrian Johansen, Håkon Karlsen, Di Xie
 * @version 06.11.2023
 */
public class ClientHandler extends Thread {
  private final Socket socket;

  private final Server server;

  private final BufferedReader socketReader;

  private final PrintWriter socketWriter;

  /**
   * Creates an instance of ClientHandler.
   *
   * @param socket Socket associated with this client
   * @param server Reference to the main TCP server class
   * @throws IOException When something goes wrong with establishing the input or output streams
   */
  public ClientHandler(Socket socket, Server server) throws IOException {
    this.server = server;
    this.socket = socket;
    socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    socketWriter = new PrintWriter(socket.getOutputStream(), true);
  }

  public void sendResponseToClient(Logger message) {
    //TODO Implement this.
  }
}
