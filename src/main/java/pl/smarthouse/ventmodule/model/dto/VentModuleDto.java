package pl.smarthouse.ventmodule.model.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinResponse;
import pl.smarthouse.ventmodule.model.core.ActiveHeatingCoolingExchanger;
import pl.smarthouse.ventmodule.model.core.AirExchanger;
import pl.smarthouse.ventmodule.model.core.Fans;
import pl.smarthouse.ventmodule.model.core.Throttle;

import java.util.HashMap;

@Data
@NoArgsConstructor
@Setter
@Getter
public class VentModuleDto {
  private HashMap<ZoneName, ZoneDto> zoneDaoHashMap;
  private Fans fans;
  private Throttle intakeThrottle;
  private AirExchanger airExchanger;
  private ActiveHeatingCoolingExchanger activeHeatingCoolingExchanger;
  private PinResponse circuitPump;
}
