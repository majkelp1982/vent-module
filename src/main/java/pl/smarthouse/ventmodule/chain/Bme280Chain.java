package pl.smarthouse.ventmodule.chain;

import static pl.smarthouse.ventmodule.properties.AirExchangerProperties.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.ActionUtils;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280CommandType;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmodule.model.enums.ActorType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.service.VentModuleService;

@Service
public class Bme280Chain {
  private final ActorMap actorMap;
  private final VentModuleService ventModuleService;

  public Bme280Chain(
      @Autowired final VentModuleService ventModuleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.ventModuleService = ventModuleService;
    actorMap = esp32ModuleConfig.getConfiguration().getActorMap();
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Read BME280 sensors");
    // Wait 10 seconds and read values from each sensor type BME280
    chain.addStep(createStep1());
    // Wait until command read successful and set command to NO_ACTION for all
    chain.addStep(createStep2());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Read values from each sensor type BME280")
        .conditionDescription("Waiting 30 seconds")
        .condition(PredicateUtils.delaySeconds(30))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {
    return () ->
        ActionUtils.setActionToAllActorType(actorMap, ActorType.BME280, Bme280CommandType.READ);
  }

  private Step createStep2() {
    return Step.builder()
        .stepDescription("Set BME280 commands to NO_ACTION")
        .conditionDescription("Wait until command read successful")
        .condition(PredicateUtils.isAllActorTypeReadCommandSuccessful(actorMap, ActorType.BME280))
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {
    return () -> {
      ventModuleService
          .getVentModuleDao()
          .map(
              ventModuleDao -> {
                ventModuleDao
                    .getAirExchanger()
                    .setInlet((Bme280Response) actorMap.getActor(BME280_INLET).getResponse());
                ventModuleDao
                    .getAirExchanger()
                    .setOutlet((Bme280Response) actorMap.getActor(BME280_OUTLET).getResponse());
                ventModuleDao
                    .getAirExchanger()
                    .setFreshAir(
                        (Bme280Response) actorMap.getActor(BME280_FRESH_AIR).getResponse());
                ventModuleDao
                    .getAirExchanger()
                    .setUserAir((Bme280Response) actorMap.getActor(BME280_USED_AIR).getResponse());
                return ventModuleDao;
              })
          .subscribe();
      ActionUtils.setActionToAllActorType(actorMap, ActorType.BME280, Bme280CommandType.NO_ACTION);
    };
  }
}
