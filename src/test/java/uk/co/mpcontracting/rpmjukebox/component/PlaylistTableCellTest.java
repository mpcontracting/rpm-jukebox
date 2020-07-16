package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;

public class PlaylistTableCellTest extends AbstractGUITest {

    private PlaylistTableCell<String> underTest;

    @Before
    public void setup() {
        underTest = new PlaylistTableCell<>();
    }

    @Test
    public void shouldUpdateItem() {
        underTest.updateItem("Value", false);

        assertThat(underTest.getText()).isEqualTo("Value");
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        underTest.updateItem("Value", true);

        assertThat(underTest.getText()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        underTest.updateItem(null, false);

        assertThat(underTest.getText()).isNull();
    }
}
