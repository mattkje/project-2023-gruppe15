package no.ntnu.command;

import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.message.ErrorMessage;
import no.ntnu.message.Message;
import no.ntnu.message.OkMessage;

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
