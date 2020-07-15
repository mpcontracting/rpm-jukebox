package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class TrackTableControllerTest extends AbstractGUITest {

    @Autowired
    private TrackTableController underTest;

    @Autowired
    private TrackTableView originalTrackTableView;

    @Mock
    private PlaylistManager playlistManager;

    private uk.co.mpcontracting.rpmjukebox.component.TrackTableView<TrackTableModel> trackTableView;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(originalTrackTableView);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "playlistManager", playlistManager);

        trackTableView = spy(
                (uk.co.mpcontracting.rpmjukebox.component.TrackTableView<TrackTableModel>) getNonNullField(underTest, "trackTableView"));
        setField(underTest, "trackTableView", trackTableView);

        ((ObservableList<TrackTableModel>) getNonNullField(underTest, "observableTracks")).clear();
    }

    @Test
    public void shouldUpdateObservableTracks() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
    }

    @Test
    public void shouldGetSelectedTrackFromWithinPlaylist() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().select(1);

        Track track = underTest.getSelectedTrack();

        assertThat(track.getTrackId()).isEqualTo("7891");
    }

    @Test
    public void shouldGetSelectedTrackFromOutsidePlaylist() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().select(10);

        Track track = underTest.getSelectedTrack();

        assertThat(track).isNull();
    }

    @Test
    public void shouldUpdatePlaylistFromEvent() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);

        underTest.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1, track);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldUpdatePlaylistFromEventWithoutIncludedTrack() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);

        underTest.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithNullPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);

        underTest.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, (Object[]) null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithEmptyPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);

        underTest.eventReceived(Event.PLAYLIST_CONTENT_UPDATED);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithDifferentVisiblePlaylistId() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);

        underTest.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1, track);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldCreatePlaylistFromEvent() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_CREATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithNullPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_CREATED, (Object[]) null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithEmptyPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_CREATED);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithSameVisiblePlaylistId() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_CREATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldDeletePlaylistFromEvent() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_DELETED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithNullPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_DELETED, (Object[]) null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithEmptyPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_DELETED);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithSameVisiblePlaylistId() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_DELETED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldSelectPlaylistFromEvent() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_SELECTED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).hasSize(10);
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithNullPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_SELECTED, (Object[]) null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithEmptyPayload() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_SELECTED);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithSameVisiblePlaylistId() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        when(playlistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        underTest.eventReceived(Event.PLAYLIST_SELECTED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>) getField(underTest, "observableTracks");

        assertThat(observableTracks).isEmpty();
        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEvent() {
        when(playlistManager.getCurrentPlaylistId()).thenReturn(1);
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(trackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithNullPayload() {
        when(playlistManager.getCurrentPlaylistId()).thenReturn(1);
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, (Object[]) null);

        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithEmptyPayload() {
        when(playlistManager.getCurrentPlaylistId()).thenReturn(1);
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING);

        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithDifferentVisiblePlaylistId() {
        when(playlistManager.getCurrentPlaylistId()).thenReturn(1);
        setField(underTest, "visiblePlaylistId", 2);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithDifferentCurrentPlaylistId() {
        when(playlistManager.getCurrentPlaylistId()).thenReturn(2);
        setField(underTest, "visiblePlaylistId", 1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        underTest.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(trackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldRemoveTrackFromPlaylistOnBackSpace() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().select(1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        trackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.BACK_SPACE));

        verify(playlistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldNotRemoveTrackFromPlaylistOnBackSpaceWithNothingSelected() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().clearSelection();

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        trackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.BACK_SPACE));

        verify(playlistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldRemoveTrackFromPlaylistOnDelete() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().select(1);

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        trackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.DELETE));

        verify(playlistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldNotRemoveTrackFromPlaylistOnDeleteWithNothingSelected() {
        when(playlistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(underTest, "updateObservableTracks", 1);

        trackTableView.getSelectionModel().clearSelection();

        Track track = generateTrack(1);
        track.setPlaylistId(1);

        trackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.DELETE));

        verify(playlistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }
}
