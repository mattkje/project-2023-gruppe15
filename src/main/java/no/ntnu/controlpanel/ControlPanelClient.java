package no.ntnu.controlpanel;

import static no.ntnu.greenhouse.GreenhouseServer.PORT_NUMBER;

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

  public void start() {
    ControlPanelLogic logic = new ControlPanelLogic();
    CommunicationChannel channel = initiateCommunication(logic);
    ControlPanelApplication.startApp(logic, channel);
    // This code is reached only after the GUI-window is closed
    Logger.info("Exiting the control panel application");
    stopCommunication();
  }

  private CommunicationChannel initiateCommunication(ControlPanelLogic logic) {
    // TODO - here you initiate TCP/UDP socket communication
    // You communication class(es) may want to get reference to the logic and call necessary
    // logic methods when events happen (for example, when sensor data is received)
    try {
      socket = new Socket(SERVER_HOST, PORT_NUMBER);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      System.err.println("Could not connect to the server: " + e.getMessage());
    }

    return null;
  }

  private CommunicationChannel initiateFakeSpawner(ControlPanelLogic logic) {
    // Here we pretend that some events will be received with a given delay
    FakeCommunicationChannel spawner = new FakeCommunicationChannel(logic);
    logic.setCommunicationChannel(spawner);
    spawner.spawnNode("4;3_window", 2);
    spawner.spawnNode("1", 3);
    spawner.spawnNode("1", 4);
    spawner.advertiseSensorData("4;temperature=27.4 °C,temperature=26.8 °C,humidity=80 %", 4);
    spawner.spawnNode("8;2_heater", 5);
    spawner.advertiseActuatorState(4, 1, true, 5);
    spawner.advertiseActuatorState(4,  1, false, 6);
    spawner.advertiseActuatorState(4,  1, true, 7);
    spawner.advertiseActuatorState(4,  2, true, 7);
    spawner.advertiseActuatorState(4,  1, false, 8);
    spawner.advertiseActuatorState(4,  2, false, 8);
    spawner.advertiseActuatorState(4,  1, true, 9);
    spawner.advertiseActuatorState(4,  2, true, 9);
    spawner.advertiseSensorData("4;temperature=22.4 °C,temperature=26.0 °C,humidity=81 %", 9);
    spawner.advertiseSensorData("1;humidity=80 %,humidity=82 %", 10);
    spawner.advertiseRemovedNode(8, 11);
    spawner.advertiseRemovedNode(8, 12);
    spawner.advertiseSensorData("1;temperature=25.4 °C,temperature=27.0 °C,humidity=67 %", 13);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 14);
    spawner.advertiseSensorData("4;temperature=25.4 °C,temperature=27.0 °C,humidity=82 %", 16);
    return spawner;
  }

  private void stopCommunication() {
    // TODO - here you stop the TCP/UDP socket communication
  }
}
