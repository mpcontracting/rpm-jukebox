package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PlaylistListCellTest extends AbstractTest implements Constants {

    @Test
    public void shouldUpdateItem() {
        PlaylistListCell playlistListCell = new PlaylistListCell(new PlaylistStringConverter<>());

        playlistListCell.updateItem(new Playlist(1, "Playlist", 10), false);

        assertThat(playlistListCell.getText()).isEqualTo("Playlist");
        assertThat(playlistListCell.isEditable()).isTrue();
    }

    @Test
    public void shouldUpdateItemNotEditableWhenReservedPlaylist() {
        PlaylistListCell playlistListCell = new PlaylistListCell(new PlaylistStringConverter<>());

        playlistListCell.updateItem(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10), false);

        assertThat(playlistListCell.getText()).isEqualTo("Favourites");
        assertThat(playlistListCell.isEditable()).isFalse();
    }

    @Test
    public void shouldUpdateItemAutoEditWhenGraphicIsTextField() {
        PlaylistListCell spyPlaylistListCell = spy(new PlaylistListCell(new PlaylistStringConverter<>()));
        TextField mockTextField = mock(TextField.class);
        when(spyPlaylistListCell.getGraphic()).thenReturn(mockTextField);

        spyPlaylistListCell.updateItem(new Playlist(1, "Playlist", 10), false);

        assertThat(spyPlaylistListCell.getText()).isNull();
        assertThat(spyPlaylistListCell.isEditable()).isTrue();
        verify(mockTextField, times(1)).selectAll();
    }

    @Test
    public void shouldUpdateItemAsEmpty() {
        PlaylistListCell spyPlaylistListCell = spy(new PlaylistListCell(new PlaylistStringConverter<>()));

        spyPlaylistListCell.updateItem(new Playlist(1, "Playlist", 10), true);

        // These are also set in the super class
        verify(spyPlaylistListCell, times(2)).setText(null);
        verify(spyPlaylistListCell, times(2)).setGraphic(null);
    }

    @Test
    public void shouldStartEditAndCommit() {
        PlaylistStringConverter<Playlist> converter = new PlaylistStringConverter<>();
        converter.setPlaylist(new Playlist(1, "Playlist", 10));

        PlaylistListCell spyPlaylistListCell = spy(new PlaylistListCell(converter));
        TextField spyTextField = spy(new TextField("Playlist Updated"));
        ReflectionTestUtils.invokeMethod(spyTextField, "setFocused", true);
        when(spyPlaylistListCell.getGraphic()).thenReturn(spyTextField);

        @SuppressWarnings("unchecked")
        ListView<Playlist> mockListView = (ListView<Playlist>)mock(ListView.class);
        when(mockListView.isEditable()).thenReturn(true);
        when(spyPlaylistListCell.getListView()).thenReturn(mockListView);

        spyPlaylistListCell.startEdit();

        ReflectionTestUtils.invokeMethod(spyTextField, "setFocused", false);

        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);

        verify(spyPlaylistListCell, times(1)).commitEdit(playlistCaptor.capture());
        assertThat(playlistCaptor.getValue().getName()).isEqualTo("Playlist Updated");
    }

    @Test
    public void shouldStartEditNoCommit() {
        PlaylistListCell spyPlaylistListCell = spy(new PlaylistListCell(new PlaylistStringConverter<>()));
        TextField spyTextField = spy(new TextField("Playlist"));
        when(spyPlaylistListCell.getGraphic()).thenReturn(spyTextField);

        @SuppressWarnings("unchecked")
        ListView<Playlist> mockListView = (ListView<Playlist>)mock(ListView.class);
        when(mockListView.isEditable()).thenReturn(true);
        when(spyPlaylistListCell.getListView()).thenReturn(mockListView);

        spyPlaylistListCell.startEdit();

        ReflectionTestUtils.invokeMethod(spyTextField, "setFocused", true);

        verify(spyPlaylistListCell, never()).commitEdit(any());
    }

    @Test
    public void shouldStartEditNoTextField() {
        PlaylistListCell spyPlaylistListCell = spy(new PlaylistListCell(new PlaylistStringConverter<>()));

        @SuppressWarnings("unchecked")
        ListView<Playlist> mockListView = (ListView<Playlist>)mock(ListView.class);
        when(mockListView.isEditable()).thenReturn(true);
        when(spyPlaylistListCell.getListView()).thenReturn(mockListView);

        spyPlaylistListCell.startEdit();

        verify(spyPlaylistListCell, never()).commitEdit(any());
    }
}
