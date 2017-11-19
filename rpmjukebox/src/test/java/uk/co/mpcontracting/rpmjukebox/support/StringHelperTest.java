package uk.co.mpcontracting.rpmjukebox.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import javafx.util.Duration;
import uk.co.mpcontracting.rpmjukebox.AbstractTest;

public class StringHelperTest extends AbstractTest {

	@Test
	public void shouldFormatAnElapsedZeroTime() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(0));

		assertThat("Duration should be '00:00/00:00'", elapsedTime, equalTo("00:00/00:00"));
	}

	@Test
	public void shouldFormatAnElapsedTimeWithSeconds() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(2000), new Duration(1000));

		assertThat("Duration should be '00:01/00:02'", elapsedTime, equalTo("00:01/00:02"));
	}
	
	@Test
	public void shouldFormatAnElapsedTimeWithMinutes() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(120000), new Duration(60000));

		assertThat("Duration should be '01:00/02:00'", elapsedTime, equalTo("01:00/02:00"));
	}
	
	@Test
	public void shouldFormatAnElapsedTimeWithHours() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(7200000), new Duration(3600000));

		assertThat("Duration should be '1:00:00/2:00:00'", elapsedTime, equalTo("1:00:00/2:00:00"));
	}
	
	@Test
	public void shouldFormatAnElapsedTimeWithZeroMediaDurationSeconds() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(1000));

		assertThat("Duration should be '00:01'", elapsedTime, equalTo("00:01"));
	}
	
	@Test
	public void shouldFormatAnElapsedTimeWithZeroMediaDurationMinutes() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(60000));

		assertThat("Duration should be '01:00'", elapsedTime, equalTo("01:00"));
	}
	
	@Test
	public void shouldFormatAnElapsedTimeWithZeroMediaDurationHours() {
		String elapsedTime = StringHelper.formatElapsedTime(new Duration(0), new Duration(3600000));

		assertThat("Duration should be '1:00:00'", elapsedTime, equalTo("1:00:00"));
	}
}
