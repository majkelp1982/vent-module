package pl.smarthouse.ventmodule.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import pl.smarthouse.sharedobjects.enums.Operation;
import pl.smarthouse.ventmodule.enums.FunctionType;
import pl.smarthouse.ventmodule.model.core.Throttle;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ZoneDto {
  @NonNull private LocalDateTime lastUpdate;
  @NonNull private FunctionType functionType;
  private Operation operation;
  @NonNull private Throttle throttle;
  private int requiredPower;
}
