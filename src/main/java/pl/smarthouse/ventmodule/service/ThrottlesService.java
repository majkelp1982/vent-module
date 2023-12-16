package pl.smarthouse.ventmodule.service;

import static pl.smarthouse.sharedobjects.enums.Operation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.IntakeThrottleMode;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.State;
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
              final List<Operation> requireOpenPositionOperations =
                  List.of(AIR_EXCHANGE, AIR_HEATING, AIR_COOLING, AIR_CONDITION);
              if (requireOpenPositionOperations.contains(zoneDao.getOperation())) {
                throttle.setGoalPosition(throttle.getOpenPosition());
              } else {
                throttle.setGoalPosition(throttle.getClosePosition());
              }
              return zoneDao.getOperation();
            })
        .collectList()
        .flatMap(this::calculateIntakeThrottlePosition);
  }

  private Mono<Throttle> calculateIntakeThrottlePosition(final List<Operation> operations) {
    return Mono.just(ventModuleParamsService.getParams())
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

              if (State.ON.equals(
                      ventModuleService.getVentModuleDao().getFireplaceAirOverpressureActive())
                  || IntakeThrottleMode.FORCED_OUTSIDE.equals(intakeThrottleMode)) {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getOpenPosition());
                          return Mono.just(throttle);
                        });
              }
              final List externalIntakeAllowedOperations =
                  List.of(AIR_EXCHANGE, STANDBY, FLOOR_HEATING, HUMIDITY_ALERT);
              if (externalIntakeAllowedOperations.containsAll(operations)
                  && operations.contains(AIR_EXCHANGE)) {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getOpenPosition());
                          return Mono.just(throttle);
                        });
              } else {
                return ventModuleService
                    .getIntakeThrottle()
                    .flatMap(
                        throttle -> {
                          throttle.setGoalPosition(throttle.getClosePosition());
                          return Mono.just(throttle);
                        });
              }
            });
  }
}
