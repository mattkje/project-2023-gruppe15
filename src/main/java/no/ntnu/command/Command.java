package no.ntnu.command;

import no.ntnu.greenhouse.SensorActuatorNode;
import no.ntnu.message.Message;
import no.ntnu.greenhouse.Actuator;


/**
 * A command sent from the client to the server (from remote to TV).
 */
public abstract class Command implements Message {
  /**
   * Execute the command.
   *
   * @param sensorActuatorNode The Actuator to be affected by this command
   * @return The message which contains the output of the command
   */
  public abstract Message execute(SensorActuatorNode sensorActuatorNode, int actuatorIndex);
}
