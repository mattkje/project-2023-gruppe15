package no.gruppe15.message;

import no.gruppe15.command.IgnoreCommand;
import no.gruppe15.command.SelectCommand;
import no.gruppe15.command.TurnOffCommand;
import no.gruppe15.command.TurnOnCommand;

/**
 * Serializes messages to protocol-defined strings and vice versa.
 *
 * @author Matti Kjellstadli
 */
public class MessageSerializer {

  public static final String TURN_ON_COMMAND = "1";
  public static final String TURN_OFF_COMMAND = "0";
  public static final String SELECT_COMMAND = "s";

  /**
   * Create message from a string, according to the communication protocol.
   *
   * @param s The string sent over the communication channel
   * @return The logical message, as interpreted according to the protocol
   */
  public static Message fromString(String s) {
    if (s.isEmpty() || s.equals("null")) {
      return new IgnoreCommand();
    }

    if (s.startsWith(SELECT_COMMAND)) {
      // Extract the actuator number from the "select" command
      String number = s.substring(SELECT_COMMAND.length()).trim();
      try {
        return new SelectCommand(number);
      } catch (NumberFormatException e) {
        return new IgnoreCommand();
      }
    }

    char firstS = s.charAt(0);
    return switch (firstS) {
      case 's' -> new SelectCommand(s);
      case '1' -> new TurnOnCommand();
      case '0' -> new TurnOffCommand();
      default -> new IgnoreCommand();
    };
  }

  /**
   * Returns the command as a string.
   *
   * @param m message to be sent
   * @return command as message
   */
  public static String toString(Message m) {
    String s = null;
    if (m instanceof TurnOffCommand) {
      s = TURN_OFF_COMMAND;
    } else if (m instanceof TurnOnCommand) {
      s = TURN_ON_COMMAND;
    } else if (m instanceof SelectCommand selectCommand) {
      s = SELECT_COMMAND + " " + selectCommand.getActuator();
    }
    return s;
  }

  public static void processCommand(String command) {
    String[] parts = command.split(" "); // Split the command into parts
    if (parts.length >= 2) {
      String commandType = parts[0];
      if (commandType.equalsIgnoreCase("select")) {
        try {
          int actuatorNumber = Integer.parseInt(parts[1]);
          new SelectCommand(""+actuatorNumber);
        } catch (NumberFormatException e) {
          System.err.println("Invalid actuator number.");
        }
      } else {
        System.err.println("Unknown command: " + commandType);
      }
    } else {
      System.err.println("Invalid command format. Usage: select <actuatorNumber>");
    }
  }
}
