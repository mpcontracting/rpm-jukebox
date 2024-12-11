package uk.co.mpcontracting.rpmjukebox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EqualizerTest {

  private int numberOfBands;
  private Equalizer underTest;

  @BeforeEach
  void beforeEach() {
    numberOfBands = getFaker().number().numberBetween(10, 20);
    underTest = new Equalizer(numberOfBands);
  }

  @Test
  void shouldGetNumberOfBands() {
    assertThat(underTest.getNumberOfBands()).isEqualTo(numberOfBands);
  }

  @Test
  void shouldSetGain() {
    int band = getFaker().number().numberBetween(0, numberOfBands - 1);
    double value = getFaker().number().numberBetween(1, 20);
    underTest.setGain(band, value);

    double gain = ((double[]) getNonNullField(underTest, "gain", Object.class))[band];

    assertThat(gain).isEqualTo(value);
  }

  @Test
  void shouldThrowExceptionWhenSettingBandLessThanZero() {
    assertThatThrownBy(() -> underTest.setGain(-1, 5)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenSettingBandGreaterThanMax() {
    assertThatThrownBy(() -> underTest.setGain(numberOfBands, 5)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldGetGain() {
    int band = getFaker().number().numberBetween(0, numberOfBands - 1);
    double value = getFaker().number().numberBetween(1, 20);
    double[] gain = new double[numberOfBands];
    gain[band] = value;

    setField(underTest, "gain", gain);

    double result = underTest.getGain(band);

    assertThat(result).isEqualTo(value);
  }

  @Test
  void shouldThrowExceptionWhenGettingBandLessThanZero() {
    assertThatThrownBy(() -> underTest.getGain(-1)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenGettingBandGreaterThanMax() {
    assertThatThrownBy(() -> underTest.getGain(numberOfBands)).isInstanceOf(IllegalArgumentException.class);
  }
}