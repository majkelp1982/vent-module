package pl.smarthouse.ventmodule.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.smarthouse.sharedobjects.enums.Operation;

class TempHysteresisServiceTest {
  @Test
  void updateTest() {
    final TempHysteresisService tempHysteresisService = new TempHysteresisService();
    final Operation initOperation = Operation.STANDBY;
    Operation currentOperation;
    // HUMIDITY TEST PART I
    currentOperation = tempHysteresisService.update(initOperation, 65, 0);
    assertEquals(Operation.STANDBY, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 75, 0);
    assertEquals(Operation.HUMIDITY_ALERT, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 70, 0);
    assertEquals(Operation.HUMIDITY_ALERT, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 69, 0);
    assertEquals(Operation.STANDBY, currentOperation);

    // HUMIDITY TEST PART II
    currentOperation = tempHysteresisService.update(Operation.AIR_HEATING, 75, -2.5);
    assertEquals(Operation.HUMIDITY_ALERT, currentOperation);
    currentOperation = tempHysteresisService.update(Operation.AIR_COOLING, 75, 1.5);
    assertEquals(Operation.HUMIDITY_ALERT, currentOperation);
    currentOperation = tempHysteresisService.update(Operation.AIR_CONDITION, 75, 3.1);
    assertEquals(Operation.HUMIDITY_ALERT, currentOperation);

    // HEATING TEST
    currentOperation = tempHysteresisService.update(currentOperation, 50, -2.4);
    assertEquals(Operation.STANDBY, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, -2.5);
    assertEquals(Operation.AIR_HEATING, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, -1.5);
    assertEquals(Operation.AIR_HEATING, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, -1.4);
    assertEquals(Operation.STANDBY, currentOperation);

    // COOLING TEST
    currentOperation = tempHysteresisService.update(currentOperation, 50, 1.4);
    assertEquals(Operation.STANDBY, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 1.5);
    assertEquals(Operation.AIR_COOLING, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 1.0);
    assertEquals(Operation.AIR_COOLING, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 0.9);
    assertEquals(Operation.STANDBY, currentOperation);

    // AIR CONDITION + COOLING TEST
    currentOperation = tempHysteresisService.update(currentOperation, 50, 1.9);
    assertEquals(Operation.AIR_COOLING, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 2.0);
    assertEquals(Operation.AIR_CONDITION, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 1.0);
    assertEquals(Operation.AIR_CONDITION, currentOperation);

    currentOperation = tempHysteresisService.update(currentOperation, 50, 0.9);
    assertEquals(Operation.STANDBY, currentOperation);
  }
}
