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
        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistId()).thenReturn(1);
        when(playlist.getName()).thenReturn("Playlist");

        PlaylistTableModel underTest = new PlaylistTableModel(playlist);

        assertThat(underTest.getPlaylist()).isEqualTo(playlist);
        assertThat(underTest.getSelected()).isNotNull();
        assertThat(underTest.getName().get()).isEqualTo("Playlist");
    }
}
