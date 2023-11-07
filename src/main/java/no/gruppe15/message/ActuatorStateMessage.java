package no.gruppe15.message;

/**
 * A message telling whether the actuator is ON or off.
 */
public record ActuatorStateMessage(boolean isOn) implements Message {
  /**
   * Create a actuator state message.
   *
   * @param isOn The actuator is ON if this is true, the actuator is off if this is false.
   */
  public ActuatorStateMessage {
  }

  /**
   * Check whether the actuator is ON.
   *
   * @return ON if true, OFF if false
   */
  @Override
  public boolean isOn() {
    return isOn;
  }

}
