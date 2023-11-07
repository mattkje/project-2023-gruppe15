package no.gruppe15.command;

import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.ErrorMessage;
import no.gruppe15.message.Message;
import no.gruppe15.message.OkMessage;

public class SelectCommand extends Command{
  private final String actuator;

  public SelectCommand(String actuator) {
    this.actuator = actuator;
  }

  @Override
  public Message execute(SensorActuatorNode sensorActuatorNode, int actuatorIndex) {
    try {
      sensorActuatorNode.setActuator(actuatorToInt(), true);
      return new OkMessage("Selected actuator " + actuatorToInt());
    } catch (IllegalStateException e) {
      return new ErrorMessage("The actuator must be turned on first");
    } catch (IllegalArgumentException e2) {
      return new ErrorMessage("This actuator does not exist");
    }
  }

  /**
   * Reformats the users input to a readable format.
   * Format: C (Change channel command) + # (Channel number to change to)
   * Handles wrong formats and gives feedback
   *
   * @return channel number as int
   */
  public int actuatorToInt() {
    if (actuator.matches("s\\d+")) {
      return Integer.parseInt(actuator.substring(1));
    } else {
      throw new IllegalArgumentException("Invalid actuator format: " + actuator);
    }
  }

  public String getActuator() {
    return actuator;
  }
}
