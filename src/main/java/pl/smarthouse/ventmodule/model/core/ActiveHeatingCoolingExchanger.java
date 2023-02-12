package pl.smarthouse.ventmodule.model.core;

import lombok.Builder;
import lombok.Data;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;

@Data
@Builder
public class ActiveHeatingCoolingExchanger {
  private Ds18b20Result watterIn;
  private Ds18b20Result watterOut;
  private Ds18b20Result airIn;
  private Ds18b20Result airOut;
}
