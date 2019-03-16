package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SliderProgressBarTest extends AbstractTest {

    @Test
    public void shouldGetSliderValueProperty() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();

        assertThat(sliderProgressBar.sliderValueProperty()).isNotNull();
    }

    @Test
    public void shouldGetSliderValueChangingProperty() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();

        assertThat(sliderProgressBar.sliderValueChangingProperty()).isNotNull();
    }

    @Test
    public void shouldGetIsSliderValueChanging() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();

        assertThat(sliderProgressBar.isSliderValueChanging()).isFalse();
    }

    @Test
    public void shouldGetSliderValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();

        assertThat(sliderProgressBar.getSliderValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetSliderValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        sliderProgressBar.setSliderValue(0.5);

        assertThat(sliderProgressBar.getSliderValue()).isEqualTo(0.5d);
    }

    @Test
    public void shouldGetProgressValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();

        assertThat(sliderProgressBar.getProgressValue()).isEqualTo(0.0d);
    }

    @Test
    public void shouldSetProgressValue() {
        SliderProgressBar sliderProgressBar = new SliderProgressBar();
        sliderProgressBar.setProgressValue(50);

        assertThat(sliderProgressBar.getProgressValue()).isEqualTo(0.5d);
    }
}
