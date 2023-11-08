package no.gruppe15.command;

import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.message.Message;
import no.gruppe15.message.OkMessage;
import no.gruppe15.tools.Logger;

public class TurnOnCommand extends Command{
  @Override
  public Message execute(String actuatorType, int nodeId) {
    Actuator actuator = new Actuator(actuatorType, nodeId);
    if (actuator.isOn()){
      actuator.turnOn();
      return new OkMessage("Actuator successfully turned on");
    } else {
      return new OkMessage("Actuator successfully turned off");
    }
  }
}
