package pl.smarthouse.ventmodule.properties;

import pl.smarthouse.smartmodule.model.actors.type.pin.PinState;

public class AirConditionProperties {
  // Circuit pomp
  public static final String AIR_CONDITION = "air_condition";
  public static final PinState AIR_CONDITION_DEFAULT_STATE = PinState.HIGH;
  public static final boolean AIR_CONDITION_DEFAULT_ENABLED = true;
  public static final int AIR_CONDITION_PIN = 25;
}
