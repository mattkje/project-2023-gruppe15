package no.gruppe15.command;

import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.greenhouse.SensorActuatorNode;
import no.gruppe15.message.Message;

public class IgnoreCommand extends Command{
  @Override
  public Message execute(Actuator actuator) {
    return null;
  }
}
