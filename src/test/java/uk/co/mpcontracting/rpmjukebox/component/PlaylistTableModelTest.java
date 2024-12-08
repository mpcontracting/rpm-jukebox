package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;

import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PlaylistTableModelTest extends AbstractGuiTest {

  @Test
  void shouldInitialise() {
    String playlistName = createPlaylistName();

    Playlist playlist = mock(Playlist.class);
    when(playlist.getName()).thenReturn(playlistName);

    PlaylistTableModel underTest = new PlaylistTableModel(playlist);

    assertThat(underTest.getPlaylist()).isEqualTo(playlist);
    assertThat(underTest.getSelected()).isNotNull();
    assertThat(underTest.getName().get()).isEqualTo(playlistName);
  }
}