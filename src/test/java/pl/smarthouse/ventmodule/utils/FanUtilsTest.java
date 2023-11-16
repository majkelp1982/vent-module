package pl.smarthouse.ventmodule.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;

class FanUtilsTest {

  @Test
  void validateRequiredGoalPowerTest1() {
    // settings:
    // VentModuleParamsDto: enabled, maxPower:50, timeRange: 21:30-07:00
    // goalPower: 80
    // given:
    // currentTime: 10:01
    // when: validateRequiredGoalPower
    // then: calculated power 80
    final LocalTime currentTime = LocalTime.of(10, 1, 0);
    final LocalTime from = LocalTime.of(21, 30);
    final LocalTime to = LocalTime.of(7, 0);

    try (MockedStatic<LocalTime> localTimeMock = Mockito.mockStatic(LocalTime.class)) {
      localTimeMock.when(() -> LocalTime.now()).thenReturn(currentTime);
      localTimeMock.when(() -> LocalTime.of(21, 30)).thenReturn(from);
      localTimeMock.when(() -> LocalTime.of(7, 0)).thenReturn(to);
      Assertions.assertEquals(currentTime, LocalTime.now());

      final int calculatedPower =
          FanUtils.validateRequiredGoalPower(mockVentModuleParamsDto(true, 50, 50), 80, 50);
      Assertions.assertEquals(80, calculatedPower);
    }
  }

  @Test
  void validateRequiredGoalPowerTest2() {
    // settings:
    // VentModuleParamsDto: enabled, maxPower:50, timeRange: 21:30-07:00
    // goalPower: 80
    // given:
    // currentTime: 21:30
    // when: validateRequiredGoalPower
    // then: calculated power 50
    final LocalTime currentTime = LocalTime.of(21, 30, 0);
    final LocalTime from = LocalTime.of(21, 30);
    final LocalTime to = LocalTime.of(7, 0);

    try (MockedStatic<LocalTime> localTimeMock = Mockito.mockStatic(LocalTime.class)) {
      localTimeMock.when(() -> LocalTime.now()).thenReturn(currentTime);
      localTimeMock.when(() -> LocalTime.of(21, 30)).thenReturn(from);
      localTimeMock.when(() -> LocalTime.of(7, 0)).thenReturn(to);
      Assertions.assertEquals(currentTime, LocalTime.now());

      final int calculatedPower =
          FanUtils.validateRequiredGoalPower(mockVentModuleParamsDto(true, 50, 50), 80, 50);
      Assertions.assertEquals(50, calculatedPower);
    }
  }

  @Test
  void validateRequiredGoalPowerTest3() {
    // settings:
    // VentModuleParamsDto: enabled, maxPower:50, timeRange: 21:30-07:00
    // goalPower: 80
    // given:
    // currentTime: 07:01
    // when: validateRequiredGoalPower
    // then: calculated power 80
    final LocalTime currentTime = LocalTime.of(7, 1, 0);
    final LocalTime from = LocalTime.of(21, 30);
    final LocalTime to = LocalTime.of(7, 0);

    try (MockedStatic<LocalTime> localTimeMock = Mockito.mockStatic(LocalTime.class)) {
      localTimeMock.when(() -> LocalTime.now()).thenReturn(currentTime);
      localTimeMock.when(() -> LocalTime.of(21, 30)).thenReturn(from);
      localTimeMock.when(() -> LocalTime.of(7, 0)).thenReturn(to);
      Assertions.assertEquals(currentTime, LocalTime.now());

      final int calculatedPower =
          FanUtils.validateRequiredGoalPower(mockVentModuleParamsDto(true, 50, 50), 80, 50);
      Assertions.assertEquals(80, calculatedPower);
    }
  }

  private VentModuleParamsDto mockVentModuleParamsDto(
      final boolean nightHoursEnabled, final int inletMaxPower, final int outletMaxPower) {
    final VentModuleParamsDto ventModuleParamsDto = new VentModuleParamsDto();
    ventModuleParamsDto.setNightHoursEnabled(nightHoursEnabled);
    ventModuleParamsDto.setInletFanNightHoursMaxPower(inletMaxPower);
    ventModuleParamsDto.setOutletFanNightHoursMaxPower(outletMaxPower);
    ventModuleParamsDto.setNightHours(
        TimeRange.builder().from(LocalTime.of(21, 30)).to(LocalTime.of(7, 0)).build());
    return ventModuleParamsDto;
  }
}
