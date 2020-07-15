package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class SliderProgressBarTest extends AbstractGUITest {

    @Test
    public void shouldGetSliderValueProperty() {
        SliderProgressBar underTest = new SliderProgressBar();

        assertThat(underTest.sliderValueProperty()).isNotNull();
    }

    @Test
    public void shouldGetSliderValueChangingProperty() {
        SliderProgressBar underTest = new SliderProgressBar();

        assertThat(underTest.sliderValueChangingProperty()).isNotNull();
    }

    @Test
    public void shouldGetIsSliderValueChanging() {
        SliderProgressBar underTest = new SliderProgressBar();

        assertThat(underTest.isSliderValueChanging()).isFalse();
    }

    @Test
    public void shouldGetSliderValue() {
        SliderProgressBar underTest = new SliderProgressBar();

        assertThat(underTest.getSliderValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetSliderValue() {
        SliderProgressBar underTest = new SliderProgressBar();
        underTest.setSliderValue(0.5);

        assertThat(underTest.getSliderValue()).isEqualTo(0.5d);
    }

    @Test
    public void shouldGetProgressValue() {
        SliderProgressBar underTest = new SliderProgressBar();

        assertThat(underTest.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetProgressValue() {
        SliderProgressBar underTest = new SliderProgressBar();
        underTest.setProgressValue(50);

        assertThat(underTest.getProgressValue()).isEqualTo(0.5d);
    }
}
