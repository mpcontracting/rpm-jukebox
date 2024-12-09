package uk.co.mpcontracting.rpmjukebox.controller;

import static javafx.scene.input.KeyCode.BACK_SPACE;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CONTENT_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CREATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_DELETED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_QUEUED_FOR_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createKeyEvent;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylist;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

class TrackTableControllerTest extends AbstractGuiTest {

  @MockBean
  private PlaylistService playlistService;

  @MockBean
  private SettingsService settingsService;

  @Autowired
  private TrackTableView trackTableView;

  @Autowired
  private TrackTableController underTest;

  private static boolean isSpySet = false;
  private uk.co.mpcontracting.rpmjukebox.component.TrackTableView trackTableViewComponent;

  @SneakyThrows
  @PostConstruct
  void postConstruct() {
    init(trackTableView);
  }

  @BeforeEach
  void beforeEach() {
    getNonNullField(underTest, "observableTracks", ObservableList.class).clear();

    if (!isSpySet) {
      trackTableViewComponent = spy(getNonNullField(underTest, "trackTableView", uk.co.mpcontracting.rpmjukebox.component.TrackTableView.class));
      setField(underTest, "trackTableView", trackTableViewComponent);
      isSpySet = true;
    } else {
      trackTableViewComponent = getNonNullField(underTest, "trackTableView", uk.co.mpcontracting.rpmjukebox.component.TrackTableView.class);
    }
  }

  @Test
  void shouldUpdateObservableTracks() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    invokeMethod(underTest, "updateObservableTracks", 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
  }

  @Test
  void shouldGetSelectedTrackFromWithinPlaylist() {
    Optional<Playlist> playlist = createPlaylist();
    int playlistSize = playlist.orElseThrow().size();
    int index = getFaker().number().numberBetween(0, playlistSize - 1);

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    invokeMethod(underTest, "updateObservableTracks", 1);

    trackTableViewComponent.getSelectionModel().select(index);

    Track track = underTest.getSelectedTrack();

    assertThat(track.getTrackId()).isEqualTo(playlist.orElseThrow().getTracks().get(index).getTrackId());
  }

  @Test
  void shouldGetNullTrackWhenSelectedIsOutsidePlaylist() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    invokeMethod(underTest, "updateObservableTracks", 1);

    trackTableViewComponent.getSelectionModel().select(playlist.orElseThrow().size());

    Track track = underTest.getSelectedTrack();

    assertThat(track).isNull();
  }

  @Test
  void shouldUpdatePlaylistFromEvent() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);

    underTest.eventReceived(PLAYLIST_CONTENT_UPDATED, 1, track);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldUpdatePlaylistFromEventWithoutIncludedTrack() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);

    underTest.eventReceived(PLAYLIST_CONTENT_UPDATED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotUpdatePlaylistFromEventWithNullPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);

    underTest.eventReceived(PLAYLIST_CONTENT_UPDATED, (Object[]) null);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotUpdatePlaylistFromEventWithEmptyPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);

    underTest.eventReceived(PLAYLIST_CONTENT_UPDATED);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotUpdatePlaylistFromEventWithDifferentVisiblePlaylistId() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);

    underTest.eventReceived(PLAYLIST_CONTENT_UPDATED, 1, track);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldCreatePlaylistFromEvent() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_CREATED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldNotCreatePlaylistFromEventWithNullPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_CREATED, (Object[]) null);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotCreatePlaylistFromEventWithEmptyPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_CREATED);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotCreatePlaylistFromEventWithSameVisiblePlaylistId() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_CREATED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldDeletePlaylistFromEvent() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_DELETED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldNotDeletePlaylistFromEventWithNullPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_DELETED, (Object[]) null);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotDeletePlaylistFromEventWithEmptyPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_DELETED);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotDeletePlaylistFromEventWithSameVisiblePlaylistId() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_DELETED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldSelectPlaylistFromEvent() {
    Optional<Playlist> playlist = createPlaylist();

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_SELECTED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).hasSize(playlist.orElseThrow().size());
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldNotSelectPlaylistFromEventWithNullPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_SELECTED, (Object[]) null);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotSelectPlaylistFromEventWithEmptyPayload() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_SELECTED);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldNotSelectPlaylistFromEventWithSameVisiblePlaylistId() {
    when(playlistService.getPlaylist(1)).thenReturn(createPlaylist());
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    when(playlistService.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

    underTest.eventReceived(PLAYLIST_SELECTED, 1);

    @SuppressWarnings("unchecked")
    ObservableList<TrackTableModel> observableTracks = getField(underTest, "observableTracks", ObservableList.class);

    assertThat(observableTracks).isEmpty();
    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldQueueTrackForPlayingFromEvent() {
    when(playlistService.getCurrentPlaylistId()).thenReturn(1);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    track.setPlaylistId(1);

    underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, track);

    verify(trackTableViewComponent).highlightTrack(track);
  }

  @Test
  void shouldQueueTrackForPlayingFromEventWithNullPayload() {
    when(playlistService.getCurrentPlaylistId()).thenReturn(1);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    track.setPlaylistId(1);

    underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, (Object[]) null);

    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldQueueTrackForPlayingFromEventWithEmptyPayload() {
    when(playlistService.getCurrentPlaylistId()).thenReturn(1);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    track.setPlaylistId(1);

    underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING);

    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldQueueTrackForPlayingFromEventWithDifferentVisiblePlaylistId() {
    when(playlistService.getCurrentPlaylistId()).thenReturn(1);
    setField(underTest, "visiblePlaylistId", 2);

    Track track = createTrack(1);
    track.setPlaylistId(1);

    underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, track);

    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @Test
  void shouldQueueTrackForPlayingFromEventWithDifferentCurrentPlaylistId() {
    when(playlistService.getCurrentPlaylistId()).thenReturn(2);
    setField(underTest, "visiblePlaylistId", 1);

    Track track = createTrack(1);
    track.setPlaylistId(1);

    underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING, track);

    verify(trackTableViewComponent, never()).highlightTrack(track);
  }

  @ParameterizedTest
  @MethodSource("getKeyPresses")
  void shouldRemoveTrackFromPlaylistOnKeyPressed(KeyCode keyCode) {
    Optional<Playlist> playlist = createPlaylist();
    int selectionIndex = getFaker().number().numberBetween(0, playlist.orElseThrow().size());
    Track selectedTrack = playlist.orElseThrow().getTrackAtIndex(selectionIndex);

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    invokeMethod(underTest, "updateObservableTracks", 1);

    trackTableViewComponent.getSelectionModel().select(selectionIndex);
    trackTableViewComponent.onKeyPressedProperty().get().handle(createKeyEvent(KEY_PRESSED, keyCode));

    verify(playlistService).removeTrackFromPlaylist(selectedTrack.getPlaylistId(), selectedTrack);
  }

  @ParameterizedTest
  @MethodSource("getKeyPresses")
  void shouldNotRemoveTrackFromPlaylistOnKeyPressedWithNothingSelected(KeyCode keyCode) {
    Optional<Playlist> playlist = createPlaylist();
    int selectionIndex = getFaker().number().numberBetween(0, playlist.orElseThrow().size());
    Track selectedTrack = playlist.orElseThrow().getTrackAtIndex(selectionIndex);

    when(playlistService.getPlaylist(1)).thenReturn(playlist);
    invokeMethod(underTest, "updateObservableTracks", 1);

    trackTableViewComponent.getSelectionModel().clearSelection();
    trackTableViewComponent.onKeyPressedProperty().get().handle(createKeyEvent(KEY_PRESSED, keyCode));

    verify(playlistService, never()).removeTrackFromPlaylist(selectedTrack.getPlaylistId(), selectedTrack);
  }

  private static Stream<Arguments> getKeyPresses() {
    return Stream.of(
        Arguments.of(BACK_SPACE),
        Arguments.of(DELETE)
    );
  }
}