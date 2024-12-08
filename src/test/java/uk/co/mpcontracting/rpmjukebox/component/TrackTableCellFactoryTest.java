package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createContextMenuEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createDragEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createMouseEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.DND_TRACK_DATA_FORMAT;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class TrackTableCellFactoryTest extends AbstractGuiTest {

  @Autowired
  private StringResourceService stringResourceService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private PlaylistService playlistService;

  private TrackTableCellFactory<String> underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new TrackTableCellFactory<>(stringResourceService, settingsService, playlistService);
    setupMockEventProcessor(underTest);
  }

  @Test
  void shouldSinglePrimaryClickOnCell() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    tableCell.onMouseClickedProperty().get()
        .handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

    verify(getMockEventProcessor()).fireEvent(Event.TRACK_SELECTED, track);
  }

  @Test
  void shouldDoublePrimaryClickOnCell() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    tableCell.onMouseClickedProperty().get()
        .handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

    verify(playlistService).playTrack(track);
  }

  @Test
  void shouldSinglePrimaryClickOnCellItemIsNull() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.setItem(null);

    tableCell.onMouseClickedProperty().get()
        .handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

    verify(getMockEventProcessor(), never()).fireEvent(Event.TRACK_SELECTED, track);
  }

  @Test
  void shouldDoublePrimaryClickOnCellItemIsNull() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.setItem(null);

    tableCell.onMouseClickedProperty().get()
        .handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

    verify(playlistService, never()).playTrack(track);
  }

  @Test
  void shouldClickCreatePlaylistFromAlbumItem() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().getFirst();

    createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

    verify(playlistService).createPlaylistFromAlbum(track);
  }

  @Test
  void shouldClickCreatePlaylistFromAlbumItemItemIsNull() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.setItem(null);

    MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().getFirst();

    createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

    verify(playlistService, never()).createPlaylistFromAlbum(track);
  }

  @Test
  void shouldClickDeleteTrackFromPlaylistItem() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

    deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

    verify(playlistService).removeTrackFromPlaylist(track.getPlaylistId(), track);
  }

  @Test
  void shouldClickDeleteTrackFromPlaylistItemItemIsNull() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.setItem(null);

    MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

    deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

    verify(playlistService, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
  }

  @Test
  void shouldOpenContextMenuOnSearchPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    track.setPlaylistId(PLAYLIST_ID_SEARCH);
    updateTableCell(tableCell, track);

    tableCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(tableCell));

    MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
    MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

    assertThat(createPlaylistFromAlbumItem.isDisable()).isFalse();
    assertThat(deleteTrackFromPlaylistItem.isDisable()).isTrue();
  }

  @Test
  void shouldOpenContextMenuOnNonSearchPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    tableCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(tableCell));

    MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
    MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

    assertThat(createPlaylistFromAlbumItem.isDisable()).isTrue();
    assertThat(deleteTrackFromPlaylistItem.isDisable()).isFalse();
  }

  @Test
  void shouldOpenContextMenuWhenItemIsNull() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.setItem(null);

    tableCell.onContextMenuRequestedProperty().get().handle(createContextMenuEvent(tableCell));

    MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
    MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

    assertThat(createPlaylistFromAlbumItem.isDisable()).isTrue();
    assertThat(deleteTrackFromPlaylistItem.isDisable()).isTrue();
  }

  @Test
  void shouldTriggerDragOver() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent).acceptTransferModes(TransferMode.MOVE);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithSameSource() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, tableCell));

    tableCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithNoContent() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithNoTrackTableModel() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.getTableRow().setItem(null);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragOverWithSearchPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    track.setPlaylistId(PLAYLIST_ID_SEARCH);
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragOverProperty().get().handle(dragEvent);

    verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragEntered() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isNotEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithSameSource() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, tableCell));

    tableCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithNoContent() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithNoTrackTableModel() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.getTableRow().setItem(null);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragEnteredWithSearchPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    track.setPlaylistId(PLAYLIST_ID_SEARCH);
    updateTableCell(tableCell, track);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragEnteredProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragExited() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track track = createTargetTrack();
    updateTableCell(tableCell, track);
    tableCell.getTableRow().setStyle("some-style: style");

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_EXITED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragExitedProperty().get().handle(dragEvent);

    assertThat(tableCell.getTableRow().getStyle()).isEmpty();
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragDropped() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track target = createTargetTrack();
    updateTableCell(tableCell, target);

    Track source = createTrack(1, createGenre(), createGenre());
    source.setPlaylistId(2);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
    when(dragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(source);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragDroppedProperty().get().handle(dragEvent);

    verify(playlistService).moveTracksInPlaylist(source.getPlaylistId(), source, target);
    verify(dragEvent).setDropCompleted(true);
    verify(dragEvent).consume();
  }

  @Test
  void shouldNotTriggerDragDroppedWithNoContent() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track target = createTargetTrack();
    updateTableCell(tableCell, target);

    Track source = createTrack(1, createGenre(), createGenre());
    source.setPlaylistId(2);

    Dragboard dragboard = mock(Dragboard.class);
    when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragDroppedProperty().get().handle(dragEvent);

    verify(playlistService, never()).moveTracksInPlaylist(source.getPlaylistId(), source, target);
    verify(dragEvent, never()).setDropCompleted(true);
    verify(dragEvent).consume();
  }

  @Test
  void shouldTriggerDragDone() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    Track target = createTargetTrack();
    updateTableCell(tableCell, target);

    Dragboard dragboard = mock(Dragboard.class);
    DragEvent dragEvent = spy(createDragEvent(DragEvent.DRAG_DONE, dragboard, TransferMode.COPY, new Object()));

    tableCell.onDragDoneProperty().get().handle(dragEvent);

    verify(dragEvent).consume();
  }

  private Track createTargetTrack() {
    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(1);

    return track;
  }

  private void updateTableCell(TableCell<TrackTableModel, String> tableCell, Track track) {
    tableCell.setItem(track.getTrackName());

    TrackTableModel trackTableModel = new TrackTableModel(track);
    TableRow<TrackTableModel> tableRow = new TableRow<>();
    tableRow.setItem(trackTableModel);
    tableCell.updateTableRow(tableRow);
  }
}