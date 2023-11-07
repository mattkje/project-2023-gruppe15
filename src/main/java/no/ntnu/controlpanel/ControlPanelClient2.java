package no.ntnu.controlpanel;

import static no.ntnu.greenhouse.GreenhouseSimulator.PORT_NUMBER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * This class is only here to help developing the actual client class.
 *
 * @author Matti Kjellstadli, Adrian Johansen, Håkon Karlsen, Di Xie
 * @version 02.11.2023
 */
public class ControlPanelClient2 {

  private static final String SERVER_HOST = "localhost";
  private Socket socket;
  private BufferedReader socketReader;
  private PrintWriter socketWriter;

  /**
   * Attempts to establish a connection to the remote server. If successful,
   * initializes the necessary communication streams and sets up the connection.
   *
   * @return True if the connection to the server was successfully established; false otherwise.
   */
  public boolean start() {
    boolean connected = false;
    try {
      socket = new Socket(SERVER_HOST, PORT_NUMBER);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      connected = true;
    } catch (IOException e) {
      System.err.println("Could not connect to the server: " + e.getMessage());
    }
    return connected;
  }

  /**
   * Closes the socket and nullifies associated resources,
   * including the socket itself, socketReader, and socketWriter.
   */
  public void stop() {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        System.err.println("Error while closing the socket: " + e.getMessage());
      } finally {
        socket = null;
        socketReader = null;
        socketWriter = null;
      }
    }
  }

  /**
   * This method checks if socket is null.
   *
   * @return true if socket is not null, false otherwise.
   */
  public boolean isServerRunning() {
    return socket != null;
  }

  /**
   * This method should return the current server host.
   *
   * @return the current server host.
   */
  public String getServerHost() {
    return SERVER_HOST + ":" + PORT_NUMBER;
  }


  /**
   * This method checks if the socket can write a byte.
   *
   * @return True if the socket is able to write byte, false otherwise.
   */
  public boolean isConnected() {
    if (socket != null && socket.isConnected() && !socket.isClosed()) {
      try {
        socket.getOutputStream().write(0);
        return true;
      } catch (IOException e) {
        return false;
      }
    }
    return false;
  }
}