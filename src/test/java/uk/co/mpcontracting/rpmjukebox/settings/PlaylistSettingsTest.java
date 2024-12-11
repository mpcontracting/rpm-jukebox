package uk.co.mpcontracting.rpmjukebox.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

@ExtendWith(MockitoExtension.class)
class PlaylistSettingsTest {

  @Test
  public void shouldPopulateTracksFromPlaylist() {
    Playlist playlist = createPlaylist().orElseThrow();

    PlaylistSettings playlistSettings = new PlaylistSettings(playlist);

    assertThat(playlistSettings.getTracks()).hasSize(playlist.size());
  }
}