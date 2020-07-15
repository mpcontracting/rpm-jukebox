package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PercentageTableColumnTest extends AbstractGUITest {

    @Test
    public void shouldGetPercentageWidthProperty() {
        PercentageTableColumn<String, String> underTest = new PercentageTableColumn<>();

        assertThat(underTest.percentageWidthProperty()).isNotNull();
    }

    @Test
    public void shouldGetPercentageWidth() {
        PercentageTableColumn<String, String> underTest = new PercentageTableColumn<>();

        assertThat(underTest.getPercentageWidth()).isEqualTo(1.0d);
    }

    @Test
    public void shouldSetPercentageWidth() {
        PercentageTableColumn<String, String> underTest = new PercentageTableColumn<>();
        underTest.setPercentageWidth(0.5);

        assertThat(underTest.getPercentageWidth()).isEqualTo(0.5d);
    }

    @Test
    public void shouldFailToSetPercentageWidthLessThanZero() {
        PercentageTableColumn<String, String> underTest = new PercentageTableColumn<>();

        assertThatThrownBy(() -> underTest.setPercentageWidth(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailToSetPercentageWidthGreaterThanOne() {
        PercentageTableColumn<String, String> underTest = new PercentageTableColumn<>();

        assertThatThrownBy(() -> underTest.setPercentageWidth(1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
