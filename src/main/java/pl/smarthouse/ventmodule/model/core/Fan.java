package pl.smarthouse.ventmodule.model.core;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Builder
public class Fan {
  @Min(value = 0)
  @Max(value = 100)
  private int currentSpeed;

  @Min(value = 0)
  @Max(value = 100)
  private int goalSpeed;

  private int revolution;
}
