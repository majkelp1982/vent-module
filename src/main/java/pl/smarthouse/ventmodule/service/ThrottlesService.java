package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.dao.ThrottleDao;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThrottlesService {
  private final ZoneService zoneService;

  public Flux<ZoneDao> setThrottles() {
    return zoneService
        .getAllZones()
        .map(
            tuple2 -> {
              final ZoneDao zoneDao = tuple2.getT2();
              final ThrottleDao throttleDao = zoneDao.getThrottleDao();
              if (Operation.STANDBY.equals(zoneDao.getOperation())) {
                throttleDao.setGoalPosition(throttleDao.getClosePosition());
              } else {
                throttleDao.setGoalPosition(throttleDao.getOpenPosition());
              }
              return zoneDao;
            });
  }
}
