package no.gruppe15.command;

import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.ErrorMessage;
import no.gruppe15.message.Message;
import no.gruppe15.message.OkMessage;

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