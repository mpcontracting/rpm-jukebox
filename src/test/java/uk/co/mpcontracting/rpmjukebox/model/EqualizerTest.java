package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getNonNullField;

@RunWith(MockitoJUnitRunner.class)
public class EqualizerTest {

    private Equalizer underTest;

    @Before
    public void setup() {
        underTest = new Equalizer(10);
    }

    @Test
    public void shouldGetNumberOfBands() {
        assertThat(underTest.getNumberOfBands()).isEqualTo(10);
    }

    @Test
    public void shouldSetGain() {
        underTest.setGain(5, 5);

        double gain = ((double[]) getNonNullField(underTest, "gain"))[5];

        assertThat(gain).isEqualTo(5d);
    }

    @Test
    public void shouldThrowExceptionWhenSettingBandLessThanZero() {
        assertThatThrownBy(() -> underTest.setGain(-1, 5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSettingBandGreaterThanMax() {
        assertThatThrownBy(() -> underTest.setGain(10, 5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldGetGain() {
        double[] gain = new double[10];
        gain[5] = 5d;

        setField(underTest, "gain", gain);

        double result = underTest.getGain(5);

        assertThat(result).isEqualTo(5d);
    }

    @Test
    public void shouldThrowExceptionWhenGettingBandLessThanZero() {
        assertThatThrownBy(() -> underTest.getGain(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenGettingBandGreaterThanMax() {
        assertThatThrownBy(() -> underTest.getGain(10)).isInstanceOf(IllegalArgumentException.class);
    }
}
