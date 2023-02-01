package pl.smarthouse.ventmodule.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import pl.smarthouse.sharedobjects.enums.Operation;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ZoneDto {
  @NonNull private LocalDateTime lastUpdate;
  private Operation operation;
}
