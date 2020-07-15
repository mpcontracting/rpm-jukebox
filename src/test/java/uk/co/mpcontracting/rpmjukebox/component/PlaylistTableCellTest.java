package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaylistTableCellTest extends AbstractGUITest {

    @Test
    public void shouldUpdateItem() {
        PlaylistTableCell<String> underTest = new PlaylistTableCell<>();
        underTest.updateItem("Value", false);

        assertThat(underTest.getText()).isEqualTo("Value");
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        PlaylistTableCell<String> underTest = new PlaylistTableCell<>();
        underTest.updateItem("Value", true);

        assertThat(underTest.getText()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        PlaylistTableCell<String> underTest = new PlaylistTableCell<>();
        underTest.updateItem(null, false);

        assertThat(underTest.getText()).isNull();
    }
}
