package no.gruppe15.command;

import no.gruppe15.greenhouse.Actuator;
import no.gruppe15.message.Message;
import no.gruppe15.message.OkMessage;

public class ToggleActuatorCommand extends Command{
  @Override
  public Message execute(Actuator actuator) {
    if (!actuator.isOn()){
      actuator.turnOn();
      return new OkMessage("Actuator successfully turned on");
    } else {
      actuator.turnOff();
      return new OkMessage("Actuator successfully turned off");
    }
  }
}
