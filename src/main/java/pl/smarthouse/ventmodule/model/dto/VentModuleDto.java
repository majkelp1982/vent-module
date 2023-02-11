package pl.smarthouse.ventmodule.model.dto;

import lombok.Builder;
import lombok.Data;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.core.Fans;

import java.util.HashMap;

@Data
@Builder
public class VentModuleDto {
  private final HashMap<ZoneName, ZoneDto> zoneDaoHashMap;
  private final Fans fans;
}
