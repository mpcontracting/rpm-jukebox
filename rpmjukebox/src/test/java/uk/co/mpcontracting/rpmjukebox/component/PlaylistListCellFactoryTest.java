package uk.co.mpcontracting.rpmjukebox.component;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistListCellFactoryTest extends AbstractTest implements Constants {

    @Mock
    private PlaylistManager mockPlaylistManager;

    private PlaylistListCellFactory cellFactory;
    
    private ApplicationContext originalContext;
    private MainPanelController mockMainPanelController;
    
    @Before
    public void setup() {
        cellFactory = new PlaylistListCellFactory();
        ReflectionTestUtils.setField(cellFactory, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(cellFactory, "playlistManager", mockPlaylistManager);
        
        reset(mockPlaylistManager);
        
        originalContext = (ApplicationContext)ReflectionTestUtils.getField(ContextHelper.class, "applicationContext");
        mockMainPanelController = mock(MainPanelController.class);
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(MainPanelController.class)).thenReturn(mockMainPanelController);
        
        ReflectionTestUtils.setField(ContextHelper.class, "applicationContext", mockContext);
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
        
        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));
        
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, playlist.getPlaylistId());
    }
    
    @Test
    public void shouldDoubleSecondaryClickOnCell() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        playlist.setPlaylistId(999);
        
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setItem(playlist);
        
        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 2));
        
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
        
        listCell.onMouseClickedProperty().get().handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));
        
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
        
        verify(mockMainPanelController, times(1)).showConfirmView(anyString(), anyBoolean(), okRunnable.capture(), any());
        
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
        
        assertThat("New playlist item should be disabled", newPlaylistItem.isDisable(), equalTo(true));
        assertThat("Delete playlist item should be disabled", deletePlaylistItem.isDisable(), equalTo(true));
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
        
        assertThat("New playlist item should be disabled", newPlaylistItem.isDisable(), equalTo(true));
        assertThat("Delete playlist item should not be disabled", deletePlaylistItem.isDisable(), equalTo(false));
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
        
        assertThat("New playlist item should not be disabled", newPlaylistItem.isDisable(), equalTo(false));
        assertThat("Delete playlist item should be disabled", deletePlaylistItem.isDisable(), equalTo(true));
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

        assertThat("List cell style should not be empty", listCell.getStyle(), not(isEmptyString()));
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

        assertThat("List cell style should be empty", listCell.getStyle(), isEmptyString());
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

        assertThat("List cell style should be empty", listCell.getStyle(), isEmptyString());
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

        assertThat("List cell style should be empty", listCell.getStyle(), isEmptyString());
        verify(spyDragEvent, times(1)).consume();
    }
    
    @Test
    public void shouldTriggerDragExited() {
        ListCell<Playlist> listCell = cellFactory.call(getListView());
        listCell.setStyle("some-style");
        
        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
        
        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_EXITED, mockDragboard, TransferMode.COPY, new Object()));
        
        listCell.onDragExitedProperty().get().handle(spyDragEvent);
        
        assertThat("List cell style should be empty", listCell.getStyle(), isEmptyString());
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
        
        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));
        
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
        
        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));

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
        ReflectionTestUtils.setField(ContextHelper.class, "applicationContext", originalContext);
    }
}
