package pl.smarthouse.ventmodule.properties;

public class FanProperties {
  // Fan PWM controllers
  // Generic
  public static final int FAN_FREQUENCY = 1000;
  public static final int FAN_RESOLUTION = 8;
  // Fan intake
  public static final String FAN_INLET = "fan_inlet";
  public static final int FAN_INLET_CHANNEL = 2;
  public static final int FAN_INLET_PIN = 33;
  // Fan outlet
  public static final String FAN_OUTLET = "fan_outlet";
  public static final int FAN_OUTLET_CHANNEL = 3;
  public static final int FAN_OUTLET_PIN = 2;
  // Rev counters
  public static final String FAN_OUTLET_REV_COUNTER = "fan_outlet_rev";
  public static final int FAN_OUTLET_REV_COUNTER_PIN = 4;
  public static final String FAN_INLET_REV_COUNTER = "fan_inlet_rev";
  public static final int FAN_INLET_REV_COUNTER_PIN = 32;
  public static final int TIMEBASE_IN_SECONDS = 10;
}
