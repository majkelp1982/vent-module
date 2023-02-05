package pl.smarthouse.ventmodule.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pin.Pin;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinCommandType;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinState;
import pl.smarthouse.smartmodule.model.actors.type.pwm.PwmCommandType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.service.VentModuleService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig.PUMP;

@Service
@Slf4j
public class PumpChain {

  private final VentModuleService ventModuleService;
  private final Pin pump;
  private PinState goalState;

  public PumpChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleService ventModuleService) {
    this.pump = (Pin) esp32ModuleConfig.getConfiguration().getActorMap().getActor(PUMP);
    chainService.addChain(createChain());
    this.ventModuleService = ventModuleService;
  }

  private Chain createChain() {
    final Chain chain = new Chain("Circuit pump");
    // Wait for pump request change and set
    chain.addStep(waitForRequestStateChangeAndSet());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseAndSetNoActionStep());
    return chain;
  }

  private Step waitForRequestStateChangeAndSet() {
    return Step.builder()
        .conditionDescription("Wait for pump request change")
        .condition(checkIfRequestStateChange())
        .stepDescription("Set pump")
        .action(setPump())
        .build();
  }

  private Predicate<Step> checkIfRequestStateChange() {
    return step -> {
      final AtomicBoolean result = new AtomicBoolean(false);
      goalState = PinState.LOW;
      ventModuleService
          .getAllZones()
          .map(zoneDao -> zoneDao.getOperation())
          .filter(
              operation ->
                  (Operation.HEATING.equals(operation) || Operation.COOLING.equals(operation)))
          .collectList()
          .map(
              operationsList -> {
                if ((Objects.isNull(pump.getResponse())
                        || (PinState.LOW.equals(pump.getResponse().getPinState())))
                    && !operationsList.isEmpty()) {
                  result.set(true);
                  goalState = PinState.HIGH;
                }
                if ((Objects.isNull(pump.getResponse())
                        || PinState.HIGH.equals(pump.getResponse().getPinState()))
                    && operationsList.isEmpty()) {
                  result.set(true);
                  goalState = PinState.LOW;
                }
                return operationsList;
              })
          .subscribe();

      return result.get();
    };
  }

  private Runnable setPump() {
    return () -> {
      pump.getCommandSet().setCommandType(PinCommandType.SET);
      pump.getCommandSet().setValue(goalState.toString());
    };
  }

  private Step waitForResponseAndSetNoActionStep() {
    return Step.builder()
        .conditionDescription("Wait for response")
        .condition(waitForResponse())
        .stepDescription("Set NO_ACTION")
        .action(setNoAction())
        .build();
  }

  private Predicate<Step> waitForResponse() {
    return PredicateUtils.isResponseUpdated(pump);
  }

  private Runnable setNoAction() {
    return () -> {
      pump.getCommandSet().setCommandType(PwmCommandType.NO_ACTION);
    };
  }
}
