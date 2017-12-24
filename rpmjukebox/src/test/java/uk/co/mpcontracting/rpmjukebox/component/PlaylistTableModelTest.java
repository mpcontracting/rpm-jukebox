package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistTableModelTest extends AbstractTest {

    @Test
    public void shouldInitialise() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");

        PlaylistTableModel playlistTableModel = new PlaylistTableModel(mockPlaylist);

        assertThat("Playlist should equal mock playlist", playlistTableModel.getPlaylist(), equalTo(mockPlaylist));
        assertThat("Selected should not be null", playlistTableModel.getSelected(), notNullValue());
        assertThat("Name should be 'Playlist'", playlistTableModel.getName().get(), equalTo("Playlist"));
    }
}
