package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class PlaylistStringConverterTest extends AbstractGUITest {

    @Test
    public void shouldSetPlaylist() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");

        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(mockPlaylist);

        Playlist playlist = (Playlist) getField(playlistStringConverter, "playlist");
        String playlistName = (String) getField(playlistStringConverter, "originalName");

        assertThat(playlist).isEqualTo(mockPlaylist);
        assertThat(playlistName).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetToString() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");

        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();

        assertThat(playlistStringConverter.toString(mockPlaylist)).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetFromString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);

        Playlist result = playlistStringConverter.fromString("Playlist From String");

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist From String");
    }

    @Test
    public void shouldGetFromNullString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);

        Playlist result = playlistStringConverter.fromString(null);

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist");
    }

    @Test
    public void shouldGetFromEmptyString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);

        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);

        Playlist result = playlistStringConverter.fromString(" ");

        assertThat(result.getPlaylistId()).isEqualTo(5);
        assertThat(result.getName()).isEqualTo("Playlist");
    }
}
