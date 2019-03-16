package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class TrackTableCellFactoryTest extends AbstractTest implements Constants {

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private PlaylistManager mockPlaylistManager;

    private TrackTableCellFactory<TrackTableModel, String> cellFactory;

    @Before
    public void setup() {
        cellFactory = new TrackTableCellFactory<>();
        setField(cellFactory, "eventManager", getMockEventManager());
        setField(cellFactory, "settingsManager", mockSettingsManager);
        setField(cellFactory, "playlistManager", mockPlaylistManager);

        reset(mockSettingsManager);
        reset(mockPlaylistManager);
    }

    @Test
    public void shouldSinglePrimaryClickOnCell() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        tableCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(getMockEventManager(), times(1)).fireEvent(Event.TRACK_SELECTED, track);
    }

    @Test
    public void shouldDoublePrimaryClickOnCell() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        tableCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(mockPlaylistManager, times(1)).playTrack(track);
    }

    @Test
    public void shouldSinglePrimaryClickOnCellItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        tableCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(getMockEventManager(), never()).fireEvent(Event.TRACK_SELECTED, track);
    }

    @Test
    public void shouldDoublePrimaryClickOnCellItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        tableCell.onMouseClickedProperty().get()
            .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(mockPlaylistManager, never()).playTrack(track);
    }

    @Test
    public void shouldClickCreatePlaylistFromAlbumItem() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);

        createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

        verify(mockPlaylistManager, times(1)).createPlaylistFromAlbum(track);
    }

    @Test
    public void shouldClickCreatePlaylistFromAlbumItemItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);

        createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

        verify(mockPlaylistManager, never()).createPlaylistFromAlbum(track);
    }

    @Test
    public void shouldClickDeleteTrackFromPlaylistItem() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(mockPlaylistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldClickDeleteTrackFromPlaylistItemItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(mockPlaylistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldOpenContextMenuOnSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        track.setPlaylistId(PLAYLIST_ID_SEARCH);
        updateTableCell(tableCell, track);

        tableCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(tableCell));

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        assertThat(createPlaylistFromAlbumItem.isDisable()).isFalse();
        assertThat(deleteTrackFromPlaylistItem.isDisable()).isTrue();
    }

    @Test
    public void shouldOpenContextMenuOnNonSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        tableCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(tableCell));

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        assertThat(createPlaylistFromAlbumItem.isDisable()).isTrue();
        assertThat(deleteTrackFromPlaylistItem.isDisable()).isFalse();
    }

    @Test
    public void shouldOpenContextMenuWhenItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        tableCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(tableCell));

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        assertThat(createPlaylistFromAlbumItem.isDisable()).isTrue();
        assertThat(deleteTrackFromPlaylistItem.isDisable()).isTrue();
    }

    @Test
    public void shouldTriggerDragOver() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, times(1)).acceptTransferModes(TransferMode.MOVE);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSameSource() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, tableCell));

        tableCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotTriggerDragOverWithNoTrackTableModel() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setItem(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        track.setPlaylistId(PLAYLIST_ID_SEARCH);
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragEntered() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_ENTERED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isNotEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithSameSource() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, mockDragboard, TransferMode.COPY, tableCell));

        tableCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_ENTERED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotTriggerDragEnteredWithNoTrackTableModel() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setItem(null);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_ENTERED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        track.setPlaylistId(PLAYLIST_ID_SEARCH);
        updateTableCell(tableCell, track);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_ENTERED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragExited() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track track = getTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setStyle("some-style");

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_EXITED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragExitedProperty().get().handle(spyDragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDropped() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track target = getTrack();
        updateTableCell(tableCell, target);

        Track source = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 2, "Location 1", true, Arrays.asList("Genre 1", "Genre 2"));
        source.setPlaylistId(2);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
        when(mockDragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(source);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDroppedProperty().get().handle(spyDragEvent);

        verify(mockPlaylistManager, times(1)).moveTracksInPlaylist(source.getPlaylistId(), source, target);
        verify(spyDragEvent, times(1)).setDropCompleted(true);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragDroppedWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track target = getTrack();
        updateTableCell(tableCell, target);

        Track source = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 2, "Location 1", true, Arrays.asList("Genre 1", "Genre 2"));
        source.setPlaylistId(2);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent spyDragEvent = spy(
            getDragEvent(DragEvent.DRAG_DROPPED, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDroppedProperty().get().handle(spyDragEvent);

        verify(mockPlaylistManager, never()).moveTracksInPlaylist(source.getPlaylistId(), source, target);
        verify(spyDragEvent, never()).setDropCompleted(true);
        verify(spyDragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDone() {
        TableCell<TrackTableModel, String> tableCell = cellFactory.call(new TableColumn<>());
        Track target = getTrack();
        updateTableCell(tableCell, target);

        Dragboard mockDragboard = mock(Dragboard.class);
        when(mockDragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent spyDragEvent = spy(getDragEvent(DragEvent.DRAG_DONE, mockDragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDoneProperty().get().handle(spyDragEvent);

        verify(spyDragEvent, times(1)).consume();
    }

    private Track getTrack() {
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789",
            "Track Name", 1, "Location", true, Arrays.asList("Genre 1", "Genre 2"));
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
