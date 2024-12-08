package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PlaylistTableCellTest extends AbstractGuiTest {

  private PlaylistTableCell<String> underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new PlaylistTableCell<>();
  }

  @Test
  void shouldUpdateItem() {
    String value = getFaker().lorem().characters(10 ,20);

    underTest.updateItem(value, false);

    assertThat(underTest.getText()).isEqualTo(value);
  }

  @Test
  void shouldUpdateItemAsEmpty() {
    underTest.updateItem(getFaker().lorem().characters(10 ,20), true);

    assertThat(underTest.getText()).isNull();
  }

  @Test
  void shouldUpdateItemAsNull() {
    underTest.updateItem(null, false);

    assertThat(underTest.getText()).isNull();
  }
}