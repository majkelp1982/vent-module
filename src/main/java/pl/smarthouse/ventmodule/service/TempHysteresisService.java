package pl.smarthouse.ventmodule.service;

import pl.smarthouse.sharedobjects.enums.Operation;

public class TempHysteresisService {
  private final int humidityThresholdLow;
  private final int humidityThresholdHigh;
  private final double heatingThresholdLow;
  private final double heatingThresholdHigh;
  private final double coolingThresholdHigh;
  private final double coolingThresholdLow;
  private final double airConditionThresholdHigh;
  private final double airConditionThresholdLow;

  public TempHysteresisService(
      final int humidityThresholdLow,
      final int humidityThresholdHigh,
      final double heatingThresholdLow,
      final double heatingThresholdHigh,
      final double coolingThresholdHigh,
      final double coolingThresholdLow,
      final double airConditionThresholdHigh,
      final double airConditionThresholdLow) {
    this.humidityThresholdLow = humidityThresholdLow;
    this.humidityThresholdHigh = humidityThresholdHigh;
    this.heatingThresholdLow = heatingThresholdLow;
    this.heatingThresholdHigh = heatingThresholdHigh;
    this.coolingThresholdHigh = coolingThresholdHigh;
    this.coolingThresholdLow = coolingThresholdLow;
    this.airConditionThresholdHigh = airConditionThresholdHigh;
    this.airConditionThresholdLow = airConditionThresholdLow;
  }

  public Operation update(
      final Operation currentOperation, final int humidity, final double deltaTemp) {
    // deltaTemp >0 ->to hot  <0 ->to cold
    Operation resultOperation = null;
    if (humidity >= humidityThresholdHigh) {
      return Operation.HUMIDITY_ALERT;
    }
    if (Operation.HUMIDITY_ALERT.equals(currentOperation)) {
      if (humidity < humidityThresholdLow) {
        resultOperation = Operation.STANDBY;
      } else {
        return Operation.HUMIDITY_ALERT;
      }
    }

    if (deltaTemp <= heatingThresholdLow) {
      resultOperation = Operation.HEATING;
    }

    if (Operation.HEATING.equals(currentOperation)) {
      if (deltaTemp > heatingThresholdHigh) {
        resultOperation = Operation.STANDBY;
      } else {
        return Operation.HEATING;
      }
    }

    if (deltaTemp >= airConditionThresholdHigh) {
      resultOperation = Operation.AIR_CONDITION;
    }

    if (Operation.AIR_CONDITION.equals(currentOperation)) {
      if (deltaTemp < airConditionThresholdLow) {
        resultOperation = Operation.STANDBY;
      } else {
        return Operation.AIR_CONDITION;
      }
    }

    if ((deltaTemp >= coolingThresholdHigh) && !Operation.AIR_CONDITION.equals(resultOperation)) {
      resultOperation = Operation.COOLING;
    }

    if (Operation.COOLING.equals(currentOperation)
        && !Operation.AIR_CONDITION.equals(resultOperation)) {
      if (deltaTemp < coolingThresholdLow) {
        resultOperation = Operation.STANDBY;
      } else {
        return Operation.COOLING;
      }
    }

    return (resultOperation == null) ? Operation.STANDBY : resultOperation;
  }
}
