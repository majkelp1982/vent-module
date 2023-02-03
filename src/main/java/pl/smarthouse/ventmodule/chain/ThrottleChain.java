package pl.smarthouse.ventmodule.chain;

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
import pl.smarthouse.ventmodule.configurations.VentModuleConfiguration;
import pl.smarthouse.ventmodule.model.dao.ThrottleDao;

import java.util.Optional;
import java.util.function.Predicate;

import static pl.smarthouse.ventmodule.configurations.Esp32ModuleConfig.THROTTLES;

@Service
@Slf4j
public class ThrottleChain {

  private final VentModuleConfiguration ventModuleConfiguration;
  private final Pca9685 throttleActor;
  private ThrottleDao throttleDao;

  public ThrottleChain(
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig,
      @Autowired final VentModuleConfiguration ventModuleConfiguration) {
    this.throttleActor =
        (Pca9685) esp32ModuleConfig.getConfiguration().getActorMap().getActor(THROTTLES);
    this.ventModuleConfiguration = ventModuleConfiguration;
    chainService.addChain(createChain());
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
      final Optional<ThrottleDao> throttleDao =
          ventModuleConfiguration.getZoneDaoHashMap().keySet().stream()
              .map(
                  zoneName ->
                      ventModuleConfiguration.getZoneDaoHashMap().get(zoneName).getThrottleDao())
              .filter(throttle -> (throttle.getCurrentPosition() != throttle.getGoalPosition()))
              .findFirst();
      if (throttleDao.isPresent()) {
        this.throttleDao = throttleDao.get();
        return true;
      } else {
        this.throttleDao = null;
        return false;
      }
    };
  }

  private Runnable driveThrottleToGoalPosition() {
    return () -> {
      throttleActor.getCommandSet().setCommandType(throttleDao.getCommandType());
      throttleActor.getCommandSet().setValue(Integer.toString(throttleDao.getGoalPosition()));
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
        throttleDao.getCommandType().toString().contains(throttleActor.getResponse().getChannel())
            && (throttleDao.getGoalPosition() == throttleActor.getResponse().getMicroseconds());
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
      throttleDao.setCurrentPosition(throttleDao.getGoalPosition());
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
    return () -> {
      throttleActor.getCommandSet().setCommandType(Pca9685CommandType.NO_ACTION);
    };
  }
}
