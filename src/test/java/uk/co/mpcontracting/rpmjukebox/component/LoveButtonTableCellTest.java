package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrackId;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_LOVE_BUTTON_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.STYLE_LOVE_BUTTON_ON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class LoveButtonTableCellTest extends AbstractGuiTest {

  @Mock
  private PlaylistService playlistService;

  private LoveButtonTableCell underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new LoveButtonTableCell(playlistService);
  }

  @Test
  void shouldUpdateItemWhenTrackIsInFavourites() {
    String trackId = createTrackId();
    when(playlistService.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(true);

    underTest.updateItem(trackId, false);

    assertThat(underTest.getId()).isEqualTo(STYLE_LOVE_BUTTON_ON);
  }

  @Test
  void shouldUpdateItemWhenTrackIsNotInFavourites() {
    String trackId = createTrackId();
    when(playlistService.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

    underTest.updateItem(trackId, false);

    assertThat(underTest.getId()).isEqualTo(STYLE_LOVE_BUTTON_OFF);
  }

  @Test
  void shouldUpdateItemAsEmpty() {
    String trackId = createTrackId();

    underTest.updateItem(trackId, true);

    assertThat(underTest.getId()).isNull();
  }

  @Test
  void shouldUpdateItemAsNull() {
    underTest.updateItem(null, false);

    assertThat(underTest.getId()).isNull();
  }
}