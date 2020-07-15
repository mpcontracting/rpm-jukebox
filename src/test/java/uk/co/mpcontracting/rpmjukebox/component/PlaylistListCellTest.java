package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PlaylistListCellTest extends AbstractGUITest implements Constants {

    @Test
    public void shouldUpdateItem() {
        PlaylistListCell underTest = new PlaylistListCell(new PlaylistStringConverter<>());

        underTest.updateItem(new Playlist(1, "Playlist", 10), false);

        assertThat(underTest.getText()).isEqualTo("Playlist");
        assertThat(underTest.isEditable()).isTrue();
    }

    @Test
    public void shouldUpdateItemNotEditableWhenReservedPlaylist() {
        PlaylistListCell underTest = new PlaylistListCell(new PlaylistStringConverter<>());

        underTest.updateItem(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10), false);

        assertThat(underTest.getText()).isEqualTo("Favourites");
        assertThat(underTest.isEditable()).isFalse();
    }

    @Test
    public void shouldUpdateItemAutoEditWhenGraphicIsTextField() {
        PlaylistListCell underTest = spy(new PlaylistListCell(new PlaylistStringConverter<>()));
        TextField textField = mock(TextField.class);
        when(underTest.getGraphic()).thenReturn(textField);

        underTest.updateItem(new Playlist(1, "Playlist", 10), false);

        assertThat(underTest.getText()).isNull();
        assertThat(underTest.isEditable()).isTrue();
        verify(textField, times(1)).selectAll();
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        PlaylistListCell underTest = spy(new PlaylistListCell(new PlaylistStringConverter<>()));

        underTest.updateItem(new Playlist(1, "Playlist", 10), true);

        // These are also set in the super class
        verify(underTest, times(2)).setText(null);
        verify(underTest, times(2)).setGraphic(null);
    }

    @Test
    public void shouldStartEditAndCommit() {
        PlaylistStringConverter<Playlist> playlistStringConverter = new PlaylistStringConverter<>();
        playlistStringConverter.setPlaylist(new Playlist(1, "Playlist", 10));

        PlaylistListCell underTest = spy(new PlaylistListCell(playlistStringConverter));
        TextField textField = spy(new TextField("Playlist Updated"));
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
        assertThat(playlistCaptor.getValue().getName()).isEqualTo("Playlist Updated");
    }

    @Test
    public void shouldStartEditNoCommit() {
        PlaylistListCell underTest = spy(new PlaylistListCell(new PlaylistStringConverter<>()));
        TextField textField = spy(new TextField("Playlist"));
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
    public void shouldStartEditNoTextField() {
        PlaylistListCell underTest = spy(new PlaylistListCell(new PlaylistStringConverter<>()));

        @SuppressWarnings("unchecked")
        ListView<Playlist> listView = (ListView<Playlist>) mock(ListView.class);
        when(listView.isEditable()).thenReturn(true);
        when(underTest.getListView()).thenReturn(listView);

        underTest.startEdit();

        verify(underTest, never()).commitEdit(any());
    }
}
