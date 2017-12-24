package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class LoveButtonTableCellTest extends AbstractTest implements Constants {

    @Mock
    private PlaylistManager mockPlaylistManager;

    private LoveButtonTableCell<String, String> loveButtonTableCell;

    @Before
    public void setup() {
        loveButtonTableCell = new LoveButtonTableCell<>(mockPlaylistManager);
    }

    @Test
    public void shouldUpdateItemWhenTrackIsInFavourites() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(true);

        loveButtonTableCell.updateItem(trackId, false);

        assertThat("ID should be '" + STYLE_LOVE_BUTTON_ON + "'", loveButtonTableCell.getId(),
            equalTo(STYLE_LOVE_BUTTON_ON));
    }

    @Test
    public void shouldUpdateItemWhenTrackIsNotInFavourites() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(trackId, false);

        assertThat("ID should be '" + STYLE_LOVE_BUTTON_OFF + "'", loveButtonTableCell.getId(),
            equalTo(STYLE_LOVE_BUTTON_OFF));
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(trackId, true);

        assertThat("ID should be null", loveButtonTableCell.getId(), nullValue());
    }

    @Test
    public void shouldUpdateItemAsNull() {
        String trackId = "trackId";
        when(mockPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, trackId)).thenReturn(false);

        loveButtonTableCell.updateItem(null, false);

        assertThat("ID should be null", loveButtonTableCell.getId(), nullValue());
    }
}
