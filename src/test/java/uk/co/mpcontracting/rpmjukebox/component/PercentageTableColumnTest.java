package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PercentageTableColumnTest extends AbstractGUITest {

    private PercentageTableColumn<String, String> underTest;

    @Before
    public void setup() {
        underTest = new PercentageTableColumn<>();
    }

    @Test
    public void shouldGetPercentageWidthProperty() {
        assertThat(underTest.percentageWidthProperty()).isNotNull();
    }

    @Test
    public void shouldGetPercentageWidth() {
        assertThat(underTest.getPercentageWidth()).isEqualTo(1.0d);
    }

    @Test
    public void shouldSetPercentageWidth() {
        underTest.setPercentageWidth(0.5);

        assertThat(underTest.getPercentageWidth()).isEqualTo(0.5d);
    }

    @Test
    public void shouldFailToSetPercentageWidthLessThanZero() {
        assertThatThrownBy(() -> underTest.setPercentageWidth(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailToSetPercentageWidthGreaterThanOne() {
        assertThatThrownBy(() -> underTest.setPercentageWidth(1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
