package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class SliderProgressBarTest extends AbstractGuiTest {

  private SliderProgressBar underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new SliderProgressBar();
  }

  @Test
  void shouldGetSliderValueProperty() {
    assertThat(underTest.sliderValueProperty()).isNotNull();
  }

  @Test
  void shouldGetSliderValueChangingProperty() {
    assertThat(underTest.sliderValueChangingProperty()).isNotNull();
  }

  @Test
  void shouldGetIsSliderValueChanging() {
    assertThat(underTest.isSliderValueChanging()).isFalse();
  }

  @Test
  void shouldGetSliderValue() {
    assertThat(underTest.getSliderValue()).isEqualTo(0.0d);
  }

  @Test
  void shouldSetSliderValue() {
    double sliderValue = getFaker().number().randomDouble(1, 0, 1);

    underTest.setSliderValue(sliderValue);

    assertThat(underTest.getSliderValue()).isEqualTo(sliderValue);
  }

  @Test
  void shouldGetProgressValue() {
    assertThat(underTest.getProgressValue()).isEqualTo(0.0d);
  }

  @Test
  void shouldSetProgressValue() {
    double progressValue = getFaker().number().randomDouble(0, 10, 100);

    underTest.setProgressValue(progressValue);

    assertThat(underTest.getProgressValue()).isEqualTo(progressValue / 100);
  }
}