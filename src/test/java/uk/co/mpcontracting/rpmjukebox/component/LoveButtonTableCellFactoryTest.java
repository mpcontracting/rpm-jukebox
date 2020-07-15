package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getMouseEvent;

public class LoveButtonTableCellFactoryTest extends AbstractGUITest implements Constants {

    @Mock
    private PlaylistManager playlistManager;

    private LoveButtonTableCellFactory underTest;

    @Before
    public void setup() {
        underTest = new LoveButtonTableCellFactory();
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "playlistManager", playlistManager);

        reset(playlistManager);
    }

    @Test
    public void shouldSinglePrimaryClickOnCellTrackInPlaylist() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        tableCell.setItem("trackId");

        Track track = generateTrack(1, "Genre 1", "Genre 2");
        track.setPlaylistId(999);
        TrackTableModel trackTableModel = new TrackTableModel(track);

        @SuppressWarnings("unchecked")
        TableRow<TrackTableModel> mockTableRow = (TableRow<TrackTableModel>) mock(TableRow.class);
        when(mockTableRow.getItem()).thenReturn(trackTableModel);

        ReflectionTestUtils.invokeMethod(tableCell, "setTableRow", mockTableRow);

        when(playlistManager.isTrackInPlaylist(anyInt(), anyString())).thenReturn(true);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(playlistManager, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(playlistManager, times(1)).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
    }

    @Test
    public void shouldSinglePrimaryClickOnCellTrackNotInPlaylist() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        tableCell.setItem("trackId");

        Track track = generateTrack(1, "Genre 1", "Genre 2");
        track.setPlaylistId(999);
        TrackTableModel trackTableModel = new TrackTableModel(track);

        @SuppressWarnings("unchecked")
        TableRow<TrackTableModel> mockTableRow = (TableRow<TrackTableModel>) mock(TableRow.class);
        when(mockTableRow.getItem()).thenReturn(trackTableModel);

        ReflectionTestUtils.invokeMethod(tableCell, "setTableRow", mockTableRow);

        when(playlistManager.isTrackInPlaylist(anyInt(), anyString())).thenReturn(false);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(playlistManager, times(1)).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(playlistManager, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
    }

    @Test
    public void shouldSinglePrimaryClickOnCellNullItem() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        tableCell.setItem(null);

        Track track = generateTrack(1, "Genre 1", "Genre 2");
        track.setPlaylistId(999);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

        verify(playlistManager, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(playlistManager, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
    }

    @Test
    public void shouldDoublePrimaryClickOnCell() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        tableCell.setItem(null);

        Track track = generateTrack(1, "Genre 1", "Genre 2");
        track.setPlaylistId(999);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

        verify(playlistManager, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(playlistManager, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
    }

    @Test
    public void shouldSingleSecondaryClickOnCell() {
        TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
        tableCell.setItem(null);

        Track track = generateTrack(1, "Genre 1", "Genre 2");
        track.setPlaylistId(999);

        tableCell.onMouseClickedProperty().get()
                .handle(getMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));

        verify(playlistManager, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(playlistManager, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
    }
}
