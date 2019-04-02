package uk.co.mpcontracting.rpmjukebox.settings;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistSettingsTest {

    @Test
    public void shouldPopulateTracksFromPlaylist() {
        Playlist playlist = new Playlist(1, "Test Playlist", 10);
        for (int i = 0; i < 11; i++) {
            playlist.addTrack(mock(Track.class));
        }

        PlaylistSettings playlistSettings = new PlaylistSettings(playlist);

        assertThat(playlistSettings.getTracks()).hasSize(10);
    }
}
