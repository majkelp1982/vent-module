package pl.smarthouse.ventmodule.configurations;

import static pl.smarthouse.ventmodule.properties.AirConditionProperties.*;
import static pl.smarthouse.ventmodule.properties.AirExchangerProperties.*;
import static pl.smarthouse.ventmodule.properties.Esp32ModuleProperties.*;
import static pl.smarthouse.ventmodule.properties.FanProperties.*;
import static pl.smarthouse.ventmodule.properties.ForcedAirSystemExchangerProperties.*;
import static pl.smarthouse.ventmodule.properties.PumpProperties.*;
import static pl.smarthouse.ventmodule.properties.ThrottleProperties.THROTTLES;
import static pl.smarthouse.ventmodule.properties.ThrottleProperties.THROTTLES_SERVO_FREQUENCY_HZ;

import javax.annotation.PostConstruct;
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
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

@Configuration
@Getter
public class Esp32ModuleConfig {

  private final pl.smarthouse.smartmodule.model.configuration.Configuration configuration;
  @Autowired ModuleService moduleService;
  @Autowired ManagerService managerService;

  public Esp32ModuleConfig() {
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

    // FAN actors
    actorMap.putActor(
        new Pwm(
            FAN_INLET,
            FAN_INLET_CHANNEL,
            FAN_FREQUENCY,
            FAN_RESOLUTION,
            FAN_INLET_PIN,
            FAN_INLET_DEFAULT_DUTY_CYCLE,
            FAN_INLET_DEFAULT_ENABLED));
    actorMap.putActor(
        new Pwm(
            FAN_OUTLET,
            FAN_OUTLET_CHANNEL,
            FAN_FREQUENCY,
            FAN_RESOLUTION,
            FAN_OUTLET_PIN,
            FAN_OUTLET_DEFAULT_DUTY_CYCLE,
            FAN_OUTLET_DEFAULT_ENABLED));
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
    actorMap.putActor(
        new Pin(PUMP, CIRCUIT_PUMP_PIN, PinMode.OUTPUT, PUMP_DEFAULT_STATE, PUMP_DEFAULT_ENABLED));

    // Air condition device
    actorMap.putActor(
        new Pin(
            AIR_CONDITION,
            AIR_CONDITION_PIN,
            PinMode.OUTPUT,
            AIR_CONDITION_DEFAULT_STATE,
            AIR_CONDITION_DEFAULT_ENABLED));

    // Throttles
    actorMap.putActor(new Pca9685(THROTTLES, THROTTLES_SERVO_FREQUENCY_HZ));

    // Exchanger
    final Ds18b20 ds18b20 = new Ds18b20(EXCHANGER, EXCHANGER_DS18B20_PIN);
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_WATTER_IN,
            Ds18b20CompFactor.builder()
                .gradient(EXCHANGER_WATTER_IN_GRADIENT)
                .intercept(EXCHANGER_WATTER_IN_INTERCEPT)
                .build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_WATTER_OUT,
            Ds18b20CompFactor.builder()
                .gradient(EXCHANGER_WATTER_OUT_GRADIENT)
                .intercept(EXCHANGER_WATTER_OUT_INTERCEPT)
                .build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_AIR_IN,
            Ds18b20CompFactor.builder()
                .gradient(EXCHANGER_AIR_IN_GRADIENT)
                .intercept(EXCHANGER_AIR_IN_INTERCEPT)
                .build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            EXCHANGER_AIR_OUT,
            Ds18b20CompFactor.builder()
                .gradient(EXCHANGER_AIR_OUT_GRADIENT)
                .intercept(EXCHANGER_AIR_OUT_INTERCEPT)
                .build());
    actorMap.putActor(ds18b20);
    return actorMap;
  }
}
