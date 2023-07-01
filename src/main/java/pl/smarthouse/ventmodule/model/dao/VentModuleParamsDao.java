package pl.smarthouse.ventmodule.model.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentModuleParamsDao {
  private boolean humidityAlertEnabled;
  private boolean airExchangeEnabled;
  private boolean airHeatingEnabled;
  private boolean airCoolingEnabled;
  private boolean airConditionEnabled;
  private boolean nightHoursEnabled;

  private TimeRange nightHours;
  private int inletFanNightHoursMaxSpeed;
  private int outletFanNightHoursMaxSpeed;
}
