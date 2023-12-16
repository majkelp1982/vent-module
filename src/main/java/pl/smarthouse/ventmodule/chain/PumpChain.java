package pl.smarthouse.ventmodule.chain;

import static pl.smarthouse.ventmodule.properties.PumpProperties.PUMP;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.State;
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
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.service.VentModuleService;

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
    chain.addStep(waitForRequestStateChangeOr1MinuteThanSet());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseThanSetNoActionStep());
    return chain;
  }

  private Step waitForRequestStateChangeOr1MinuteThanSet() {
    return Step.builder()
        .conditionDescription("Wait for pump request change or one minute delay")
        .condition(checkIfRequestStateChangeOr1MinuteDelay())
        .stepDescription("Set pump")
        .action(setPump())
        .build();
  }

  private Predicate<Step> checkIfRequestStateChangeOr1MinuteDelay() {
    return step -> {
      final AtomicBoolean result = new AtomicBoolean(false);
      goalState = PinState.HIGH;
      ventModuleService
          .getAllZones()
          .map(ZoneDao::getOperation)
          .filter(
              operation ->
                  (Operation.AIR_HEATING.equals(operation)
                      || Operation.AIR_COOLING.equals(operation)
                      || Operation.AIR_CONDITION.equals(operation)))
          .collectList()
          .map(
              operationsList -> {
                if ((Objects.isNull(pump.getResponse())
                        || (PinState.HIGH.equals(pump.getResponse().getPinState()))
                        || PredicateUtils.delaySeconds(60).test(step))
                    && !operationsList.isEmpty()) {
                  result.set(true);
                  goalState = PinState.LOW;
                }
                if ((Objects.isNull(pump.getResponse())
                        || PinState.LOW.equals(pump.getResponse().getPinState())
                        || PredicateUtils.delaySeconds(60).test(step))
                    && operationsList.isEmpty()) {
                  result.set(true);
                  goalState = PinState.HIGH;
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

  private Step waitForResponseThanSetNoActionStep() {
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
      ventModuleService
          .getVentModuleDao()
          .setCircuitPump(
              (pump.getResponse().getPinState() == PinState.HIGH) ? State.OFF : State.ON);
      pump.getCommandSet().setCommandType(PwmCommandType.NO_ACTION);
    };
  }
}
