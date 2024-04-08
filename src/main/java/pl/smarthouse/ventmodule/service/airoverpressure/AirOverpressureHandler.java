package pl.smarthouse.ventmodule.service.airoverpressure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.core.enums.State;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.service.VentModuleParamsService;
import pl.smarthouse.ventmodule.service.VentModuleService;
import pl.smarthouse.ventmodule.service.ZoneService;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class AirOverpressureHandler {
  private final ZoneService zoneService;
  private final VentModuleParamsService ventModuleParamsService;
  private final VentModuleService ventModuleService;
  private final AirOverpressureStateCalculator airOverpressureStateCalculator;

  @Scheduled(initialDelay = 10000, fixedDelay = 10000)
  private void handle() {
    airOverpressureStateCalculator.calculate();

    if (State.ON.equals(ventModuleService.getVentModuleDao().getFireplaceAirOverpressureActive())) {
      zoneService
          .setZoneOperation(
              ZoneName.SALON,
              Operation.AIR_EXCHANGE,
              ventModuleParamsService.getParams().getFireplaceAirOverpressureLevel())
          .subscribe();
    }
  }
}
