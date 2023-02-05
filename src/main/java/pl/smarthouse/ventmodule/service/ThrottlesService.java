package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.core.Throttle;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThrottlesService {
  private final VentModuleService ventModuleService;

  public Flux<ZoneDao> setThrottles() {
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
            });
  }
}
