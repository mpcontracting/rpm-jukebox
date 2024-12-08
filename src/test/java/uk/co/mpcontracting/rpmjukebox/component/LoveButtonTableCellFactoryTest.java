package uk.co.mpcontracting.rpmjukebox.component;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CONTENT_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createMouseEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistId;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrackId;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class LoveButtonTableCellFactoryTest extends AbstractGuiTest {

  @Mock
  private PlaylistService playlistService;

  private LoveButtonTableCellFactory underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new LoveButtonTableCellFactory(playlistService);
    setupMockEventProcessor(underTest);
  }

  @Test
  void shouldSinglePrimaryClickOnCellTrackInPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    tableCell.setItem(createTrackId());

    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(createPlaylistId());
    TrackTableModel trackTableModel = new TrackTableModel(track);

    @SuppressWarnings("unchecked")
    TableRow<TrackTableModel> mockTableRow = (TableRow<TrackTableModel>) mock(TableRow.class);
    when(mockTableRow.getItem()).thenReturn(trackTableModel);

    ReflectionTestUtils.invokeMethod(tableCell, "setTableRow", mockTableRow);

    when(playlistService.isTrackInPlaylist(anyInt(), anyString())).thenReturn(true);

    tableCell.onMouseClickedProperty().get().handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

    verify(playlistService, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(playlistService).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(getMockEventProcessor()).fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
  }

  @Test
  void shouldSinglePrimaryClickOnCellTrackNotInPlaylist() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    tableCell.setItem(createTrackId());

    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(createPlaylistId());
    TrackTableModel trackTableModel = new TrackTableModel(track);

    @SuppressWarnings("unchecked")
    TableRow<TrackTableModel> mockTableRow = (TableRow<TrackTableModel>) mock(TableRow.class);
    when(mockTableRow.getItem()).thenReturn(trackTableModel);

    ReflectionTestUtils.invokeMethod(tableCell, "setTableRow", mockTableRow);

    when(playlistService.isTrackInPlaylist(anyInt(), anyString())).thenReturn(false);

    tableCell.onMouseClickedProperty().get().handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

    verify(playlistService).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(playlistService, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(getMockEventProcessor()).fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
  }

  @Test
  void shouldSinglePrimaryClickOnCellNullItem() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    tableCell.setItem(null);

    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(createPlaylistId());

    tableCell.onMouseClickedProperty().get().handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1));

    verify(playlistService, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(playlistService, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(getMockEventProcessor(), never()).fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
  }

  @Test
  void shouldDoublePrimaryClickOnCell() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    tableCell.setItem(null);

    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(createPlaylistId());

    tableCell.onMouseClickedProperty().get().handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 2));

    verify(playlistService, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(playlistService, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(getMockEventProcessor(), never()).fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
  }

  @Test
  void shouldSingleSecondaryClickOnCell() {
    TableCell<TrackTableModel, String> tableCell = underTest.call(new TableColumn<>());
    tableCell.setItem(null);

    Track track = createTrack(1, createGenre(), createGenre());
    track.setPlaylistId(createPlaylistId());

    tableCell.onMouseClickedProperty().get().handle(createMouseEvent(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1));

    verify(playlistService, never()).addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(playlistService, never()).removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
    verify(getMockEventProcessor(), never()).fireEvent(PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
  }
}