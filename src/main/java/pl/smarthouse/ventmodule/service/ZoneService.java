package pl.smarthouse.ventmodule.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.ventilation.ZoneDto;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.exceptions.InvalidZoneOperationException;
import pl.smarthouse.ventmodule.model.dao.ZoneDao;
import pl.smarthouse.ventmodule.utils.ModelMapper;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneService {
  private static final String LOG_RESET_ZONE =
      "Zone: {}, will be reset due to last update is over: {} minutes ago";
  private static final String ERROR_POWER_REQUIRED =
      "Zone name: %s, operation: %s need requiresPower parameter between 10 and 100";
  private static final String ERROR_WRONG_OPERATION =
      "Zone name: %s, operation: %s is not allowed for channel type: %s. Allowed operations: %s";
  private final int ZONE_OUTDATED_IN_MINUTES = 2;

  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;

  public Mono<ZoneDto> setZoneOperation(
      final ZoneName zoneName, final Operation operation, final int requestPower) {
    if (ventModuleParamsService.getParams() == null) {
      return Mono.empty();
    }
    return Mono.just(ventModuleParamsService.getParams())
        .map(ventModuleParamsDto -> recalculateOperation(ventModuleParamsDto, operation))
        .flatMap(
            recalculatedOperation ->
                ventModuleService
                    .getZone(zoneName)
                    .flatMap(
                        zoneDao -> {
                          if (!Operation.STANDBY.equals(recalculatedOperation)) {
                            validateRequest(zoneName, zoneDao, recalculatedOperation, requestPower);
                            zoneDao.setRequiredPower(requestPower);
                          } else {
                            zoneDao.setRequiredPower(0);
                          }
                          if (zoneName.equals(ZoneName.SALON)
                              && isOverrideSalonZoneNeededDueToOverPressureRequested()) {
                            zoneDao.setRequiredPower(
                                ventModuleParamsService
                                    .getParams()
                                    .getFireplaceAirOverpressureLevel());
                            zoneDao.setOperation(Operation.AIR_EXCHANGE);
                          } else {

                            zoneDao.setOperation(recalculatedOperation);
                          }
                          return Mono.just(zoneDao);
                        }))
        .map(ModelMapper::toZoneDto);
  }

  private boolean isOverrideSalonZoneNeededDueToOverPressureRequested() {
    return State.ON.equals(
        ventModuleService.getVentModuleDao().getFireplaceAirOverpressureActive());
  }

  public Mono<HashMap<ZoneName, ZoneDto>> getActiveZones() {
    return ventModuleService
        .getZonesFullData()
        .map(
            zoneNameZoneDaoHashMap -> {
              final HashMap<ZoneName, ZoneDto> resultHashMap = new HashMap<>();
              zoneNameZoneDaoHashMap.forEach(
                  (zoneName, zoneDao) -> {
                    if (!Operation.STANDBY.equals(zoneDao.getOperation())) {
                      resultHashMap.put(zoneName, ModelMapper.toZoneDto(zoneDao));
                    }
                  });
              return resultHashMap;
            });
  }

  public Mono<ZoneDto> checkIfZonesOutdated() {
    return ventModuleService
        .getZonesFullData()
        .flatMap(
            zoneDaoHashMap -> {
              final HashMap<ZoneName, ZoneDao> outdatedZoneDaos = new HashMap<>();
              zoneDaoHashMap.forEach(
                  (zoneName, zoneDao) -> {
                    if (LocalDateTime.now()
                        .isAfter(zoneDao.getLastUpdate().plusMinutes(ZONE_OUTDATED_IN_MINUTES))) {
                      log.warn(LOG_RESET_ZONE, zoneName, ZONE_OUTDATED_IN_MINUTES);
                      outdatedZoneDaos.put(zoneName, zoneDao);
                    }
                  });
              return Mono.just(outdatedZoneDaos);
            })
        .flatMap(
            outdatedZoneDaos ->
                outdatedZoneDaos.values().stream()
                    .map(Mono::just)
                    .findFirst()
                    .orElseGet(Mono::empty))
        .flatMap(zoneDao -> resetZone(zoneDao))
        .map(zoneDao -> ModelMapper.toZoneDto(zoneDao));
  }

  private Operation recalculateOperation(
      final VentModuleParamsDto ventModuleParamsDto, final Operation operation) {
    if (!ventModuleParamsDto.isHumidityAlertEnabled()
        && Operation.HUMIDITY_ALERT.equals(operation)) {
      log.info("Operation overwritten to STANDBY. HUMIDITY_ALERT not enabled");
      return Operation.STANDBY;
    }
    if (!ventModuleParamsDto.isAirExchangeEnabled() && Operation.AIR_EXCHANGE.equals(operation)) {
      log.info("Operation overwritten to STANDBY. AIR_EXCHANGE not enabled");
      return Operation.STANDBY;
    }
    if (!ventModuleParamsDto.isAirHeatingEnabled() && Operation.AIR_HEATING.equals(operation)) {
      log.info("Operation overwritten to STANDBY. AIR_HEATING not enabled");
      return Operation.STANDBY;
    }
    if (!ventModuleParamsDto.isAirCoolingEnabled() && Operation.AIR_COOLING.equals(operation)) {
      log.info("Operation overwritten to STANDBY. AIR_COOLING not enabled");
      return Operation.STANDBY;
    }
    if (!ventModuleParamsDto.isAirConditionEnabled() && Operation.AIR_CONDITION.equals(operation)) {
      log.info("Operation overwritten to STANDBY. AIR_CONDITION not enabled");
      return Operation.STANDBY;
    }
    return operation;
  }

  private void validateRequest(
      final ZoneName zoneName,
      final ZoneDao zoneDao,
      final Operation operation,
      final int requestPower) {
    if (List.of(Operation.STANDBY, Operation.FLOOR_HEATING).contains(operation)) {
      return;
    }
    if ((requestPower < 10) || (requestPower > 100)) {
      throw new InvalidZoneOperationException(
          String.format(ERROR_POWER_REQUIRED, zoneName, operation));
    }
    if (FunctionType.AIR_SUPPLY.equals(zoneDao.getFunctionType())) {
      final List<Operation> allowedOperationList =
          List.of(
              Operation.AIR_COOLING,
              Operation.AIR_HEATING,
              Operation.AIR_CONDITION,
              Operation.AIR_EXCHANGE);
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
    if (FunctionType.AIR_EXTRACT.equals(zoneDao.getFunctionType())) {
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
