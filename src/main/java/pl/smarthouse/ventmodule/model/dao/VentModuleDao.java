package pl.smarthouse.ventmodule.model.dao;

import lombok.Builder;
import lombok.Data;
import pl.smarthouse.sharedobjects.enums.ZoneName;
import pl.smarthouse.ventmodule.model.core.Fans;

import java.util.HashMap;

@Data
@Builder
public class VentModuleDao {
  private final HashMap<ZoneName, ZoneDao> zoneDaoHashMap;
  private final Fans fans;
}
