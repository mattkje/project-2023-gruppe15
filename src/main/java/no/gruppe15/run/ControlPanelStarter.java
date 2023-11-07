package no.gruppe15.run;

import no.gruppe15.controlpanel.ControlPanelClient;

public class ControlPanelStarter {

  /**
   * Entrypoint for the application.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication.
   */
  public static void main(String[] args) {
    ControlPanelClient client = new ControlPanelClient();
    if (client.start()) {
      System.out.println("Server found: " + client.getServerHost());
    } else {
      System.out.println("No server was found. Starting in disconnected mode");
    }

    client.stopCommunication();

  }
}
