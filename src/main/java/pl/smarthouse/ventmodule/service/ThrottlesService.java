package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.core.Throttle;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThrottlesService {
  private final VentModuleService ventModuleService;

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
              return zoneDao;
            })
        .filter(
            zoneDao ->
                List.of(Operation.COOLING, Operation.HEATING).contains(zoneDao.getOperation()))
        .collectList()
        .flatMap(
            zoneDaoList -> {
              if (zoneDaoList.isEmpty()) {
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
