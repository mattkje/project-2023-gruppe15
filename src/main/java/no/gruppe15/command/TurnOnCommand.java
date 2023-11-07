package no.gruppe15.command;

import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.ErrorMessage;
import no.gruppe15.message.Message;
import no.gruppe15.message.OkMessage;

/**
 * A command requesting to turn on the TV when executed.
 * Handles exceptions when TV is already turned on
 */
public class TurnOnCommand extends Command {
  @Override
  public Message execute(SensorActuatorNode sensorActuatorNode, int actuatorIndex) {
    try {
      sensorActuatorNode.getActuators().get(actuatorIndex).turnOn();
      return new OkMessage("Actuator turned on successfully");
    } catch (IllegalStateException e) {
      return new ErrorMessage("The actuator is already on");
    }

  }

}
