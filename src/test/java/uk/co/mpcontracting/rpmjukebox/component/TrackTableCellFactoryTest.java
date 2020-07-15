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
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class TrackTableCellFactoryTest extends AbstractGUITest implements Constants {

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private PlaylistManager playlistManager;

    private TrackTableCellFactory<TrackTableModel, String> underTest;

    @Before
    public void setup() {
        underTest = new TrackTableCellFactory<>();
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "settingsManager", settingsManager);
        setField(underTest, "playlistManager", playlistManager);

        reset(settingsManager);
        reset(playlistManager);
    }

    @Test
    public void shouldDoublePrimaryClickOnCell() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(playlistManager, times(1)).playTrack(track);
    }

    @Test
    public void shouldSinglePrimaryClickOnCellItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(getMockEventManager(), never()).fireEvent(Event.TRACK_SELECTED, track);
    }

    @Test
    public void shouldDoublePrimaryClickOnCellItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(playlistManager, never()).playTrack(track);
    }

    @Test
    public void shouldClickCreatePlaylistFromAlbumItem() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);

        createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

        verify(playlistManager, times(1)).createPlaylistFromAlbum(track);
    }

    @Test
    public void shouldClickCreatePlaylistFromAlbumItemItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);

        createPlaylistFromAlbumItem.onActionProperty().get().handle(new ActionEvent());

        verify(playlistManager, never()).createPlaylistFromAlbum(track);
    }

    @Test
    public void shouldClickDeleteTrackFromPlaylistItem() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(playlistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldClickDeleteTrackFromPlaylistItemItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.setItem(null);

        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        deleteTrackFromPlaylistItem.onActionProperty().get().handle(new ActionEvent());

        verify(playlistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldOpenContextMenuOnSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
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
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        tableCell.onContextMenuRequestedProperty().get().handle(getContextMenuEvent(tableCell));

        MenuItem createPlaylistFromAlbumItem = tableCell.getContextMenu().getItems().get(0);
        MenuItem deleteTrackFromPlaylistItem = tableCell.getContextMenu().getItems().get(1);

        assertThat(createPlaylistFromAlbumItem.isDisable()).isTrue();
        assertThat(deleteTrackFromPlaylistItem.isDisable()).isFalse();
    }

    @Test
    public void shouldOpenContextMenuWhenItemIsNull() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
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
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, times(1)).acceptTransferModes(TransferMode.MOVE);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSameSource() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, tableCell));

        tableCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotTriggerDragOverWithNoTrackTableModel() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setItem(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragOverWithSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        track.setPlaylistId(PLAYLIST_ID_SEARCH);
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_OVER, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragOverProperty().get().handle(dragEvent);

        verify(dragEvent, never()).acceptTransferModes(TransferMode.MOVE);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragEntered() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isNotEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithSameSource() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, tableCell));

        tableCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldNotTriggerDragEnteredWithNoTrackTableModel() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setItem(null);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragEnteredWithSearchPlaylist() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        track.setPlaylistId(PLAYLIST_ID_SEARCH);
        updateTableCell(tableCell, track);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_ENTERED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragEnteredProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragExited() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track track = getTargetTrack();
        updateTableCell(tableCell, track);
        tableCell.getTableRow().setStyle("some-style: style");

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_EXITED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragExitedProperty().get().handle(dragEvent);

        assertThat(tableCell.getTableRow().getStyle()).isEmpty();
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDropped() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track target = getTargetTrack();
        updateTableCell(tableCell, target);

        Track source = generateTrack(1, "Genre 1", "Genre 2");
        source.setPlaylistId(2);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);
        when(dragboard.getContent(DND_TRACK_DATA_FORMAT)).thenReturn(source);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDroppedProperty().get().handle(dragEvent);

        verify(playlistManager, times(1)).moveTracksInPlaylist(source.getPlaylistId(), source, target);
        verify(dragEvent, times(1)).setDropCompleted(true);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldNotTriggerDragDroppedWithNoContent() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track target = getTargetTrack();
        updateTableCell(tableCell, target);

        Track source = generateTrack(1, "Genre 1", "Genre 2");
        source.setPlaylistId(2);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(false);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_DROPPED, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDroppedProperty().get().handle(dragEvent);

        verify(playlistManager, never()).moveTracksInPlaylist(source.getPlaylistId(), source, target);
        verify(dragEvent, never()).setDropCompleted(true);
        verify(dragEvent, times(1)).consume();
    }

    @Test
    public void shouldTriggerDragDone() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        Track target = getTargetTrack();
        updateTableCell(tableCell, target);

        Dragboard dragboard = mock(Dragboard.class);
        when(dragboard.hasContent(DND_TRACK_DATA_FORMAT)).thenReturn(true);

        DragEvent dragEvent = spy(getDragEvent(DragEvent.DRAG_DONE, dragboard, TransferMode.COPY, new Object()));

        tableCell.onDragDoneProperty().get().handle(dragEvent);

        verify(dragEvent, times(1)).consume();
    }

    private Track getTargetTrack() {
        Track track = generateTrack(1, "Genre 1", "Genre 2");
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
