package uk.co.mpcontracting.rpmjukebox.support;

import javafx.util.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class StringHelperTest {

    @Test
    public void shouldFormatAnElapsedZeroTime() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(0));

        assertThat(elapsedTime).isEqualTo("00:00/00:00");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithSeconds() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(2000), new Duration(1000));

        assertThat(elapsedTime).isEqualTo("00:01/00:02");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithMinutes() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(120000), new Duration(60000));

        assertThat(elapsedTime).isEqualTo("01:00/02:00");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithHours() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(7200000), new Duration(3600000));

        assertThat(elapsedTime).isEqualTo("1:00:00/2:00:00");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithZeroMediaDurationSeconds() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(1000));

        assertThat(elapsedTime).isEqualTo("00:01");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithZeroMediaDurationMinutes() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(60000));

        assertThat(elapsedTime).isEqualTo("01:00");
    }

    @Test
    public void shouldFormatAnElapsedTimeWithZeroMediaDurationHours() {
        String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(3600000));

        assertThat(elapsedTime).isEqualTo("1:00:00");
    }
}
