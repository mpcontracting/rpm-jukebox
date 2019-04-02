package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getNonNullField;

@RunWith(MockitoJUnitRunner.class)
public class EqualizerTest {

    @Test
    public void shouldGetNumberOfBands() {
        Equalizer equalizer = new Equalizer(10);

        assertThat(equalizer.getNumberOfBands()).isEqualTo(10);
    }

    @Test
    public void shouldSetGain() {
        Equalizer equalizer = new Equalizer(10);
        equalizer.setGain(5, 5);

        double gain = ((double[]) getNonNullField(equalizer, "gain"))[5];

        assertThat(gain).isEqualTo(5d);
    }

    @Test
    public void shouldThrowExceptionWhenSettingBandLessThanZero() {
        Equalizer equalizer = new Equalizer(10);

        assertThatThrownBy(() -> equalizer.setGain(-1, 5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSettingBandGreaterThanMax() {
        Equalizer equalizer = new Equalizer(10);

        assertThatThrownBy(() -> equalizer.setGain(10, 5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldGetGain() {
        Equalizer equalizer = new Equalizer(10);
        double[] gain = new double[10];
        gain[5] = 5d;

        setField(equalizer, "gain", gain);

        double result = equalizer.getGain(5);

        assertThat(result).isEqualTo(5d);
    }

    @Test
    public void shouldThrowExceptionWhenGettingBandLessThanZero() {
        Equalizer equalizer = new Equalizer(10);

        assertThatThrownBy(() -> equalizer.getGain(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenGettingBandGreaterThanMax() {
        Equalizer equalizer = new Equalizer(10);

        assertThatThrownBy(() -> equalizer.getGain(10)).isInstanceOf(IllegalArgumentException.class);
    }
}
