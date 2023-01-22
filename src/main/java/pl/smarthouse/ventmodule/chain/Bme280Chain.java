package pl.smarthouse.ventmodule.chain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.Chain;
import pl.smarthouse.smartchain.model.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.ActionUtils;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280CommandType;
import pl.smarthouse.smartmodule.model.actors.type.bme280.Bme280Response;
import pl.smarthouse.smartmodule.model.enums.ActorType;
import pl.smarthouse.ventmodule.configurations.ModuleConfig;

import java.util.Objects;
import java.util.function.Predicate;

@Service
public class Bme280Chain {
  private ActorMap actorMap;
  private final Predicate<Step> isReadCommandSuccessful =
      step ->
          actorMap.stream()
              .filter(actor -> ActorType.BME280.equals(actor.getType()))
              .map(actor -> (Bme280) actor)
              .allMatch(
                  bme280 -> {
                    Bme280Response bme280Response = bme280.getResponse();
                    if (Objects.isNull(bme280Response)
                        || Objects.isNull(bme280Response.getResponseUpdate())) {
                      return false;
                    } else {
                      return step.getStartTime().isBefore(bme280Response.getResponseUpdate());
                    }
                  });

  public Bme280Chain(
      @Autowired final ChainService chainService, @Autowired final ModuleConfig moduleConfig) {
    actorMap = moduleConfig.getConfiguration().getActorMap();
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
        .condition(isReadCommandSuccessful)
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {
    return () ->
        ActionUtils.setActionToAllActorType(
            actorMap, ActorType.BME280, Bme280CommandType.NO_ACTION);
  }
}
