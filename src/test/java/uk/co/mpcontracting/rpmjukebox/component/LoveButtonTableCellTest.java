package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LoveButtonTableCellTest extends AbstractGUITest implements Constants {

    @Mock
    private PlaylistManager playlistManager;

    private LoveButtonTableCell underTest;

    @Before
    public void setup() {
        underTest = new LoveButtonTableCell(playlistManager);
    }

    @Test
    public void shouldUpdateItemWhenTrackIsInFavourites() {
        String trackId = "trackId";
        when(playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(true);

        underTest.updateItem(trackId, false);

        assertThat(underTest.getId()).isEqualTo(STYLE_LOVE_BUTTON_ON);
    }

    @Test
    public void shouldUpdateItemWhenTrackIsNotInFavourites() {
        String trackId = "trackId";
        when(playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        underTest.updateItem(trackId, false);

        assertThat(underTest.getId()).isEqualTo(STYLE_LOVE_BUTTON_OFF);
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        String trackId = "trackId";
        when(playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        underTest.updateItem(trackId, true);

        assertThat(underTest.getId()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        String trackId = "trackId";
        when(playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        underTest.updateItem(null, false);

        assertThat(underTest.getId()).isNull();
    }
}
