package pl.smarthouse.ventmodule.chain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.ActionUtils;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280CommandType;
import pl.smarthouse.smartmodule.model.enums.ActorType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;

@Service
public class Bme280Chain {
  private final ActorMap actorMap;

  public Bme280Chain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
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
        .conditionDescription("Waiting 10 seconds")
        .condition(PredicateUtils.delaySeconds(10))
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
    return () ->
        ActionUtils.setActionToAllActorType(
            actorMap, ActorType.BME280, Bme280CommandType.NO_ACTION);
  }
}
