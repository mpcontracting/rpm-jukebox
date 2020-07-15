package uk.co.mpcontracting.rpmjukebox.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
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
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getContextMenuEvent;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getDragEvent;

public class PlaylistListCellFactoryTest extends AbstractGUITest implements Constants {

    @Mock
    private PlaylistManager playlistManager;

    private ApplicationContext originalContext;
    private MainPanelController mainPanelController;

    private PlaylistListCellFactory underTest;



    @Before
    public void setup() {
        underTest = new PlaylistListCellFactory();
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "playlistManager", playlistManager);

        reset(playlistManager);

        originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        mainPanelController = mock(MainPanelController.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(MainPanelController.class)).thenReturn(mainPanelController);

        setField(ContextHelper.class, "applicationContext", applicationContext);
    }

    @Test
    public void shouldClickNewPlaylistItem() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);

        newPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(playlistManager, times(1)).createPlaylist();
    }

    @Test
    public void shouldClickDeletePlaylistItem() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = underTest.call(listView);
        listView.getSelectionModel().select(5);

        MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

        deletePlaylistItem.onActionProperty().get().handle(new ActionEvent());

        ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

        verify(mainPanelController, times(1)).showConfirmView(anyString(), anyBoolean(), okRunnable.capture(),
                any());

        okRunnable.getValue().run();

        verify(playlistManager, times(1)).deletePlaylist(3);
    }

    @Test
    public void shouldOpenContextMenuOnReservedPlaylist() {
        ListView<Playlist> listView = getListView();
        ListCell<Playlist> listCell = underTest.call(listView);

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
        ListCell<Playlist> listCell = underTest.call(listView);

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
        ListCell<Playlist> listCell = underTest.call(listView);

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
        ListCell<Playlist> listCell = underTest.call(getListView());

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, times(1)).acceptTransferModes(TransferMode.COPY);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSameSource() {
        ListCell<Playlist> listCell = underTest.call(getListView());

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, listCell));

        listCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.COPY);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithNoContent() {
        ListCell<Playlist> listCell = underTest.call(getListView());

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.COPY);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragEntered() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isNotEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithSameSource() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, listCell));

        listCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoContent() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));
        listCell.setStyle(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoPlaylist() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(null);
        listCell.setStyle(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithReservedPlaylist() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));
        listCell.setStyle(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragExited() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setStyle("some-style: style");

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_EXITED, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragExitedProperty().get().handle(dragEvent);

        assertThat(listCell.getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDropped() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));

        Track track = mock(Track.class);
        when(track.clone()).thenReturn(track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
        when(dragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(track);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragDroppedProperty().get().handle(dragEvent);

        verify(playlistManager, times(1)).addTrackToPlaylist(1, track);
        verify(dragEvent, times(1)).setDropCompleted(true);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragDroppedWithNoContent() {
        ListCell<Playlist> listCell = underTest.call(getListView());
        listCell.setItem(new Playlist(1, "Playlist", 10));

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

        listCell.onDragDroppedProperty().get().handle(dragEvent);

        verify(playlistManager, never()).addTrackToPlaylist(anyInt(), any());
        verify(dragEvent, never()).setDropCompleted(true);
        verify(dragEvent, times(1)).consume();
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
