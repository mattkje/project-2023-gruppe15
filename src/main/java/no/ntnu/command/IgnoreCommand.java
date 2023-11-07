package no.ntnu.command;


import no.ntnu.greenhouse.Actuator;
import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.message.ErrorMessage;
import no.ntnu.message.Message;

/**
 * Command that is sent when user input is to be ignored.
 * Returns "invalid command"
 */
public class IgnoreCommand extends Command {
  @Override
  public Message execute(SensorActuatorNode sensorActuatorNode, int actuatorIndex) {
    return new ErrorMessage("Invalid command");
  }
}
