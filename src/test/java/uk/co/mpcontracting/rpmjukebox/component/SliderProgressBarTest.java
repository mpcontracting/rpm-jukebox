package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class SliderProgressBarTest extends AbstractGUITest {

    private SliderProgressBar underTest;

    @Before
    public void setup() {
        underTest = new SliderProgressBar();
    }

    @Test
    public void shouldGetSliderValueProperty() {
        assertThat(underTest.sliderValueProperty()).isNotNull();
    }

    @Test
    public void shouldGetSliderValueChangingProperty() {
        assertThat(underTest.sliderValueChangingProperty()).isNotNull();
    }

    @Test
    public void shouldGetIsSliderValueChanging() {
        assertThat(underTest.isSliderValueChanging()).isFalse();
    }

    @Test
    public void shouldGetSliderValue() {
        assertThat(underTest.getSliderValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetSliderValue() {
        underTest.setSliderValue(0.5);

        assertThat(underTest.getSliderValue()).isEqualTo(0.5d);
    }

    @Test
    public void shouldGetProgressValue() {
        assertThat(underTest.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetProgressValue() {
        underTest.setProgressValue(50);

        assertThat(underTest.getProgressValue()).isEqualTo(0.5d);
    }
}
