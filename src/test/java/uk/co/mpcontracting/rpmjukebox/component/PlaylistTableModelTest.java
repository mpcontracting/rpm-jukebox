package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaylistTableModelTest extends AbstractGUITest {

    @Test
    public void shouldInitialise() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");

        PlaylistTableModel playlistTableModel = new PlaylistTableModel(mockPlaylist);

        assertThat(playlistTableModel.getPlaylist()).isEqualTo(mockPlaylist);
        assertThat(playlistTableModel.getSelected()).isNotNull();
        assertThat(playlistTableModel.getName().get()).isEqualTo("Playlist");
    }
}
