package uk.co.mpcontracting.rpmjukebox.settings;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistSettingsTest extends AbstractTest {

    @Test
    public void shouldPopulateTracksFromPlaylist() {
        Playlist playlist = new Playlist(1, "Test Playlist", 10);
        for (int i = 0; i < 10; i++) {
            playlist.addTrack(mock(Track.class));
        }
        
        PlaylistSettings playlistSettings = new PlaylistSettings(playlist);
        
        assertThat("Playlist settings should have 10 tracks", playlistSettings.getTracks(), hasSize(10));
    }
}
