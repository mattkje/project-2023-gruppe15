package no.gruppe15.greenhouse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
  public void run() {
    String clientAddress = socket.getRemoteSocketAddress().toString();
    try {
      BufferedReader socketReader =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));

      while (true) {
        String line = socketReader.readLine();
        if (line == null) {
          break;
        }
      }

      System.out.println("Client at " + clientAddress + " has disconnected.");
    } catch (IOException e) {

      System.err.println("Error with client at " + clientAddress + ": " + e.getMessage());
    }
  }


}
