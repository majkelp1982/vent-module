package pl.smarthouse.ventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.enums.FunctionType;
import pl.smarthouse.ventmodule.exceptions.InvalidZoneOperationException;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.model.dto.ZoneDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {
  private static final String LOG_RESET_ZONE =
      "Zone: {}, will be reset due to last update is over: {} minutes ago";
  private static final String ERROR_POWER_REQUIRED =
      "Zone name: %s, operation: %s need requiresPower parameter between 0 and 100";
  private static final String ERROR_WRONG_OPERATION =
      "Zone name: %s, operation: %s is not allowed for channel type: %s. Allowed operations: %s";
  private final int ZONE_OUTDATED_IN_MINUTES = 2;

  private final VentModuleService ventModuleService;
  private final ModelMapper modelMapper = new ModelMapper();

  public Mono<ZoneDto> setZoneOperation(
      final ZoneName zoneName, final Operation operation, final int requestPower) {
    return ventModuleService
        .getZone(zoneName)
        .flatMap(
            zoneDao -> {
              if (!Operation.STANDBY.equals(operation)) {
                validateRequest(zoneName, zoneDao, operation, requestPower);
                zoneDao.setRequiredPower(requestPower);
              } else {
                zoneDao.setRequiredPower(0);
              }
              zoneDao.setOperation(operation);
              return Mono.just(zoneDao);
            })
        .map(zoneDao -> modelMapper.map(zoneDao, ZoneDto.class));
  }

  public Flux<ZoneDao> checkIfZonesOutdated() {
    return ventModuleService
        .getAllZonesWithZoneNames()
        .flatMap(
            tuple -> {
              final ZoneName zoneName = tuple.getT1();
              final ZoneDao zoneDao = tuple.getT2();
              if (LocalDateTime.now()
                  .isAfter(zoneDao.getLastUpdate().plusMinutes(ZONE_OUTDATED_IN_MINUTES))) {
                log.warn(LOG_RESET_ZONE, zoneName, ZONE_OUTDATED_IN_MINUTES);
                return resetZone(zoneDao);
              }
              return Mono.empty();
            });
  }

  private void validateRequest(
      final ZoneName zoneName,
      final ZoneDao zoneDao,
      final Operation operation,
      final int requestPower) {
    if (!Operation.STANDBY.equals(operation) && requestPower == 0) {
      throw new InvalidZoneOperationException(
          String.format(ERROR_POWER_REQUIRED, zoneName, operation));
    }
    if ((requestPower < 0) || (requestPower > 100)) {
      throw new InvalidZoneOperationException(
          String.format(ERROR_POWER_REQUIRED, zoneName, operation));
    }
    if (FunctionType.OUTLET.equals(zoneDao.getFunctionType())) {
      final List<Operation> allowedOperationList =
          List.of(Operation.COOLING, Operation.HEATING, Operation.AIR_EXCHANGE);
      if (!allowedOperationList.contains(operation)) {
        throw new InvalidZoneOperationException(
            String.format(
                ERROR_WRONG_OPERATION,
                zoneName,
                operation,
                zoneDao.getFunctionType(),
                allowedOperationList));
      }
    }
    if (FunctionType.INLET.equals(zoneDao.getFunctionType())) {
      final List<Operation> allowedOperationList =
          List.of(Operation.HUMIDITY_ALERT, Operation.AIR_EXCHANGE);
      if (!allowedOperationList.contains(operation)) {
        throw new InvalidZoneOperationException(
            String.format(
                ERROR_WRONG_OPERATION,
                zoneName,
                operation,
                zoneDao.getFunctionType(),
                allowedOperationList));
      }
    }
  }

  private Mono<ZoneDao> resetZone(final ZoneDao zoneDao) {
    return Mono.just(zoneDao)
        .map(
            zone -> {
              zone.setOperation(Operation.STANDBY);
              zone.setRequiredPower(0);
              zone.setLastUpdate(LocalDateTime.now());
              return zone;
            });
  }
}
