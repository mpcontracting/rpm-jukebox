package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class PlaylistStringConverterTest extends AbstractGUITest {

    private PlaylistStringConverter<Playlist> underTest;

    @Before
    public void setup() {
        underTest = new PlaylistStringConverter<>();
    }

    @Test
    public void shouldSetPlaylist() {
        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistId()).thenReturn(1);
        when(playlist.getName()).thenReturn("Playlist");

        underTest.setPlaylist(playlist);

        Playlist resultPlaylist = (Playlist) getField(underTest, "playlist");
        String resultName = (String) getField(underTest, "originalName");

        assertThat(resultPlaylist).isEqualTo(playlist);
        assertThat(resultName).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetToString() {
        Playlist playlist = mock(Playlist.class);
        when(playlist.getPlaylistId()).thenReturn(1);
        when(playlist.getName()).thenReturn("Playlist");

        assertThat(underTest.toString(playlist)).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetFromString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        underTest.setPlaylist(playlist);

        Playlist result = underTest.fromString("Playlist From String");

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist From String");
    }

    @Test
    public void shouldGetFromNullString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        underTest.setPlaylist(playlist);

        Playlist result = underTest.fromString(null);

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetFromEmptyString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        underTest.setPlaylist(playlist);

        Playlist result = underTest.fromString(" ");

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist");
    }
}
