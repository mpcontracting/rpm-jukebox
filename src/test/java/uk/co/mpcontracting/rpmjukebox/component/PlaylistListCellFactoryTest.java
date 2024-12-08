package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createContextMenuEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createDragEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.DND_TRACK_DATA_FORMAT;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.EventProcessor;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class PlaylistListCellFactoryTest extends AbstractGuiTest {

  @Autowired
  private StringResourceService stringResourceService;

  @Mock
  private ApplicationContext applicationContext;

  @Mock
  private PlaylistService playlistService;

  @Mock
  private MainPanelController mainPanelController;

  private PlaylistListCellFactory underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new PlaylistListCellFactory(stringResourceService, playlistService);
    setupMockEventProcessor(underTest);

    when(applicationContext.getBean(EventProcessor.class)).thenReturn(getMockEventProcessor());
    underTest.setApplicationContext(applicationContext);
  }

  @Test
  void shouldClickNewPlaylistItem() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    MenuItem newPlaylistItem = listCell.getContextMenu().getItems().getFirst();

    newPlaylistItem.onActionProperty().get().handle(new ActionEvent());

    verify(playlistService).createPlaylist();
  }

  @Test
  void shouldClickDeletePlaylistItem() {
    when(applicationContext.getBean(MainPanelController.class)).thenReturn(mainPanelController);

    ListView<Playlist> listView = createListView();
    ListCell<Playlist> listCell = underTest.call(listView);
    listView.getSelectionModel().select(5);

    MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

    deletePlaylistItem.onActionProperty().get().handle(new ActionEvent());

    ArgumentCaptor<Runnable> okRunnable = ArgumentCaptor.forClass(Runnable.class);

    verify(mainPanelController).showConfirmView(anyString(), anyBoolean(), okRunnable.capture(), any());

    okRunnable.getValue().run();

    verify(playlistService).deletePlaylist(3);
  }

  @Test
  void shouldOpenContextMenuOnReservedPlaylist() {
    ListView<Playlist> listView = createListView();
    ListCell<Playlist> listCell = underTest.call(listView);

    listCell.updateListView(listView);
    listCell.updateIndex(0);

    listCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(listCell));

    MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
    MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

    assertThat(newPlaylistItem.isDisable()).isTrue();
    assertThat(deletePlaylistItem.isDisable()).isTrue();
  }

  @Test
  void shouldOpenContextMenuOnUserPlaylist() {
    ListView<Playlist> listView = createListView();
    ListCell<Playlist> listCell = underTest.call(listView);

    listCell.updateListView(listView);
    listCell.updateIndex(2);

    listCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(listCell));

    MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
    MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

    assertThat(newPlaylistItem.isDisable()).isTrue();
    assertThat(deletePlaylistItem.isDisable()).isFalse();
  }

  @Test
  void shouldOpenContextMenuBelowPlaylists() {
    ListView<Playlist> listView = createListView();
    ListCell<Playlist> listCell = underTest.call(listView);

    listCell.updateListView(listView);
    listCell.updateIndex(10);

    listCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(listCell));

    MenuItem newPlaylistItem = listCell.getContextMenu().getItems().get(0);
    MenuItem deletePlaylistItem = listCell.getContextMenu().getItems().get(1);

    assertThat(newPlaylistItem.isDisable()).isFalse();
    assertThat(deletePlaylistItem.isDisable()).isTrue();
  }

  @Test
  void shouldTriggerDragOver() {
    ListCell<Playlist> listCell = underTest.call(createListView());

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent spyDragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragOverProperty().get().handle(spyDragEvent);

    verify(spyDragEvent).acceptTransferModes(TransferMode.COPY);
    verify(spyDragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithSameSource() {
    ListCell<Playlist> listCell = underTest.call(createListView());

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, listCell));

    listCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.COPY);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithNoContent() {
    ListCell<Playlist> listCell = underTest.call(createListView());

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.COPY);
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragEntered() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(1, createPlaylistName(), 10));
    listCell.setStyle(null);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isNotEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithSameSource() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(1, createPlaylistName(), 10));
    listCell.setStyle(null);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, listCell));

    listCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithNoContent() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(1, createPlaylistName(), 10));
    listCell.setStyle(null);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithNoPlaylist() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(null);
    listCell.setStyle(null);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithReservedPlaylist() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10));
    listCell.setStyle(null);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragExited() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setStyle("some-style: style");

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_EXITED, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragExitedProperty().get().handle(dragEvent);

    assertThat(listCell.getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragDropped() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(1, createPlaylistName(), 10));

    Track track = mock(Track.class);
    when(track.createClone()).thenReturn(track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
    when(dragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(track);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragDroppedProperty().get().handle(dragEvent);

    verify(playlistService).addTrackToPlaylist(1, track);
    verify(dragEvent).setDropCompleted(true);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragDroppedWithNoContent() {
    ListCell<Playlist> listCell = underTest.call(createListView());
    listCell.setItem(new Playlist(1, createPlaylistName(), 10));

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

    listCell.onDragDroppedProperty().get().handle(dragEvent);

    verify(playlistService, never()).addTrackToPlaylist(anyInt(), any());
    verify(dragEvent, never()).setDropCompleted(true);
    verify(dragEvent).consume();
  }

  private ListView<Playlist> createListView() {
    ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    for (int i = -2; i < 8; i++) {
      playlists.add(new Playlist(i, createPlaylistName(), 10));
    }

    return new ListView<>(playlists);
  }
}