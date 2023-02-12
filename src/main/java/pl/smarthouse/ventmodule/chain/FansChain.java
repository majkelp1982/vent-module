package pl.smarthouse.ventmodule.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.model.actors.type.pwm.PwmCommandType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.model.core.Fan;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static pl.smarthouse.ventmodule.properties.FanProperties.FAN_INLET;
import static pl.smarthouse.ventmodule.properties.FanProperties.FAN_OUTLET;

@Service
@Slf4j
public class FansChain {

  private final VentModuleService ventModuleService;
  private final Pwm inletFanActor;
  private final Pwm outletFanActor;
  private Fan inletFan;
  private Fan outletFan;

  public FansChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleService ventModuleService) {
    this.inletFanActor =
        (Pwm) esp32ModuleConfig.getConfiguration().getActorMap().getActor(FAN_INLET);
    this.outletFanActor =
        (Pwm) esp32ModuleConfig.getConfiguration().getActorMap().getActor(FAN_OUTLET);
    chainService.addChain(createChain());
    this.ventModuleService = ventModuleService;
  }

  private Chain createChain() {
    final Chain chain = new Chain("Fans");
    // Wait for fans state change and set to goal power
    chain.addStep(waitForFanStateChangeAndSetGoalPower());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseAndSetNoActionStep());
    return chain;
  }

  private Step waitForFanStateChangeAndSetGoalPower() {
    return Step.builder()
        .conditionDescription("Wait for fans state change")
        .condition(checkIfFansGoalPowerIfDifferentThenCurrent())
        .stepDescription("Set goal power to inlet and outlet fans")
        .action(setGoalPower())
        .build();
  }

  private Predicate<Step> checkIfFansGoalPowerIfDifferentThenCurrent() {
    return step -> {
      final AtomicInteger numberOfDiscrepancies = new AtomicInteger(0);
      ventModuleService
          .getFans()
          .flatMap(
              fans -> {
                inletFan = fans.getInlet();
                outletFan = fans.getOutlet();
                if (inletFan.getGoalSpeed() != inletFan.getCurrentSpeed()) {
                  numberOfDiscrepancies.addAndGet(1);
                }
                if (outletFan.getGoalSpeed() != outletFan.getCurrentSpeed()) {
                  numberOfDiscrepancies.addAndGet(1);
                }
                return Mono.just(fans);
              })
          .block();
      return numberOfDiscrepancies.get() != 0;
    };
  }

  private Runnable setGoalPower() {
    return () -> {
      inletFanActor.getCommandSet().setCommandType(PwmCommandType.DUTY_CYCLE);
      inletFanActor
          .getCommandSet()
          .setValue(String.valueOf(calculateDutyCycle(inletFan.getGoalSpeed())));
      outletFanActor.getCommandSet().setCommandType(PwmCommandType.DUTY_CYCLE);
      outletFanActor
          .getCommandSet()
          .setValue(String.valueOf(calculateDutyCycle(outletFan.getGoalSpeed())));
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
    return PredicateUtils.isResponseUpdated(inletFanActor)
        .and(PredicateUtils.isResponseUpdated(outletFanActor));
  }

  private Runnable setNoAction() {
    return () -> {
      inletFan.setCurrentSpeed(inletFan.getGoalSpeed());
      outletFan.setCurrentSpeed(outletFan.getGoalSpeed());
      inletFanActor.getCommandSet().setCommandType(PwmCommandType.NO_ACTION);
      outletFanActor.getCommandSet().setCommandType(PwmCommandType.NO_ACTION);
    };
  }

  private int calculateDutyCycle(final int power) {
    // Power parameter is from 0 to 100
    // return value is from 0 to 255
    return ((int) (255 * (power / 100.00)));
  }
}
