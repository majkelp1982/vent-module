package pl.smarthouse.ventmodule.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WaterRequirementDto {
  boolean requiredColdWater;
  boolean requiredWarmWater;
}
