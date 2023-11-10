package no.gruppe15.gui.controlpanel;

import java.util.Scanner;
import no.gruppe15.controlpanel.ControlPanelSocket;
import no.gruppe15.tools.Logger;

public class CommandLineControlPanel {

  private ControlPanelSocket socket;

  public CommandLineControlPanel(ControlPanelSocket socket){
    this.socket = socket;
  }

  public void startCommandControl(Scanner input) {
    Logger.infoNoNewline("Enter command: ");
    String userInput = input.nextLine();
    String[] parts = userInput.split(" ");

    if (parts.length > 0) {
      String command = parts[0];

      if ("select".equals(command)) {
        handleSelectCommand(parts, input);
      } else if ("list".equals(command)) {
        handleListCommand(input);
      } else {
        Logger.info("Invalid command");
        Logger.help("Available commands");
        Logger.help("   select n: select a node with number n");
        Logger.help("   list:  n: List actuators in node with number n");
      }
    } else {
      Logger.info("Empty command");
    }

    startCommandControl(input);
  }

  private void handleSelectCommand(String[] parts, Scanner input) {
    if (parts.length >= 2) {
      String nodeId = parts[1];
      Logger.info("Node " + nodeId + " selected");
      Logger.infoNoNewline("Enter command: ");
      String userInput = input.nextLine();
      String[] commandParts = userInput.split(" ");

      if (commandParts.length >= 2) {
        handleActuatorChangeCommand(nodeId, commandParts, input);
      } else {
        Logger.info("Node does not exist");
        startCommandControl(input);
      }
    } else {
      Logger.info("Invalid 'select' command format");
      startCommandControl(input);
    }
  }

  private void handleActuatorChangeCommand(String nodeId, String[] commandParts, Scanner input) {
    boolean on = !commandParts[1].equals("0");
    socket.sendActuatorChange(Integer.parseInt(nodeId), Integer.parseInt(commandParts[0]), on);
    startCommandControl(input);
  }

  private void handleListCommand(Scanner input) {
    // Implement logic for the 'list' command to list all nodes
    // ...
    Logger.info("List command not implemented yet");
    startCommandControl(input);
  }

}
