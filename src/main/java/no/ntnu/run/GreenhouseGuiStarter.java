package no.ntnu.run;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.greenhouse.Server;
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
    SensorActuatorNode logic = new SensorActuatorNode(1);
    Server server = new Server(logic);
    Thread serverThread = new Thread(server::startServer);
    serverThread.start();
    boolean fake = false;

    //TODO Remove fake when appropriate.
    if (args.length == 1 && "fake".equals(args[0])) {
      fake = true;
      Logger.info("Using FAKE events");
    }

    GreenhouseApplication.startApp(fake);
    server.stopServer();
  }
}
