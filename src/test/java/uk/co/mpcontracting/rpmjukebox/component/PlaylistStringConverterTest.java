package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PlaylistStringConverterTest extends AbstractGuiTest {

  private PlaylistStringConverter<Playlist> underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new PlaylistStringConverter<>();
  }

  @Test
  void shouldSetPlaylist() {
    String playlistName = createPlaylistName();

    Playlist playlist = mock(Playlist.class);
    when(playlist.getName()).thenReturn(playlistName);

    underTest.setPlaylist(playlist);

    Playlist resultPlaylist = (Playlist) getField(underTest, "playlist");
    String resultName = (String) getField(underTest, "originalName");

    assertThat(resultPlaylist).isEqualTo(playlist);
    assertThat(resultName).isEqualTo(playlistName);
  }

  @Test
  void shouldGetToString() {
    String playlistName = createPlaylistName();
    Playlist playlist = mock(Playlist.class);
    when(playlist.getName()).thenReturn(playlistName);

    assertThat(underTest.toString(playlist)).isEqualTo(playlistName);
  }

  @Test
  void shouldGetFromString() {
    String playlistFromStringName = createPlaylistName();
    Playlist playlist = new Playlist(5, createPlaylistName(), 10);

    underTest.setPlaylist(playlist);

    Playlist result = underTest.fromString(playlistFromStringName);

    assertThat(result.getPlaylistId()).isEqualTo(5);
    assertThat(result.getName()).isEqualTo(playlistFromStringName);
  }

  @Test
  void shouldGetFromNullString() {
    String playlistName = createPlaylistName();
    Playlist playlist = new Playlist(5, playlistName, 10);

    underTest.setPlaylist(playlist);

    Playlist result = underTest.fromString(null);

    assertThat(result.getPlaylistId()).isEqualTo(5);
    assertThat(result.getName()).isEqualTo(playlistName);
  }

  @Test
  void shouldGetFromEmptyString() {
    String playlistName = createPlaylistName();
    Playlist playlist = new Playlist(5, playlistName, 10);

    underTest.setPlaylist(playlist);

    Playlist result = underTest.fromString(" ");

    assertThat(result.getPlaylistId()).isEqualTo(5);
    assertThat(result.getName()).isEqualTo(playlistName);
  }
}