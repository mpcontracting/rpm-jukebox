package uk.co.mpcontracting.rpmjukebox.util;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.util.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringHelperTest {

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