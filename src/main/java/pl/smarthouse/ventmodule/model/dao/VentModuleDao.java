package pl.smarthouse.ventmodule.model.dao;

import java.util.HashMap;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.enums.State;
import pl.smarthouse.ventmodule.model.core.AirExchanger;
import pl.smarthouse.ventmodule.model.core.Fans;
import pl.smarthouse.ventmodule.model.core.ForcedAirSystemExchanger;
import pl.smarthouse.ventmodule.model.core.Throttle;

@Data
@SuperBuilder
public class VentModuleDao extends ModuleDao {
  @Transient private final HashMap<ZoneName, ZoneDao> zoneDaoHashMap;
  private final Fans fans;
  private final Throttle intakeThrottle;
  private final AirExchanger airExchanger;
  private final ForcedAirSystemExchanger forcedAirSystemExchanger;
  private State circuitPump;
  private State airCondition;
}
