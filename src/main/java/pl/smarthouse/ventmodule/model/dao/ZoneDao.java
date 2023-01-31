package pl.smarthouse.ventmodule.model.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.smarthouse.ventmodule.enums.FunctionType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ZoneDao {
  @NonNull private String name;
  @NonNull private LocalDateTime lastUpdate;
  @NonNull private FunctionType functionType;
  private FeatureDao currentState;
  @NonNull private ThrottleDao throttleDao;
}
