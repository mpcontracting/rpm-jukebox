package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistTableCellTest extends AbstractTest {

    @Test
    public void shouldUpdateItem() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem("Value", false);

        assertThat("Text should be 'Value'", playlistTableCell.getText(), equalTo("Value"));
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem("Value", true);

        assertThat("Text should be null", playlistTableCell.getText(), nullValue());
    }

    @Test
    public void shouldUpdateItemAsNull() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem(null, false);

        assertThat("Text should be null", playlistTableCell.getText(), nullValue());
    }
}
