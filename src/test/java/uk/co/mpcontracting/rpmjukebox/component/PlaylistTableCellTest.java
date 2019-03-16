package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaylistTableCellTest extends AbstractTest {

    @Test
    public void shouldUpdateItem() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem("Value", false);

        assertThat(playlistTableCell.getText()).isEqualTo("Value");
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem("Value", true);

        assertThat(playlistTableCell.getText()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        PlaylistTableCell<String, String> playlistTableCell = new PlaylistTableCell<>();
        playlistTableCell.updateItem(null, false);

        assertThat(playlistTableCell.getText()).isNull();
    }
}
