package no.gruppe15.message;

import no.gruppe15.command.IgnoreCommand;
import no.gruppe15.command.TurnOffCommand;
import no.gruppe15.command.ToggleActuatorCommand;

/**
 * Serializes messages to protocol-defined strings and vice versa.
 *
 * @author Girts Strazdins
 * @see <a href="https://github.com/strazdinsg/datakomm-tools/tree/master" target="_blank">External Repository</a>
 */
public class MessageSerializer {

  public static final String TURN_ON_COMMAND = "1";
  public static final String TURN_OFF_COMMAND = "0";


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
    char firstS = s.charAt(0);
    return switch (firstS) {
      case '1' -> new ToggleActuatorCommand();
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
    } else if (m instanceof ToggleActuatorCommand) {
      s = TURN_ON_COMMAND;
    }
    return s;
  }
}
