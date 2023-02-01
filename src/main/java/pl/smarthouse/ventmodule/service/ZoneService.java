package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.configurations.VentModuleConfiguration;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.model.dto.ZoneDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {
  private static final String LOG_RESET_ZONE =
      "Zone: {}, will be reset due to last update is over: {} minutes ago";
  private final int ZONE_OUTDATED_IN_MINUTES = 2;

  private final VentModuleConfiguration ventModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public Flux<Tuple2<ZoneName, ZoneDao>> getAllZones() {
    return Flux.fromIterable(ventModuleConfiguration.getZoneDaoHashMap().keySet())
        .map(
            zoneName ->
                Tuples.of(zoneName, ventModuleConfiguration.getZoneDaoHashMap().get(zoneName)));
  }

  public Mono<ZoneDto> setZoneOperation(final ZoneName zoneName, final Operation operation) {
    return Mono.justOrEmpty(ventModuleConfiguration.getZoneDaoHashMap().get(zoneName))
        .map(
            zoneDao -> {
              zoneDao.setOperation(operation);
              return zoneDao;
            })
        .map(zoneDao -> modelMapper.map(zoneDao, ZoneDto.class));
  }

  public Flux<ZoneDao> checkIfZonesOutdated() {
    return Flux.fromIterable(ventModuleConfiguration.getZoneDaoHashMap().keySet())
        .flatMap(
            zoneName -> {
              if (LocalDateTime.now()
                  .isAfter(
                      ventModuleConfiguration
                          .getZoneDaoHashMap()
                          .get(zoneName)
                          .getLastUpdate()
                          .plusMinutes(ZONE_OUTDATED_IN_MINUTES))) {
                log.info(LOG_RESET_ZONE, zoneName, ZONE_OUTDATED_IN_MINUTES);
                return resetZone(ventModuleConfiguration.getZoneDaoHashMap().get(zoneName));
              }
              return Mono.empty();
            });
  }

  private Mono<ZoneDao> resetZone(final ZoneDao zoneDao) {
    return Mono.just(zoneDao)
        .map(
            zone -> {
              zone.setOperation(Operation.STANDBY);
              zone.getThrottleDao().setGoalPosition(zone.getThrottleDao().getClosePosition());
              zone.setLastUpdate(LocalDateTime.now());
              return zone;
            });
  }
}
