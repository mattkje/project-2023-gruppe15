package no.ntnu.run;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.greenhouse.GreenhouseServer;
import no.ntnu.gui.greenhouse.GreenhouseApplication;
import no.ntnu.tools.Logger;

/**
 * Starter for GUI version of the greenhouse simulator.
 */
public class GreenhouseGuiStarter {
  /**
   * Entrypoint gor the Greenhouse GUI application.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication.
   */
  public static void main(String[] args) {
    GreenhouseServer server = new GreenhouseServer();
    Thread serverThread = new Thread(server::startServer);
    serverThread.start();

    GreenhouseApplication.startApp();
    server.stopServer();
  }
}
