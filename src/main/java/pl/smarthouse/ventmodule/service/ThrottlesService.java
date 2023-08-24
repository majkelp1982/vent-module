package pl.smarthouse.ventmodule.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.IntakeThrottleMode;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.core.Throttle;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThrottlesService {
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;

  public Mono<Throttle> setThrottles() {
    return ventModuleService
        .getAllZones()
        .map(
            zoneDao -> {
              final Throttle throttle = zoneDao.getThrottle();
              if (Operation.STANDBY.equals(zoneDao.getOperation())) {
                throttle.setGoalPosition(throttle.getClosePosition());
              } else {
                throttle.setGoalPosition(throttle.getOpenPosition());
              }
              return zoneDao.getOperation();
            })
        .collectList()
        .flatMap(this::calculateIntakeThrottlePosition);
  }

  private Mono<Throttle> calculateIntakeThrottlePosition(final List<Operation> operations) {
    return ventModuleParamsService
        .getParams()
        .flatMap(
            ventModuleParamsDto -> {
              final IntakeThrottleMode intakeThrottleMode =
                  ventModuleParamsDto.getIntakeThrottleMode();
              if (IntakeThrottleMode.FORCED_INSIDE.equals(intakeThrottleMode)) {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getClosePosition());
                          return Mono.just(throttle);
                        });
              }

              if (IntakeThrottleMode.FORCED_OUTSIDE.equals(intakeThrottleMode)) {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getOpenPosition());
                          return Mono.just(throttle);
                        });
              }

              if (operations.contains(Operation.AIR_HEATING)) {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getClosePosition());
                          return Mono.just(throttle);
                        });
              } else {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getOpenPosition());
                          return Mono.just(throttle);
                        });
              }
            });
  }
}
