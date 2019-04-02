package uk.co.mpcontracting.rpmjukebox.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class PlaylistListCellFactoryTest extends AbstractGUITest implements Constants {

    @Mock
    private PlaylistManager mockPlaylistManager;

    private PlaylistListCellFactory cellFactory;

    private ApplicationContext originalContext;
    private MainPanelController mockMainPanelController;

    @Before
    public void setup() {
        cellFactory = new PlaylistListCellFactory();
        setField(cellFactory, "eventManager", getMockEventManager());
        setField(cellFactory, "playlistManager", mockPlaylistManager);

        reset(mockPlaylistManager);

        originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        mockMainPanelController = mock(MainPanelController.class);
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(MainPanelController.class)).thenReturn(mockMainPanelController);

        setField(ContextHelper.class, "applicationContext", mockContext);
    }

    @Test
    public void shouldSinglePrimaryClickOnCell() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setPlaylistId(999);

        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(playlist);

        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, playlist.getPlaylistId());
    }

    @Test
    public void shouldDoublePrimaryClickOnCell() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setPlaylistId(999);

        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(playlist);

        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, playlist.getPlaylistId());
    }

    @Test
    public void shouldSingleSecondaryClickOnCell() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setPlaylistId(999);

        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(playlist);

        listCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));

        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, playlist.getPlaylistId());
    }

    @Test
    public void shouldDoubleSecondaryClickOnCell() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setPlaylistId(999);

        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(playlist);

        listCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 2));

        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, playlist.getPlaylistId());
    }

    @Test
    public void shouldSinglePrimaryClickOnCellItemIsNull() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(null);

        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(getMockEventManager(), never()).fireEvent(any());
    }

    @Test
    public void shouldSingleSecondaryClickOnCellItemIsNull() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(null);

        listCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));

        verify(getMockEventManager(), never()).fireEvent(any());
    }

    @Test
    public void shouldClickNewPlaylistItem() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);

        newPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(mockPlaylistManager, times(1)).createPlaylist();
    }

    @Test
    public void shouldClickDeletePlaylistItem() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = cellFactory.call(listView);
        listView.getSelectionModel().select(5);

        MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

        deletePlaylistItem.onActionProperty().get().handle(new ActionEvent());

        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mockMainPanelController, times(1)).showConfirmView(anyString(), anyBoolean(), okRunnable.capture(),
            any());

        okRunnable.getValue().run();

        verify(mockPlaylistManager, times(1)).deletePlaylist(3);
    }

    @Test
    public void shouldOpenContextMenuOnReservedPlaylist() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = cellFactory.call(listView);

        listCell.updateListView(listView);
        listCell.updateIndex(0);

        listCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(listCell));

        MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
        MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

        assertThat(newPlaylistItem.isDisable()).isTrue();
        assertThat(deletePlaylistItem.isDisable()).isTrue();
    }

    @Test
    public void shouldOpenContextMenuOnUserPlaylist() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = cellFactory.call(listView);

        listCell.updateListView(listView);
        listCell.updateIndex(2);

        listCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(listCell));

        MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
        MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

        assertThat(newPlaylistItem.isDisable()).isTrue();
        assertThat(deletePlaylistItem.isDisable()).isFalse();
    }

    @Test
    public void shouldOpenContextMenuBelowPlaylists() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = cellFactory.call(listView);

        listCell.updateListView(listView);
        listCell.updateIndex(10);

        listCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(listCell));

        MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
        MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

        assertThat(newPlaylistItem.isDisable()).isFalse();
        assertThat(deletePlaylistItem.isDisable()).isTrue();
    }

    @Test
    public void shouldTriggerDragOver() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, times(1)).acceptTransferModes(TransferMode.COPY);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSameSource() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, listCell));

        listCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.COPY);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithNoContent() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.COPY);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragEntered() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isNotEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnterdWithSameSource() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, listCell));

        listCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoContent() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoPlaylist() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(null);
        listCell.setStyle(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithReservedPlaylist() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        listCell.setStyle(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragExited() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setStyle("some-style: style");

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_EXITED, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragExitedProperty().get().handle(spyDragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDropped() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));

        Track mockTrack = mock(Track.class);
        when(mockTrack.clone()).thenReturn(mockTrack);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
        when(mockDragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(mockTrack);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragDroppedProperty().get().handle(spyDragEvent);

        verify(mockPlaylistManager, times(1)).addTrackToPlaylist(1, mockTrack);
        verify(spyDragEvent, times(1)).setDropCompleted(true);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragDroppedWithNoContent() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));

        listCell.onDragDroppedProperty().get().handle(spyDragEvent);

        verify(mockPlaylistManager, never()).addTrackToPlaylist(anyInt(), any());
        verify(spyDragEvent, never()).setDropCompleted(true);
        verify(spyDragEvent, times(1)).consume();
    }

    private ListView<Playlist> getListView() {
        ObservableList<Playlist> playlists = FXCollections.observableArrayList();

        for (int i = -2; i < 8; i++) {
            playlists.add(new Playlist(i, "Playlist " + i, 10));
        }

        return new ListView<>(playlists);
    }

    @After
    public void cleanup() {
        setField(ContextHelper.class, "applicationContext", originalContext);
    }
}
