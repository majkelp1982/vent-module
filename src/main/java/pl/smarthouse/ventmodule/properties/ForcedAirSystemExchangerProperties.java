package pl.smarthouse.ventmodule.properties;

public class ForcedAirSystemExchangerProperties {
  // Exchanger
  public static final String EXCHANGER = "exchanger";
  public static final int EXCHANGER_DS18B20_PIN = 16;
  public static final String EXCHANGER_WATTER_IN = "40-12-1-7-51-138-1-132"; // DS18b20 - 104
  public static final float EXCHANGER_WATTER_IN_GRADIENT = 0.9550f;
  public static final float EXCHANGER_WATTER_IN_INTERCEPT = 5.2f;
  public static final String EXCHANGER_WATTER_OUT = "40-2-0-7-141-53-1-8"; // DS18b20 - 112
  public static final float EXCHANGER_WATTER_OUT_GRADIENT = 1f;
  public static final float EXCHANGER_WATTER_OUT_INTERCEPT = 3.5f;
  public static final String EXCHANGER_AIR_IN = "40-2-0-7-75-42-1-126"; // DS18b20 - 113
  public static final float EXCHANGER_AIR_IN_GRADIENT = 0.97f;
  public static final float EXCHANGER_AIR_IN_INTERCEPT = 5.4f;
  public static final String EXCHANGER_AIR_OUT = "40-255-192-161-96-23-5-84"; // DS18b20 - 118
  public static final float EXCHANGER_AIR_OUT_GRADIENT = 1.075f;
  public static final float EXCHANGER_AIR_OUT_INTERCEPT = -1.5f;

  // Read command:
  // 40-12-1-7-51-138-1-132;40-2-0-7-141-53-1-8;40-2-0-7-75-42-1-126;40-255-192-161-96-23-5-84;

}
