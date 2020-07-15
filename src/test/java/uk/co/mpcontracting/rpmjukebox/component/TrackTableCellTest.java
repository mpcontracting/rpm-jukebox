package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackTableCellTest extends AbstractGUITest {

    @Test
    public void shouldUpdateItem() {
        TrackTableCell<String> underTest = new TrackTableCell<>();
        underTest.updateItem("Value", false);

        assertThat(underTest.getText()).isEqualTo("Value");
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        TrackTableCell<String> underTest = new TrackTableCell<>();
        underTest.updateItem("Value", true);

        assertThat(underTest.getText()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        TrackTableCell<String> underTest = new TrackTableCell<>();
        underTest.updateItem(null, false);

        assertThat(underTest.getText()).isNull();
    }
}
