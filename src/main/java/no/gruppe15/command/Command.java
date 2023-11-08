package no.gruppe15.command;

import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.Message;

/**
 * A command sent from the client to the server (from remote to TV).
 */
public abstract class Command implements Message {
  /**
   * Execute the command.
   *
   * @return The message which contains the output of the command
   */
  public abstract Message execute(Actuator actuator);
}

