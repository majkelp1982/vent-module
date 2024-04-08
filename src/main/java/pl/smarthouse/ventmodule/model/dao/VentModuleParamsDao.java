package pl.smarthouse.ventmodule.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.IntakeThrottleMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentModuleParamsDao {
  private boolean fireplaceAirOverpressureEnabled;
  private int fireplaceAirOverpressureLevel;
  private boolean humidityAlertEnabled;
  private boolean airExchangeEnabled;
  private boolean airHeatingEnabled;
  private boolean airCoolingEnabled;
  private boolean airConditionEnabled;
  private boolean nightHoursEnabled;
  private IntakeThrottleMode intakeThrottleMode;
  private double outsideIntakeThreshold;

  private TimeRange nightHours;
  private int inletFanNightHoursMaxPower;
  private int outletFanNightHoursMaxPower;
}
