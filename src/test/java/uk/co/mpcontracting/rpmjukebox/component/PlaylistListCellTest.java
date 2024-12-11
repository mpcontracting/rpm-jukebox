package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PlaylistListCellTest extends AbstractGuiTest {

  private PlaylistListCell underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new PlaylistListCell(new PlaylistStringConverter<>()));
  }

  @Test
  void shouldUpdateItem() {
    String playlistName = createPlaylistName();

    underTest.updateItem(new Playlist(1, playlistName, 10), false);

    assertThat(underTest.getText()).isEqualTo(playlistName);
    assertThat(underTest.isEditable()).isTrue();
  }

  @Test
  void shouldUpdateItemNotEditableWhenReservedPlaylist() {
    String playlistName = createPlaylistName();

    underTest.updateItem(new Playlist(PLAYLIST_ID_FAVOURITES, playlistName, 10), false);

    assertThat(underTest.getText()).isEqualTo(playlistName);
    assertThat(underTest.isEditable()).isFalse();
  }

  @Test
  void shouldUpdateItemAutoEditWhenGraphicIsTextField() {
    TextField textField = mock(TextField.class);
    when(underTest.getGraphic()).thenReturn(textField);

    underTest.updateItem(new Playlist(1, createPlaylistName(), 10), false);

    assertThat(underTest.getText()).isNull();
    assertThat(underTest.isEditable()).isTrue();
    verify(textField).selectAll();
  }

  @Test
  void shouldUpdateItemAsEmpty() {
    underTest.updateItem(new Playlist(1, createPlaylistName(), 10), true);

    // These are also set in the super class
    verify(underTest, times(2)).setText(null);
    verify(underTest, times(2)).setGraphic(null);
  }

  @Test
  void shouldStartEditAndCommit() {
    String playlistUpdatedName = createPlaylistName();

    PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
    playlistStringConverter.setPlaylist(new Playlist(1, createPlaylistName(), 10));

    underTest = spy(new PlaylistListCell(playlistStringConverter));
    TextField textField = spy(new TextField(playlistUpdatedName));
    ReflectionTestUtils.invokeMethod(textField, "setFocused", true);
    when(underTest.getGraphic()).thenReturn(textField);

    @SuppressWarnings("unchecked")
    ListView<Playlist> listView = (ListView<Playlist>) mock(ListView.class);
    when(listView.isEditable()).thenReturn(true);
    when(underTest.getListView()).thenReturn(listView);

    underTest.startEdit();

    ReflectionTestUtils.invokeMethod(textField, "setFocused", false);

    ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

    verify(underTest, times(1)).commitEdit(playlistCaptor.capture());
    assertThat(playlistCaptor.getValue().getName()).isEqualTo(playlistUpdatedName);
  }

  @Test
  void shouldStartEditNoCommit() {
    TextField textField = spy(new TextField(createPlaylistName()));
    when(underTest.getGraphic()).thenReturn(textField);

    @SuppressWarnings("unchecked")
    ListView<Playlist> listView = (ListView<Playlist>) mock(ListView.class);
    when(listView.isEditable()).thenReturn(true);
    when(underTest.getListView()).thenReturn(listView);

    underTest.startEdit();

    ReflectionTestUtils.invokeMethod(textField, "setFocused", true);

    verify(underTest, never()).commitEdit(any());
  }

  @Test
  void shouldStartEditNoTextField() {
    @SuppressWarnings("unchecked")
    ListView<Playlist> listView = (ListView<Playlist>) mock(ListView.class);
    when(listView.isEditable()).thenReturn(true);
    when(underTest.getListView()).thenReturn(listView);

    underTest.startEdit();

    verify(underTest, never()).commitEdit(any());
  }
}