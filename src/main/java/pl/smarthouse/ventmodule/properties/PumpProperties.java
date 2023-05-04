package pl.smarthouse.ventmodule.properties;

import pl.smarthouse.smartmodule.model.actors.type.pin.PinState;

public class PumpProperties {
  // Circuit pomp
  public static final String PUMP = "pump";
  public static final PinState PUMP_DEFAULT_STATE = PinState.HIGH;
  public static final boolean PUMP_DEFAULT_ENABLED = true;
  public static final int CIRCUIT_PUMP_PIN = 17;
}
