package no.ntnu.controlpanel;

import static no.ntnu.greenhouse.GreenhouseSimulator.PORT_NUMBER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import no.ntnu.gui.controlpanel.ControlPanelApplication;
import no.ntnu.tools.Logger;

/**
 * Starter class for the control panel.
 * Note: we could launch the Application class directly, but then we would have issues with the
 * debugger (JavaFX modules not found)
 */
public class ControlPanelClient {

  private static final String SERVER_HOST = "localhost";
  private Socket socket;
  private BufferedReader socketReader;
  private PrintWriter socketWriter;

  public boolean start() {
    boolean connected = false;
    CommunicationChannel channel;
    ControlPanelLogic logic = new ControlPanelLogic();
    try {
      channel = initiateCommunication(logic);
      ControlPanelApplication.startApp(logic, channel);
      // This code is reached only after the GUI-window is closed
      Logger.info("Exiting the control panel application");
      stopCommunication();
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
   * This method should return the current server host.
   *
   * @return the current server host.
   */
  public String getServerHost() {
    return SERVER_HOST + ":" + PORT_NUMBER;
  }

  private CommunicationChannel initiateCommunication(ControlPanelLogic logic) throws IOException {
    // TODO - here you initiate TCP/UDP socket communication
    // You communication class(es) may want to get reference to the logic and call necessary
    // logic methods when events happen (for example, when sensor data is received)
    socket = new Socket(SERVER_HOST, PORT_NUMBER);
    socketWriter = new PrintWriter(socket.getOutputStream(), true);
    socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    return null;
  }

  private void stopCommunication() {
    // TODO - here you stop the TCP/UDP socket communication
  }
}
