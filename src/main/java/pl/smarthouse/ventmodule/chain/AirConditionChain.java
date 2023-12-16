package pl.smarthouse.ventmodule.chain;

import static pl.smarthouse.ventmodule.properties.AirConditionProperties.AIR_CONDITION;

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
public class AirConditionChain {

  private final VentModuleService ventModuleService;
  private final Pin airCondition;
  private PinState goalState;

  public AirConditionChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleService ventModuleService) {
    this.airCondition =
        (Pin) esp32ModuleConfig.getConfiguration().getActorMap().getActor(AIR_CONDITION);
    chainService.addChain(createChain());
    this.ventModuleService = ventModuleService;
  }

  private Chain createChain() {
    final Chain chain = new Chain("Air Condition");
    // Wait for air condition request change and set
    chain.addStep(waitForRequestStateChangeOr1MinuteThanSet());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseThanSetNoActionStep());
    return chain;
  }

  private Step waitForRequestStateChangeOr1MinuteThanSet() {
    return Step.builder()
        .conditionDescription("Wait for air condition request change or 1 minute")
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
          .filter(operation -> Operation.AIR_CONDITION.equals(operation))
          .collectList()
          .map(
              operationsList -> {
                if ((Objects.isNull(airCondition.getResponse())
                        || (PinState.HIGH.equals(airCondition.getResponse().getPinState()))
                        || PredicateUtils.delaySeconds(60).test(step))
                    && !operationsList.isEmpty()) {
                  result.set(true);
                  goalState = PinState.LOW;
                }
                if ((Objects.isNull(airCondition.getResponse())
                        || PinState.LOW.equals(airCondition.getResponse().getPinState())
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
      airCondition.getCommandSet().setCommandType(PinCommandType.SET);
      airCondition.getCommandSet().setValue(goalState.toString());
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
    return PredicateUtils.isResponseUpdated(airCondition);
  }

  private Runnable setNoAction() {
    return () -> {
      ventModuleService
          .getVentModuleDao()
          .setAirCondition(
              (airCondition.getResponse().getPinState() == PinState.HIGH) ? State.OFF : State.ON);
      airCondition.getCommandSet().setCommandType(PwmCommandType.NO_ACTION);
    };
  }
}
