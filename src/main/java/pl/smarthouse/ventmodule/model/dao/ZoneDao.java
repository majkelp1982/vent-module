package pl.smarthouse.ventmodule.model.dao;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.smarthouse.sharedobjects.dto.ventilation.enums.FunctionType;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.model.core.Throttle;

@Getter
@Setter
@Builder
public class ZoneDao {
  @NonNull private LocalDateTime lastUpdate;
  @NonNull private FunctionType functionType;
  private Operation operation;
  @NonNull private Throttle throttle;
  private int requiredPower;

  public void setOperation(final Operation operation) {
    this.operation = operation;
    lastUpdate = LocalDateTime.now();
  }
}
