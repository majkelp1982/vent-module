package pl.smarthouse.ventmodule.model.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ThrottleDao {
  private final int openPosition;
  private final int closePosition;
  private int currentPosition;
  private int goalPosition;
}
