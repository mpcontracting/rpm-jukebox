package uk.co.mpcontracting.rpmjukebox.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class EqualizerTest extends AbstractTest {

    @Test
    public void shouldGetNumberOfBands() {
        Equalizer equalizer = new Equalizer(10);

        assertThat("Equalizer should have 10 bands", equalizer.getNumberOfBands(), equalTo(10));
    }

    @Test
    public void shouldSetGain() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.setGain(5, 5);

        double gain = ((double[])ReflectionTestUtils.getField(equalizer, "gain"))[5];

        assertThat("Gain should be 5", gain, equalTo(5d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingBandLessThanZero() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.setGain(-1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingBandGreaterThanMax() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.setGain(10, 5);
    }

    @Test
    public void shouldGetGain() {
        Equalizer equalizer = new Equalizer(10);
        double[] gain = new double[10];
        gain[5] = 5d;

        ReflectionTestUtils.setField(equalizer, "gain", gain);

        double result = equalizer.getGain(5);

        assertThat("Gain should be 5", result, equalTo(5d));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingBandLessThanZero() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.getGain(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGettingBandGreaterThanMax() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.getGain(10);
    }
}
