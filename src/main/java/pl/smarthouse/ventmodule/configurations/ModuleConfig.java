package pl.smarthouse.ventmodule.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.model.actors.actor.Actor;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.typelibs.BME280.BME280;
import pl.smarthouse.smartmodule.model.actors.typelibs.PWM.PWM;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class ModuleConfig {
  // Actors
  // Temp, humid, pressure sensors
  public static final String BME280_INTAKE = "bme280_intake";
  public static final String BME280_OUTLET = "bme280_outlet";
  public static final String BME280_FRESH_AIR = "bme280_fresh_air";
  public static final String BME280_USED_AIR = "bme280_used_air";
  // Fan PWM controllers
  public static final int FAN_FREQUENCY = 1000;
  public static final int FAN_RESOLUTION = 8;
  public static final String FAN_INTAKE = "fan_intake";
  public static final int FAN_INTAKE_CHANNEL = 2;
  public static final int FAN_INTAKE_PIN = 33;

  public static final String FAN_OUTLET = "fan_outlet";
  public static final int FAN_OUTLET_CHANNEL = 3;
  public static final int FAN_OUTLET_PIN = 2;

  // Module specific
  private static final String FIRMWARE = "20230104.17";
  private static final String VERSION = "20230103.22";
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
    actorMap.putActor(new BME280(BME280_INTAKE, 13));
    actorMap.putActor(new BME280(BME280_OUTLET, 14));
    actorMap.putActor(new BME280(BME280_FRESH_AIR, 27));
    actorMap.putActor(new BME280(BME280_USED_AIR, 26));

    // PWM actors
    actorMap.putActor(
        new PWM(FAN_INTAKE, FAN_INTAKE_CHANNEL, FAN_FREQUENCY, FAN_RESOLUTION, FAN_INTAKE_PIN));
    actorMap.putActor(
        new PWM(FAN_OUTLET, FAN_OUTLET_CHANNEL, FAN_FREQUENCY, FAN_RESOLUTION, FAN_OUTLET_PIN));
    return actorMap;
  }

  public Actor getActor(final String name) {
    return configuration.getActorMap().getActor(name);
  }
}
