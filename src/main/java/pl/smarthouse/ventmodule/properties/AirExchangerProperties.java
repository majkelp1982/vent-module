package pl.smarthouse.ventmodule.properties;

public class AirExchangerProperties {
  // Temp, humid, pressure sensors
  public static final String BME280_INLET = "bme280_inlet";
  public static final int BME280_INLET_PIN = 13;
  public static final String BME280_OUTLET = "bme280_outlet";
  public static final int BME280_OUTLET_PIN = 14;
  public static final String BME280_FRESH_AIR = "bme280_fresh_air";
  public static final int BME280_FRESH_AIR_PIN = 27;
  public static final String BME280_USED_AIR = "bme280_used_air";
  public static final int BME280_USED_AIR_PIN = 26;
}
