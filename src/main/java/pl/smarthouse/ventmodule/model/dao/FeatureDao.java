package pl.smarthouse.ventmodule.model.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FeatureDao {
  private boolean airExchange;
  private boolean humidityAlert;
  private boolean activeCooling;
  private boolean activeHeating;
}
