package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistStringConverterTest extends AbstractTest {

    @Test
    public void shouldSetPlaylist() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");
        
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(mockPlaylist);
        
        Playlist playlist = (Playlist)ReflectionTestUtils.getField(playlistStringConverter, "playlist");
        String playlistName = (String)ReflectionTestUtils.getField(playlistStringConverter, "originalName");
        
        assertThat("Playlist should equal mock playlist", playlist, equalTo(mockPlaylist));
        assertThat("Playlist name should be 'Playlist'", playlistName, equalTo("Playlist"));
    }
    
    @Test
    public void shouldGetToString() {
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.getPlaylistId()).thenReturn(1);
        when(mockPlaylist.getName()).thenReturn("Playlist");
        
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        
        assertThat("To string should be 'Playlist'", playlistStringConverter.toString(mockPlaylist), equalTo("Playlist"));
    }
    
    @Test
    public void shouldGetFromString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);
        
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);
        
        Playlist result = playlistStringConverter.fromString("Playlist From String");
        
        assertThat("Playlist ID should be 5", result.getPlaylistId(), equalTo(5));
        assertThat("Playlist name should be 'Playlist From String'", result.getName(), equalTo("Playlist From String"));
    }
    
    @Test
    public void shouldGetFromNullString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);
        
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);
        
        Playlist result = playlistStringConverter.fromString(null);
        
        assertThat("Playlist ID should be 5", result.getPlaylistId(), equalTo(5));
        assertThat("Playlist name should be 'Playlist'", result.getName(), equalTo("Playlist"));
    }
    
    @Test
    public void shouldGetFromEmptyString() {
        Playlist playlist = new Playlist(5, "Playlist", 10);
        
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(playlist);
        
        Playlist result = playlistStringConverter.fromString(" ");
        
        assertThat("Playlist ID should be 5", result.getPlaylistId(), equalTo(5));
        assertThat("Playlist name should be 'Playlist'", result.getName(), equalTo("Playlist"));
    }
}
