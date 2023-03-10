package pl.smarthouse.ventmodule.model.core;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;

@Getter
@Setter
@Builder
public class Throttle {
  private final int openPosition;
  private final int closePosition;
  private int currentPosition;
  private int goalPosition;
  @NonNull private Pca9685CommandType commandType;
}
