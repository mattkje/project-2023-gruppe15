package no.gruppe15.command;


import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.ErrorMessage;
import no.gruppe15.message.Message;

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