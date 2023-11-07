package no.ntnu.command;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.message.ErrorMessage;
import no.ntnu.message.Message;
import no.ntnu.message.OkMessage;

/**
 * Command that turns off the actuator when executed.
 * Handles exceptions and provides feedback
 */
public class TurnOffCommand extends Command {
  @Override
  public Message execute(SensorActuatorNode sensorActuatorNode, int actuatorIndex) {
    try {
      sensorActuatorNode.getActuators().get(actuatorIndex).turnOff();
      return new OkMessage("Actuator turned off successfully");
    } catch (IllegalStateException e) {
      return new ErrorMessage("The actuator must be turned on first");
    }

  }
}