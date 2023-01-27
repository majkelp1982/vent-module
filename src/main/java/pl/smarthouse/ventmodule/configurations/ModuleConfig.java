package pl.smarthouse.ventmodule.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20CompFactor;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685;
import pl.smarthouse.smartmodule.model.actors.type.pin.Pin;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinMode;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinState;
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class ModuleConfig {
  // Actors

  // Temp, humid, pressure sensors
  public static final String BME280_INLET = "bme280_inlet";
  public static final int BME280_INLET_PIN = 13;
  public static final String BME280_OUTLET = "bme280_outlet";
  public static final int BME280_OUTLET_PIN = 14;
  public static final String BME280_FRESH_AIR = "bme280_fresh_air";
  public static final int BME280_FRESH_AIR_PIN = 27;
  public static final String BME280_USED_AIR = "bme280_used_air";
  public static final int BME280_USED_AIR_PIN = 26;

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
  public static final int FAN_OUTLET_REV_COUNTER_PIN = 15;
  public static final String FAN_INLET_REV_COUNTER = "fan_inlet_rev";
  public static final int FAN_INLET_REV_COUNTER_PIN = 32;
  public static final int TIMEBASE_IN_SECONDS = 10;

  // Circuit pomp
  public static final String PUMP = "pump";
  public static final int CIRCUIT_PUMP_PIN = 17;

  // Servo driver
  public static final String THROTTLES = "throttles";
  public static final int THROTTLES_SERVO_FREQUENCY_HZ = 50;

  // Exchanger
  public static final String EXCHANGER = "exchanger";
  public static final int EXCHANGER_DS18B20_PIN = 15;
  public static final String EXCHANGER_WATTER_IN = "40-12-1-7-51-138-1-132"; // DS18b20 - 104
  public static final String EXCHANGER_WATTER_OUT = "40-2-0-7-141-53-1-8"; // DS18b20 - 112
  public static final String EXCHANGER_AIR_IN = "40-2-0-7-75-42-1-126"; // DS18b20 - 113
  public static final String EXCHANGER_AIR_OUT = "40-255-192-161-96-23-5-84"; // DS18b20 - 118
  // Read command:
  // 40-12-1-7-51-138-1-132;40-2-0-7-141-53-1-8;40-2-0-7-75-42-1-126;40-255-192-161-96-23-5-84;

  // Module specific
  private static final String FIRMWARE = "20230122.00";
  private static final String VERSION = "20230107.23";
  private static final String MAC_ADDRESS = "3C:71:BF:4D:6A:40";
  private static final String MODULE_TYPE = "VENTILATION";
  private final pl.smarthouse.smartmodule.model.configuration.Configuration configuration;
  @Autowired ModuleService moduleService;
  @Autowired ManagerService managerService;

  public ModuleConfig() {
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            MODULE_TYPE, FIRMWARE, VERSION, MAC_ADDRESS, createActors());
  }

  @PostConstruct
  public void postConstruct() {
    moduleService.setConfiguration(configuration);
    managerService.setConfiguration(configuration);
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();

    // BME280 sensors
    actorMap.putActor(new Bme280(BME280_INLET, BME280_INLET_PIN));
    actorMap.putActor(new Bme280(BME280_OUTLET, BME280_OUTLET_PIN));
    actorMap.putActor(new Bme280(BME280_FRESH_AIR, BME280_FRESH_AIR_PIN));
    actorMap.putActor(new Bme280(BME280_USED_AIR, BME280_USED_AIR_PIN));

    // PWM actors
    actorMap.putActor(
        new Pwm(
            FAN_INLET, FAN_INLET_CHANNEL, FAN_FREQUENCY, FAN_RESOLUTION, FAN_INLET_PIN, 0, true));
    actorMap.putActor(
        new Pwm(
            FAN_OUTLET,
            FAN_OUTLET_CHANNEL,
            FAN_FREQUENCY,
            FAN_RESOLUTION,
            FAN_OUTLET_PIN,
            0,
            true));
    actorMap.putActor(
        new Pin(
            FAN_INLET_REV_COUNTER,
            FAN_INLET_REV_COUNTER_PIN,
            PinMode.LOW_STATE_COUNTER,
            TIMEBASE_IN_SECONDS));
    actorMap.putActor(
        new Pin(
            FAN_OUTLET_REV_COUNTER,
            FAN_OUTLET_REV_COUNTER_PIN,
            PinMode.LOW_STATE_COUNTER,
            TIMEBASE_IN_SECONDS));

    // Circuit pump
    actorMap.putActor(new Pin(PUMP, CIRCUIT_PUMP_PIN, PinMode.OUTPUT, PinState.LOW, false));

    // Throttles
    actorMap.putActor(new Pca9685(THROTTLES, THROTTLES_SERVO_FREQUENCY_HZ));

    // Exchanger
    final Ds18b20 ds18b20 = new Ds18b20(EXCHANGER, EXCHANGER_DS18B20_PIN);
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_WATTER_IN,
            Ds18b20CompFactor.builder().gradient(0.9550f).intercept(2.1f).build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(EXCHANGER_WATTER_OUT, Ds18b20CompFactor.builder().gradient(1f).intercept(0f).build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(EXCHANGER_AIR_IN, Ds18b20CompFactor.builder().gradient(0.97f).intercept(1.6f).build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_AIR_OUT, Ds18b20CompFactor.builder().gradient(1.075f).intercept(-5f).build());
    actorMap.putActor(ds18b20);
    return actorMap;
  }
}
