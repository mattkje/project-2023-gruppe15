package no.ntnu.run;

import no.ntnu.greenhouse.GreenhouseSimulator;
import no.ntnu.gui.greenhouse.GreenhouseApplication;

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
    GreenhouseSimulator server = new GreenhouseSimulator();
    Thread serverThread = new Thread(server::start);
    serverThread.start();

    GreenhouseApplication.startApp();
    server.stop();
  }
}
