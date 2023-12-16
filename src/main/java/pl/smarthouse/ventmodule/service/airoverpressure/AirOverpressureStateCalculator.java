package pl.smarthouse.ventmodule.service.airoverpressure;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.State;
import pl.smarthouse.ventmodule.service.VentModuleParamsService;
import pl.smarthouse.ventmodule.service.VentModuleService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirOverpressureStateCalculator {
  private static final int AIR_OVERPRESSURE_HOLD_TIME_IN_MIN = 2;
  private final VentModuleService ventModuleService;
  private final VentModuleParamsService ventModuleParamsService;
  LocalDateTime lastTriggerTimestamp;

  public void forceOverpressure() {
    lastTriggerTimestamp = LocalDateTime.now();
    calculate();
  }

  public void calculate() {
    if (isOverpressureEnabled() && !isTriggerTimeout()) {
      ventModuleService.getVentModuleDao().setFireplaceAirOverpressureActive(State.ON);
    } else {
      ventModuleService.getVentModuleDao().setFireplaceAirOverpressureActive(State.OFF);
    }
  }

  private boolean isTriggerTimeout() {
    return lastTriggerTimestamp == null
        || LocalDateTime.now()
            .isAfter(lastTriggerTimestamp.plusMinutes(AIR_OVERPRESSURE_HOLD_TIME_IN_MIN));
  }

  private boolean isOverpressureEnabled() {
    return ventModuleParamsService.getParams().isFireplaceAirOverpressureEnabled();
  }
}
