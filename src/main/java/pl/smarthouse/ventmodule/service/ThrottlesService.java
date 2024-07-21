package pl.smarthouse.ventmodule.service;

import static pl.smarthouse.sharedobjects.enums.Operation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.IntakeThrottleMode;
import pl.smarthouse.sharedobjects.dto.weather.WeatherModuleDto;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.core.Throttle;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThrottlesService {
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;
  private final WeatherModuleService weatherModuleService;

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
        .map(this::calculateIntakeThrottlePosition);
  }

  private Throttle calculateIntakeThrottlePosition(final List<Operation> operations) {
    final VentModuleParamsDto ventModuleParamsDto = ventModuleParamsService.getParams();
    final Throttle throttle = ventModuleService.getIntakeThrottle();
    if (ventModuleParamsDto == null) {
      return throttle;
    }

    // Check position base on manual or auto mode
    final IntakeThrottleMode intakeThrottleMode = ventModuleParamsDto.getIntakeThrottleMode();
    if (IntakeThrottleMode.FORCED_INSIDE.equals(intakeThrottleMode)) {
      throttle.setGoalPosition(throttle.getClosePosition());
      return throttle;
    }
    if (State.ON.equals(ventModuleService.getVentModuleDao().getFireplaceAirOverpressureActive())
        || IntakeThrottleMode.FORCED_OUTSIDE.equals(intakeThrottleMode)) {

      throttle.setGoalPosition(throttle.getOpenPosition());
      return throttle;
    }

    // Check if zones contain not allowed operation for external intake
    final List externalIntakeNotAllowedOperations = List.of(FLOOR_HEATING, AIR_HEATING);
    if (operations.stream().anyMatch(externalIntakeNotAllowedOperations::contains)) {
      throttle.setGoalPosition(throttle.getClosePosition());
      return throttle;
    }

    determinateThrottlePositionByExternalTemperature(throttle, ventModuleParamsDto);
    return throttle;
  }

  private void determinateThrottlePositionByExternalTemperature(
      final Throttle throttle, final VentModuleParamsDto ventModuleParamsDto) {
    final WeatherModuleDto weatherMetadata = weatherModuleService.getWeatherMetadata();
    if (weatherMetadata.getBme280Response() == null
        || weatherMetadata.getBme280Response().isError()) {
      throttle.setGoalPosition(throttle.getClosePosition());
    }

    final double outsideTemperature = weatherMetadata.getBme280Response().getTemperature();
    if (outsideTemperature <= ventModuleParamsDto.getOutsideIntakeThreshold()) {
      throttle.setGoalPosition(throttle.getOpenPosition());
    }

    if (outsideTemperature > (ventModuleParamsDto.getOutsideIntakeThreshold() + 0.5d)) {
      throttle.setGoalPosition(throttle.getClosePosition());
    }
  }
}
