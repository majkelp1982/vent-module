package pl.smarthouse.ventmodule.chain;

import static pl.smarthouse.ventmodule.properties.ThrottleProperties.THROTTLES;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;
import pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.ventmodule.model.core.Throttle;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.service.VentModuleService;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ThrottleChain {

  private final VentModuleService ventModuleService;
  private final Pca9685 throttleActor;
  private Throttle throttle;

  public ThrottleChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleService ventModuleService) {
    this.throttleActor =
        (Pca9685) esp32ModuleConfig.getConfiguration().getActorMap().getActor(THROTTLES);
    chainService.addChain(createChain());
    this.ventModuleService = ventModuleService;
  }

  private Chain createChain() {
    final Chain chain = new Chain("Throttles");
    // Wait for throttles state change and drive to goal position
    chain.addStep(waitForThrottlesAndDriveStep());
    // Wait for response and after set NO_ACTION
    chain.addStep(waitForResponseAndSetNoActionStep());
    // Wait 1s and release all servo motors
    chain.addStep(wait1SecondAndReleaseServoMotor());
    // Wait for response and set NO_ACTION
    chain.addStep(waitAndSetNoAction());
    return chain;
  }

  private Step waitForThrottlesAndDriveStep() {
    return Step.builder()
        .conditionDescription("Wait for throttles state change")
        .condition(findThrottleToDrive())
        .stepDescription("Drive throttle to goal position.")
        .action(driveThrottleToGoalPosition())
        .build();
  }

  private Predicate<Step> findThrottleToDrive() {
    return step -> {
      this.throttle = null;
      ventModuleService
          .getAllZones()
          .map(ZoneDao::getThrottle)
          .filter(throttle -> (throttle.getCurrentPosition() != throttle.getGoalPosition()))
          .flatMap(
              throttle -> {
                this.throttle = throttle;
                return Mono.just(throttle);
              })
          .switchIfEmpty(Mono.just(ventModuleService.getIntakeThrottle()))
          .filter(throttle -> (throttle.getCurrentPosition() != throttle.getGoalPosition()))
          .flatMap(
              throttle -> {
                this.throttle = throttle;
                return Mono.just(throttle);
              })
          .subscribe();
      return !Objects.isNull(throttle);
    };
  }

  private Runnable driveThrottleToGoalPosition() {
    return () -> {
      throttleActor.getCommandSet().setCommandType(throttle.getCommandType());
      throttleActor.getCommandSet().setValue(Integer.toString(throttle.getGoalPosition()));
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
    return PredicateUtils.isResponseUpdated(throttleActor).and(isResponseContainStepMotorIndex());
  }

  private Predicate<Step> isResponseContainStepMotorIndex() {
    return step ->
        throttle.getCommandType().toString().contains(throttleActor.getResponse().getChannel())
            && (throttle.getGoalPosition() == throttleActor.getResponse().getMicroseconds());
  }

  private Step wait1SecondAndReleaseServoMotor() {
    return Step.builder()
        .conditionDescription("Wait 2 seconds")
        .condition(PredicateUtils.delaySeconds(1))
        .stepDescription("Write current position and release motor")
        .action(writeCurrentPositionAndReleaseAllServoMotors())
        .build();
  }

  private Runnable writeCurrentPositionAndReleaseAllServoMotors() {
    return () -> {
      throttle.setCurrentPosition(throttle.getGoalPosition());
      throttleActor.getCommandSet().setCommandType(Pca9685CommandType.WRITE_ALL_MICROSECONDS);
      throttleActor.getCommandSet().setValue(Integer.toString(0));
    };
  }

  private Step waitAndSetNoAction() {
    return Step.builder()
        .conditionDescription("Wait for response")
        .condition(
            PredicateUtils.isResponseUpdated(throttleActor).and(isResponseContainCommandAll()))
        .stepDescription("Set NO_ACTION")
        .action(setNoAction())
        .build();
  }

  private Predicate<Step> isResponseContainCommandAll() {
    return step -> "ALL".contains(throttleActor.getResponse().getChannel().toUpperCase());
  }

  private Runnable setNoAction() {
    return () -> throttleActor.getCommandSet().setCommandType(Pca9685CommandType.NO_ACTION);
  }
}
