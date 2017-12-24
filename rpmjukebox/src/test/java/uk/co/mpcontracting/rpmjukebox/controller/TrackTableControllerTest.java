package uk.co.mpcontracting.rpmjukebox.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.annotation.PostConstruct;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

public class TrackTableControllerTest extends AbstractTest {

    @Autowired
    private TrackTableController trackTableController;

    @Autowired
    private TrackTableView trackTableView;

    @Mock
    private PlaylistManager mockPlaylistManager;

    private uk.co.mpcontracting.rpmjukebox.component.TrackTableView<TrackTableModel> spyTrackTableView;

    @PostConstruct
    public void constructView() throws Exception {
        init(trackTableView);
    }

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        ReflectionTestUtils.setField(trackTableController, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(trackTableController, "playlistManager", mockPlaylistManager);

        spyTrackTableView = spy(
            (uk.co.mpcontracting.rpmjukebox.component.TrackTableView<TrackTableModel>)ReflectionTestUtils
                .getField(trackTableController, "trackTableView"));
        ReflectionTestUtils.setField(trackTableController, "trackTableView", spyTrackTableView);

        ((ObservableList<TrackTableModel>)ReflectionTestUtils.getField(trackTableController, "observableTracks"))
            .clear();
    }

    @Test
    public void shouldUpdateObservableTracks() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
    }

    @Test
    public void shouldGetSelectedTrackFromWithinPlaylist() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().select(1);

        Track track = trackTableController.getSelectedTrack();

        assertThat("Track ID should be '7891'", track.getTrackId(), equalTo("7891"));
    }

    @Test
    public void shouldGetSelectedTrackFromOutsidePlaylist() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().select(10);

        Track track = trackTableController.getSelectedTrack();

        assertThat("Track should be null", track, nullValue());
    }

    @Test
    public void shouldUpdatePlaylistFromEvent() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);

        trackTableController.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1, track);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldUpdatePlaylistFromEventWithoutIncludedTrack() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);

        trackTableController.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithNullPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);

        trackTableController.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, (Object[])null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithEmptyPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);

        trackTableController.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, new Object[] {});

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotUpdatePlaylistFromEventWithDifferentVisiblePlaylistId() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);

        trackTableController.eventReceived(Event.PLAYLIST_CONTENT_UPDATED, 1, track);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldCreatePlaylistFromEvent() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_CREATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithNullPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_CREATED, (Object[])null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithEmptyPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_CREATED, new Object[] {});

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotCreatePlaylistFromEventWithSameVisiblePlaylistId() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_CREATED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldDeletePlaylistFromEvent() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_DELETED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithNullPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_DELETED, (Object[])null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithEmptyPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_DELETED, new Object[] {});

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotDeletePlaylistFromEventWithSameVisiblePlaylistId() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_DELETED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldSelectPlaylistFromEvent() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_SELECTED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should have a size of 10", observableTracks, hasSize(10));
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithNullPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_SELECTED, (Object[])null);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithEmptyPayload() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_SELECTED, new Object[] {});

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldNotSelectPlaylistFromEventWithSameVisiblePlaylistId() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        when(mockPlaylistManager.getTrackAtPlayingPlaylistIndex()).thenReturn(track);

        trackTableController.eventReceived(Event.PLAYLIST_SELECTED, 1);

        @SuppressWarnings("unchecked")
        ObservableList<TrackTableModel> observableTracks = (ObservableList<TrackTableModel>)ReflectionTestUtils
            .getField(trackTableController, "observableTracks");

        assertThat("Observable tracks should be empty", observableTracks, empty());
        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEvent() {
        when(mockPlaylistManager.getCurrentPlaylistId()).thenReturn(1);
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);
        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        trackTableController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(spyTrackTableView, times(1)).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithNullPayload() {
        when(mockPlaylistManager.getCurrentPlaylistId()).thenReturn(1);
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);
        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        trackTableController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, (Object[])null);

        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithEmptyPayload() {
        when(mockPlaylistManager.getCurrentPlaylistId()).thenReturn(1);
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);
        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        trackTableController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, new Object[] {});

        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithDifferentVisiblePlaylistId() {
        when(mockPlaylistManager.getCurrentPlaylistId()).thenReturn(1);
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 2);
        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        trackTableController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldQueueTrackForPlayingFromEventWithDifferentCurrentPlaylistId() {
        when(mockPlaylistManager.getCurrentPlaylistId()).thenReturn(2);
        ReflectionTestUtils.setField(trackTableController, "visiblePlaylistId", 1);
        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        trackTableController.eventReceived(Event.TRACK_QUEUED_FOR_PLAYING, track);

        verify(spyTrackTableView, never()).highlightTrack(track);
    }

    @Test
    public void shouldRemoveTrackFromPlaylistOnBackSpace() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().select(1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        spyTrackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.BACK_SPACE));

        verify(mockPlaylistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldNotRemoveTrackFromPlaylistOnBackSpaceWithNothingSelected() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().clearSelection();

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        spyTrackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.BACK_SPACE));

        verify(mockPlaylistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldRemoveTrackFromPlaylistOnDelete() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().select(1);

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        spyTrackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.DELETE));

        verify(mockPlaylistManager, times(1)).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    @Test
    public void shouldNotRemoveTrackFromPlaylistOnDeleteWithNothingSelected() {
        when(mockPlaylistManager.getPlaylist(1)).thenReturn(generatePlaylist());
        ReflectionTestUtils.invokeMethod(trackTableController, "updateObservableTracks", 1);

        spyTrackTableView.getSelectionModel().clearSelection();

        Track track = new Track("1231", "Artist Name 1", "Artist Image 1", "4561", "Album Name 1", "Album Image 1",
            2001, "7891", "Track Name 1", 1, "Location 1", true, null);
        track.setPlaylistId(1);

        spyTrackTableView.onKeyPressedProperty().get().handle(getKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.DELETE));

        verify(mockPlaylistManager, never()).removeTrackFromPlaylist(track.getPlaylistId(), track);
    }

    private Playlist generatePlaylist() {
        Playlist playlist = new Playlist(1, "Playlist", 10);
        for (int i = 0; i < 10; i++) {
            playlist
                .addTrack(new Track("123" + i, "Artist Name " + i, "Artist Image " + i, "456" + i, "Album Name " + i,
                    "Album Image " + i, 2000 + i, "789" + i, "Track Name " + i, (i + 1), "Location " + i, true, null));
        }

        return playlist;
    }
}
