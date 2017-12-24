package uk.co.mpcontracting.rpmjukebox.support;

import javafx.util.Duration;

public abstract class StringHelper {

    private StringHelper() {
    }

    public static String formatElapsedTime(Duration mediaDuration, Duration currentTime) {
        int intElapsed = (int)Math.floor(currentTime.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);

        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }

        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedMinutes * 60;

        if (mediaDuration.equals(Duration.ZERO) && currentTime.equals(Duration.ZERO)) {
            return "00:00/00:00";
        } else if (mediaDuration.greaterThan(Duration.ZERO)) {
            int intDuration = (int)Math.floor(mediaDuration.toSeconds());
            int durationHours = intDuration / (60 * 60);

            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }

            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationMinutes * 60;

            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
                    durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
                    durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
            }
        }
    }
}
