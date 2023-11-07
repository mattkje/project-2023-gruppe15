package no.ntnu.run;

import no.ntnu.controlpanel.ControlPanelClient;
import no.ntnu.tools.Logger;

public class ControlPanelStarter {

  /**
   * Entrypoint for the application.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication.
   */
  public static void main(String[] args) {
    ControlPanelClient starter = new ControlPanelClient();
    starter.start();
  }
}
