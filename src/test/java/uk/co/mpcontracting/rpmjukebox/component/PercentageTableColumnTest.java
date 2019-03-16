package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PercentageTableColumnTest extends AbstractTest {

    @Test
    public void shouldGetPercentageWidthProperty() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();

        assertThat(percentageTableColumn.percentageWidthProperty()).isNotNull();
    }

    @Test
    public void shouldGetPercentageWidth() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();

        assertThat(percentageTableColumn.getPercentageWidth()).isEqualTo(1.0d);
    }

    @Test
    public void shouldSetPercentageWidth() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();
        percentageTableColumn.setPercentageWidth(0.5);

        assertThat(percentageTableColumn.getPercentageWidth()).isEqualTo(0.5d);
    }

    @Test
    public void shouldFailToSetPercentageWidthLessThanZero() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();

        assertThatThrownBy(() -> percentageTableColumn.setPercentageWidth(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFailToSetPercentageWidthGreaterThanOne() {
        PercentageTableColumn<String, String> percentageTableColumn = new PercentageTableColumn<>();

        assertThatThrownBy(() -> percentageTableColumn.setPercentageWidth(1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
