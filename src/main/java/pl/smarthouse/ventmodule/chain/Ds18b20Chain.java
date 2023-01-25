package pl.smarthouse.ventmodule.chain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.Chain;
import pl.smarthouse.smartchain.model.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.ActionUtils;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20CommandType;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Utils;
import pl.smarthouse.smartmodule.model.enums.ActorType;
import pl.smarthouse.ventmodule.configurations.ModuleConfig;

import static pl.smarthouse.ventmodule.configurations.ModuleConfig.*;

@Service
public class Ds18b20Chain {
  private final ActorMap actorMap;

  public Ds18b20Chain(
      @Autowired final ChainService chainService, @Autowired final ModuleConfig moduleConfig) {
    actorMap = moduleConfig.getConfiguration().getActorMap();
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Read DS18B20 sensors");
    // Wait 10 seconds and read values from each sensor type DS18B20
    chain.addStep(createStep1());
    // Wait until command read successful and set command to NO_ACTION for all
    chain.addStep(createStep2());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Read values from each sensor type DS18B20")
        .conditionDescription("Waiting 10 seconds")
        .condition(PredicateUtils.delaySeconds(10))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {
    return () ->
        ActionUtils.setActionToAllActorType(
            actorMap,
            ActorType.DS18B20,
            Ds18b20CommandType.READ,
            Ds18b20Utils.getDs18b20Command(
                EXCHANGER_WATTER_IN, EXCHANGER_WATTER_OUT, EXCHANGER_AIR_IN, EXCHANGER_AIR_OUT));
  }

  private Step createStep2() {
    return Step.builder()
        .stepDescription("Set DS18B20 commands to NO_ACTION")
        .conditionDescription("Wait until command read successful")
        .condition(PredicateUtils.isAllActorTypeReadCommandSuccessful(actorMap, ActorType.DS18B20))
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {
    return () ->
        ActionUtils.setActionToAllActorType(
            actorMap, ActorType.DS18B20, Ds18b20CommandType.NO_ACTION);
  }
}
