package no.gruppe15.run;

import no.gruppe15.controlpanel.ControlPanelLogic;
import no.gruppe15.controlpanel.ControlPanelSocket;
import no.gruppe15.tools.Logger;

/**
 * Run the control panel using command-line interface (no GUI).
 */
public class CommandControlStarter {
  /**
   * Application entrypoint for the command-line version of the control panel.
   *
   * @param args Command line arguments, only the first one of them used: when it is "fake",
   *             emulate fake events, when it is either something else or not present,
   *             use real socket communication.
   */
  public static void main(String[] args) {
    Logger.info("Starting control panel in command line...");
    ControlPanelLogic logic = new ControlPanelLogic();
    ControlPanelSocket starter = new ControlPanelSocket(logic, false);
    starter.openCommandLine();
  }
}
