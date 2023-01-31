package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.ventmodule.configurations.VentModuleConfiguration;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {
  private static final String LOG_RESET_ZONE =
      "Zone: {}, will be reset due to last update is over: {} minutes ago";
  private final int ZONE_OUTDATED_IN_MINUTES = 2;

  private final VentModuleConfiguration ventModuleConfiguration;

  public Flux<ZoneDao> checkIfZonesOutdated() {
    return Flux.fromIterable(ventModuleConfiguration.getZoneDaoList())
        .doOnNext(
            zoneDao -> {
              if (LocalDateTime.now()
                  .isAfter(zoneDao.getLastUpdate().plusMinutes(ZONE_OUTDATED_IN_MINUTES))) {
                log.info(LOG_RESET_ZONE, zoneDao.getName(), ZONE_OUTDATED_IN_MINUTES);
                resetZone(zoneDao);
              }
            });
  }

  private void resetZone(final ZoneDao zoneDao) {
    zoneDao.getCurrentState().setAirExchange(false);
    zoneDao.getCurrentState().setActiveCooling(false);
    zoneDao.getCurrentState().setActiveHeating(false);
    zoneDao.getCurrentState().setHumidityAlert(false);
    zoneDao.getThrottleDao().setGoalPosition(zoneDao.getThrottleDao().getClosePosition());
    zoneDao.setLastUpdate(LocalDateTime.now());
  }
}
