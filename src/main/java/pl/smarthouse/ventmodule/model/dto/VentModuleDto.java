package pl.smarthouse.ventmodule.model.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.enums.State;
import pl.smarthouse.ventmodule.model.core.AirExchanger;
import pl.smarthouse.ventmodule.model.core.Fans;
import pl.smarthouse.ventmodule.model.core.ForcedAirSystemExchanger;
import pl.smarthouse.ventmodule.model.core.Throttle;

@Data
@NoArgsConstructor
@Setter
@Getter
@JsonPropertyOrder({
  "fans",
  "airExchanger",
  "forcedAirSystemExchanger",
  "circuitPump",
  "intakeThrottle",
  "zoneDaoHashMap"
})
public class VentModuleDto {
  private HashMap<ZoneName, ZoneDto> zoneDaoHashMap;
  private Fans fans;
  private Throttle intakeThrottle;
  private AirExchanger airExchanger;
  private ForcedAirSystemExchanger forcedAirSystemExchanger;
  private State circuitPump;
  private State airCondition;
}
