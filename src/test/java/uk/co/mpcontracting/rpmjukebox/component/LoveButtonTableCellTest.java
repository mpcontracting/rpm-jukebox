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
    private PlaylistManager mockPlaylistManager;

    private LoveButtonTableCell loveButtonTableCell;

    @Before
    public void setup() {
        loveButtonTableCell = new LoveButtonTableCell(mockPlaylistManager);
    }

    @Test
    public void shouldUpdateItemWhenTrackIsInFavourites() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(true);

        loveButtonTableCell.updateItem(trackId, false);

        assertThat(loveButtonTableCell.getId()).isEqualTo(STYLE_LOVE_BUTTON_ON);
    }

    @Test
    public void shouldUpdateItemWhenTrackIsNotInFavourites() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(trackId, false);

        assertThat(loveButtonTableCell.getId()).isEqualTo(STYLE_LOVE_BUTTON_OFF);
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(trackId, true);

        assertThat(loveButtonTableCell.getId()).isNull();
    }

    @Test
    public void shouldUpdateItemAsNull() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(null, false);

        assertThat(loveButtonTableCell.getId()).isNull();
    }
}
