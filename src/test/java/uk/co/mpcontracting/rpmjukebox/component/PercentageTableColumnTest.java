package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PercentageTableColumnTest extends AbstractGuiTest {

  private PercentageTableColumn<String, String> underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new PercentageTableColumn<>();
  }

  @Test
  void shouldGetPercentageWidthProperty() {
    assertThat(underTest.percentageWidthProperty()).isNotNull();
  }

  @Test
  void shouldGetPercentageWidth() {
    assertThat(underTest.getPercentageWidth()).isEqualTo(1.0d);
  }

  @Test
  void shouldSetPercentageWidth() {
    double width = getFaker().number().randomDouble(1, 0, 1);
    underTest.setPercentageWidth(width);

    assertThat(underTest.getPercentageWidth()).isEqualTo(width);
  }

  @Test
  void shouldFailToSetPercentageWidthLessThanZero() {
    assertThatThrownBy(() -> underTest.setPercentageWidth(-0.1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldFailToSetPercentageWidthGreaterThanOne() {
    assertThatThrownBy(() -> underTest.setPercentageWidth(1.1))
        .isInstanceOf(IllegalArgumentException.class);
  }
}