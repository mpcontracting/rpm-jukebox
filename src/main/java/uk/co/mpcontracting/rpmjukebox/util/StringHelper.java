package uk.co.mpcontracting.rpmjukebox.util;

import javafx.util.Duration;
import lombok.AllArgsConstructor;

public final class StringHelper {

  private StringHelper() {}

  public static String formatElapsedTime(Duration mediaDuration, Duration currentTime) {
    HoursMinutesSeconds elapsed = getHoursMinutesSeconds(currentTime);

    if (mediaDuration.equals(Duration.ZERO) && currentTime.equals(Duration.ZERO)) {
      return "00:00/00:00";
    } else if (mediaDuration.greaterThan(Duration.ZERO)) {
      HoursMinutesSeconds duration = getHoursMinutesSeconds(mediaDuration);

      if (duration.hours > 0) {
        return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsed.hours, elapsed.minutes, elapsed.seconds,
            duration.hours, duration.minutes, duration.seconds);
      } else {
        return String.format("%02d:%02d/%02d:%02d", elapsed.minutes, elapsed.seconds, duration.minutes,
            duration.seconds);
      }
    } else {
      if (elapsed.hours > 0) {
        return String.format("%d:%02d:%02d", elapsed.hours, elapsed.minutes, elapsed.seconds);
      } else {
        return String.format("%02d:%02d", elapsed.minutes, elapsed.seconds);
      }
    }
  }

  private static HoursMinutesSeconds getHoursMinutesSeconds(Duration duration) {
    int intDuration = (int) Math.floor(duration.toSeconds());
    int hours = intDuration / (60 * 60);

    if (hours > 0) {
      intDuration -= hours * 60 * 60;
    }

    int minutes = intDuration / 60;
    int seconds = intDuration - minutes * 60;

    return new HoursMinutesSeconds(hours, minutes, seconds);
  }

  @AllArgsConstructor
  private static class HoursMinutesSeconds {
    private final int hours;
    private final int minutes;
    private final int seconds;
  }
}
