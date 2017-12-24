package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackTableCellTest extends AbstractTest {

    @Test
    public void shouldUpdateItem() {
        TrackTableCell<String, String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem("Value", false);

        assertThat("Text should be 'Value'", trackTableCell.getText(), equalTo("Value"));
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        TrackTableCell<String, String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem("Value", true);

        assertThat("Text should be null", trackTableCell.getText(), nullValue());
    }

    @Test
    public void shouldUpdateItemAsNull() {
        TrackTableCell<String, String> trackTableCell = new TrackTableCell<>();
        trackTableCell.updateItem(null, false);

        assertThat("Text should be null", trackTableCell.getText(), nullValue());
    }
}
