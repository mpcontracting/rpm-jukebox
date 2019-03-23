package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackTableCellTest extends AbstractGUITest {

    @Test
    public void shouldUpdateItem() {
        TrackTableCell<String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem("Value", false);

        assertThat(trackTableCell.getText()).isEqualTo("Value");
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        TrackTableCell<String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem("Value", true);

        assertThat(trackTableCell.getText()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        TrackTableCell<String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem(null, false);

        assertThat(trackTableCell.getText()).isNull();
    }
}
