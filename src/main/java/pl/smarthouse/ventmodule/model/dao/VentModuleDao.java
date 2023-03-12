package pl.smarthouse.ventmodule.model.dao;

import java.util.HashMap;
import lombok.Builder;
import lombok.Data;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.enums.State;
import pl.smarthouse.ventmodule.model.core.AirExchanger;
import pl.smarthouse.ventmodule.model.core.Fans;
import pl.smarthouse.ventmodule.model.core.ForcedAirSystemExchanger;
import pl.smarthouse.ventmodule.model.core.Throttle;

@Data
@Builder
public class VentModuleDao {
  private final HashMap<ZoneName, ZoneDao> zoneDaoHashMap;
  private final Fans fans;
  private final Throttle intakeThrottle;
  private final AirExchanger airExchanger;
  private final ForcedAirSystemExchanger forcedAirSystemExchanger;
  private State circuitPump;
  private State airCondition;
}
