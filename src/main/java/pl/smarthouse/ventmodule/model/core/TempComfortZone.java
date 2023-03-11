package pl.smarthouse.ventmodule.model.core;

import lombok.Data;

@Data
public class TempComfortZone {
  boolean enabled;
  private double temperature;
  private int humidity;
  private double requiredTemperature;

  private boolean forcedAirSystemEnabled;

  public TempComfortZone(final boolean enabled) {
    this.enabled = enabled;
  }
}
