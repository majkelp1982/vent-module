package pl.smarthouse.ventmodule.utils;

import java.time.LocalTime;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import pl.smarthouse.sharedobjects.dto.comfort.core.TimeRange;

@UtilityClass
public class TimeRangeUtils {
  public boolean inTimeRange(final TimeRange timeRange) {
    if (Objects.isNull(timeRange)) {
      return false;
    }
    final LocalTime currentTime = LocalTime.now();
    return currentTime.isAfter(timeRange.getFrom()) && currentTime.isBefore(timeRange.getTo());
  }
}
