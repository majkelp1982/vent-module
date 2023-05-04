package pl.smarthouse.ventmodule.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import pl.smarthouse.sharedobjects.enums.Operation;

@Service
@Setter
@Getter
public class TempHysteresisService {
  private int humidityThresholdLow = 60;
  private int humidityThresholdHigh = 75;
  private double heatingThresholdLow = -2.5;
  private double heatingThresholdHigh = -1.5;
  private double coolingThresholdHigh = 1.5;
  private double coolingThresholdLow = 1.0;
  private double airConditionThresholdHigh = 2.0;
  private double airConditionThresholdLow = 1.0;

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
