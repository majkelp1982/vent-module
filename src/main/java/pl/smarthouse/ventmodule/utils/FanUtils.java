package pl.smarthouse.ventmodule.utils;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.core.TimeRange;
import pl.smarthouse.sharedobjects.dto.ventilation.VentModuleParamsDto;

@UtilityClass
public class FanUtils {
  public int validateRequiredGoalPower(
      final VentModuleParamsDto ventModuleParamsDto,
      final int requiredGoalPower,
      final int maxPower) {
    if (ventModuleParamsDto.isNightHoursEnabled()) {
      final TimeRange timeRange = ventModuleParamsDto.getNightHours();
      if (timeRange.getFrom().isBefore(timeRange.getTo())) {
        if (TimeRangeUtils.inTimeRange(ventModuleParamsDto.getNightHours())) {
          if (requiredGoalPower > maxPower) {
            return maxPower;
          }
        }
      } else {
        final TimeRange timeRangeNegative =
            TimeRange.builder()
                .from(ventModuleParamsDto.getNightHours().getTo())
                .to(ventModuleParamsDto.getNightHours().getFrom())
                .build();

        if (!TimeRangeUtils.inTimeRange(timeRangeNegative)) {
          if (requiredGoalPower > maxPower) {
            return maxPower;
          }
        }
      }
    }
    return requiredGoalPower;
  }

  public void addAndValidateRequiredPower(final AtomicInteger goalPower, final int value) {
    goalPower.addAndGet(value);
    if (goalPower.get() > 100) {
      goalPower.set(100);
    } else if (goalPower.get() < 0) {
      goalPower.set(0);
    }
  }

  public void recalculateFansSpeedWhenAirOverpressureRequested(
      final AtomicInteger inletRequiredGoalPower,
      final AtomicInteger outletRequiredGoalPower,
      final int requestedSpeedDifference) {
    if (inletRequiredGoalPower.get() - outletRequiredGoalPower.get() < requestedSpeedDifference) {
      inletRequiredGoalPower.set(outletRequiredGoalPower.get());
      addAndValidateRequiredPower(inletRequiredGoalPower, requestedSpeedDifference);
      outletRequiredGoalPower.set(inletRequiredGoalPower.get());
      addAndValidateRequiredPower(outletRequiredGoalPower, (requestedSpeedDifference * (-1)));
    }
  }
}
