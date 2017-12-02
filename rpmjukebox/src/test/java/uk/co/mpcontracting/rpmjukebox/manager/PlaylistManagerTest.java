package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class PlaylistManagerTest extends AbstractTest implements Constants {

    @Autowired
    private PlaylistManager playlistManager;

    @Autowired
    private MessageManager messageManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private MediaManager mockMediaManager;

    @Mock
    private TrackTableController mockTrackTableController;

    private PlaylistManager spyPlaylistManager;

    @Before
    public void setup() {
        spyPlaylistManager = spy(playlistManager);
        ReflectionTestUtils.setField(spyPlaylistManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spyPlaylistManager, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(spyPlaylistManager, "mediaManager", mockMediaManager);
        ReflectionTestUtils.setField(spyPlaylistManager, "trackTableController", mockTrackTableController);

        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
    }

    @Test
    public void shouldSetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        spyPlaylistManager.setPlaylists(playlists);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");

        assertThat("Playlist map should have 4 entries", playlistMap.size(), equalTo(4));
    }

    @Test
    public void shouldGetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        List<Playlist> result = spyPlaylistManager.getPlaylists();

        assertThat("Playlist map should have 4 entries", result, hasSize(4));
        assertThat("Playlist map should be unmodifiable",
            result.getClass().isInstance(Collections.unmodifiableList(new ArrayList<Playlist>())), equalTo(true));
    }

    @Test
    public void shouldAddPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        Playlist playlist = new Playlist(999, "New Playlist", 10);

        spyPlaylistManager.addPlaylist(playlist);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");

        assertThat("Playlist map should have 5 entries", newPlaylistMap.size(), equalTo(5));
        assertThat("Playlist ID should be 2", playlist.getPlaylistId(), equalTo(2));
    }

    @Test
    public void shouldCreatePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.createPlaylist();

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist result = newPlaylistMap.get(2);

        assertThat("Playlist map should have 5 entries", newPlaylistMap.size(), equalTo(5));
        assertThat("Playlist map should contain a playlist with key of 2", result, notNullValue());
        assertThat("Playlist should have an ID of 2", result.getPlaylistId(), equalTo(2));
        assertThat("Playlist should have a name of '" + messageManager.getMessage(MESSAGE_PLAYLIST_DEFAULT) + "'",
            result.getName(), equalTo(messageManager.getMessage(MESSAGE_PLAYLIST_DEFAULT)));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, true);
    }

    @Test
    public void shouldCreatePlaylistFromAlbum() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getArtistName()).thenReturn("Artist");
        when(mockTrack.getAlbumName()).thenReturn("Album");

        List<Track> mockAlbum = new ArrayList<Track>();
        for (int i = 0; i < 10; i++) {
            mockAlbum.add(mock(Track.class));
        }

        when(mockSearchManager.getAlbumById(any())).thenReturn(mockAlbum);

        spyPlaylistManager.createPlaylistFromAlbum(mockTrack);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat("Playlist map should have 5 entries", newPlaylistMap.size(), equalTo(5));
        assertThat("Playlist map should contain a playlist with key of 2", playlist, notNullValue());
        assertThat("Playlist should have an ID of 2", playlist.getPlaylistId(), equalTo(2));
        assertThat("Playlist name should be 'Artist - Album'", playlist.getName(), equalTo("Artist - Album"));
        assertThat("Playlist should contain 10 tracks", playlist.getTracks(), hasSize(10));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, false);
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithNullTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        when(mockSearchManager.getAlbumById(any())).thenReturn(null);

        spyPlaylistManager.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat("Playlist map should have 4 entries", newPlaylistMap.size(), equalTo(4));
        assertThat("Playlist map should not contain a playlist with key of 2", playlist, nullValue());
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithEmptyTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        when(mockSearchManager.getAlbumById(any())).thenReturn(Collections.emptyList());

        spyPlaylistManager.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat("Playlist map should have 4 entries", newPlaylistMap.size(), equalTo(4));
        assertThat("Playlist map should not contain a playlist with key of 2", playlist, nullValue());
    }

    @Test
    public void shouldGetPlaylist() {
        Playlist playlist = spyPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH);

        assertThat("Playlist should not be null", playlist, notNullValue());
        assertThat("Playlist should have an ID of " + PLAYLIST_ID_SEARCH, playlist.getPlaylistId(),
            equalTo(PLAYLIST_ID_SEARCH));
    }

    @Test
    public void shouldDeletePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10), new Playlist(4, "Playlist 4", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.deletePlaylist(3);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");

        assertThat("Playlist map should have 4 entries", newPlaylistMap.size(), equalTo(4));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldNotDeleteAReservedPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.deletePlaylist(PLAYLIST_ID_FAVOURITES);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");

        assertThat("Playlist map should have 4 entries", newPlaylistMap.size(), equalTo(4));
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldSetPlaylistTracks() {
        List<Track> tracks = new ArrayList<Track>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        spyPlaylistManager.setPlaylistTracks(PLAYLIST_ID_FAVOURITES, tracks);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat("Playlist should have 10 tracks", playlist.getTracks(), hasSize(10));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldAddTrackToPlaylist() {
        spyPlaylistManager.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat("Playlist should have 1 track", playlist.getTracks(), hasSize(1));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldRemoveTrackFromPlaylist() {
        List<Track> tracks = new ArrayList<Track>();
        for (int i = 0; i < 10; i++) {
            tracks.add(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES,
            new Track(null, null, null, null, null, null, -1, Integer.toString(5), null, -1, null, false, null));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist newPlaylist = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat("Playlist should have 9 tracks", newPlaylist.getTracks(), hasSize(9));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }
    
    @Test
    public void shouldMoveTracksInPlaylist() {
        Track track1 = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        Track track2 = new Track(null, null, null, null, null, null, -1, "2", null, -1, null, false, null);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track1, track2));

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        spyPlaylistManager.moveTracksInPlaylist(PLAYLIST_ID_FAVOURITES, track1, track2);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        List<Track> tracks = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES).getTracks();

        assertThat("Tracks should have a size of 2", tracks, hasSize(2));
        assertThat("Track at position 0 should have ID of 2", tracks.get(0).getTrackId(), equalTo("2"));
        assertThat("Track at position 1 should have ID of 1", tracks.get(1).getTrackId(), equalTo("1"));
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES, track1);
    }
    
    @Test
    public void shouldReturnIfTrackIsInPlaylist() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        boolean result = spyPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, "1");
        
        assertThat("Track should be in playlist", result, equalTo(true));
    }
    
    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithNullTrackId() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        boolean result = spyPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, null);
        
        assertThat("Track should not be in playlist", result, equalTo(false));
    }
    
    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithUnknownPlaylistId() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        boolean result = spyPlaylistManager.isTrackInPlaylist(999, "1");
        
        assertThat("Track should not be in playlist", result, equalTo(false));
    }
    
    @Test
    public void shouldPlayPlaylist() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        
        List<Track> tracks = new ArrayList<Track>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 10);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        
        spyPlaylistManager.playPlaylist(PLAYLIST_ID_FAVOURITES);
        
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        
        assertThat("Current playlist ID should be " + PLAYLIST_ID_FAVOURITES, currentPlaylistId, equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(anyBoolean());
        verify(getMockEventManager(), times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }
    
    @Test
    public void shouldPlayTrack() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        
        Track track = mock(Track.class);
        when(track.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        when(track.getPlaylistIndex()).thenReturn(10);
        
        spyPlaylistManager.playTrack(track);
        
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        
        assertThat("Current playlist ID should be " + PLAYLIST_ID_FAVOURITES, currentPlaylistId, equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current playlist index should be 10", currentPlaylistIndex, equalTo(10));
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        
        verify(spyPlaylistManager, times(1)).playCurrentTrack(anyBoolean());
        verify(getMockEventManager(), never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }
    
    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverride() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }
        
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(false);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should not be null", currentTrack, notNullValue());
        assertThat("Current track ID should be 5", currentTrack.getTrackId(), equalTo(Integer.toString(currentPlaylistIndex)));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }
    
    @Test
    public void shouldPlayCurrentTrackShuffleNoOverride() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }
        
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(false);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should not be null", currentTrack, notNullValue());
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getShuffledTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }
    
    @Test
    public void shouldPlayCurrentTrackNoShuffleOverride() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }
        
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(true);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should not be null", currentTrack, notNullValue());
        assertThat("Current track ID should be 5", currentTrack.getTrackId(), equalTo(Integer.toString(currentPlaylistIndex)));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }
    
    @Test
    public void shouldPlayCurrentTrackShuffleOverride() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }
        
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(true);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should not be null", currentTrack, notNullValue());
        assertThat("Current track ID should be 5", currentTrack.getTrackId(), equalTo(Integer.toString(currentPlaylistIndex)));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(playingPlaylist, times(1)).setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }
    
    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverrideExistingPlaylist() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }
        
        Playlist spyPlaylist = spy(playlist);
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", spyPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(false);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should not be null", currentTrack, notNullValue());
        assertThat("Current track ID should be 5", currentTrack.getTrackId(), equalTo(Integer.toString(currentPlaylistIndex)));
        verify(playingPlaylist, never()).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }
    
    @Test
    public void shouldNotPlayCurrentTrackNoShuffleNoOverrideEmptyPlaylist() {
        doNothing().when(mockMediaManager).playTrack(any());
        
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        int currentPlaylistIndex = 5;
        
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        
        spyPlaylistManager.playCurrentTrack(false);
        
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "currentTrack");
        
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        assertThat("Playing playlist ID should be " + PLAYLIST_ID_FAVOURITES, playingPlaylist.getPlaylistId(), equalTo(PLAYLIST_ID_FAVOURITES));
        assertThat("Current track should be null", currentTrack, nullValue());
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
        verify(mockMediaManager, never()).playTrack(any());
    }
    
    @Test
    public void shouldPauseCurrentTrack() {
        doNothing().when(mockMediaManager).pausePlayback();
        
        spyPlaylistManager.pauseCurrentTrack();
        
        verify(mockMediaManager, times(1)).pausePlayback();
    }
    
    @Test
    public void shouldResumeCurrentTrack() {
        doNothing().when(spyPlaylistManager).playTrack(any());
        doNothing().when(mockMediaManager).resumePlayback();
        
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);
        
        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        
        spyPlaylistManager.resumeCurrentTrack();
        
        verify(spyPlaylistManager, never()).playTrack(any());
        verify(mockMediaManager, times(1)).resumePlayback();
    }
    
    @Test
    public void shouldResumeCurrentTrackWithoutSelectedTrack() {
        doNothing().when(spyPlaylistManager).playTrack(any());
        doNothing().when(mockMediaManager).resumePlayback();
        
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(null);
        
        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        
        spyPlaylistManager.resumeCurrentTrack();
        
        verify(spyPlaylistManager, never()).playTrack(any());
        verify(mockMediaManager, times(1)).resumePlayback();
    }
    
    @Test
    public void shouldResumeCurrentTrackWithDifferentCurrentTrack() {
        doNothing().when(spyPlaylistManager).playTrack(any());
        doNothing().when(mockMediaManager).resumePlayback();
        
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);
        
        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", new Track(null, null, null, null, null, null, -1, "2", null, -1, null, false, null));
        
        spyPlaylistManager.resumeCurrentTrack();
        
        verify(spyPlaylistManager, times(1)).playTrack(track);
        verify(mockMediaManager, never()).resumePlayback();
    }
    
    @Test
    public void shouldResumeCurrentTrackWithDifferentPlaylist() {
        doNothing().when(spyPlaylistManager).playTrack(any());
        doNothing().when(mockMediaManager).resumePlayback();
        
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);

        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null));
        
        spyPlaylistManager.resumeCurrentTrack();
        
        verify(spyPlaylistManager, times(1)).playTrack(track);
        verify(mockMediaManager, never()).resumePlayback();
    }
    
    @Test
    public void shouldRestartCurrentTrack() {
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());
        
        spyPlaylistManager.restartTrack();
        
        verify(mockMediaManager, times(1)).setSeekPositionPercent(0d);
    }
    
    @Test
    public void shouldPlayPreviousTrackWithNullPlaylist() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ONE);
        
        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 2", currentPlaylistIndex, equalTo(2));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ONE);
        
        boolean result = spyPlaylistManager.playPreviousTrack(true);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 4", currentPlaylistIndex, equalTo(4));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ALL);
        
        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 4", currentPlaylistIndex, equalTo(4));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 1", currentPlaylistIndex, equalTo(1));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithNullPlaylist() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Current playlist index should be 2", currentPlaylistIndex, equalTo(2));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ONE);
        
        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 2", currentPlaylistIndex, equalTo(2));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ONE);
        
        boolean result = spyPlaylistManager.playNextTrack(true);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ALL);
        
        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be true", result, equalTo(true));
        assertThat("Current playlist index should be 3", currentPlaylistIndex, equalTo(3));
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());
        doNothing().when(mockMediaManager).stopPlayback();
        doNothing().when(mockMediaManager).setSeekPositionPercent(anyDouble());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Result should be false", result, equalTo(false));
        assertThat("Current playlist index should be 4", currentPlaylistIndex, equalTo(4));
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }
    
    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistIndexNoShuffle() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(false);
        when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        when(playingPlaylist.getShuffledTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();
        
        assertThat("Track should not be null", track, notNullValue());
        verify(playingPlaylist, times(1)).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }
    
    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistIndexWithShuffle() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(false);
        when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        when(playingPlaylist.getShuffledTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        
        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();
        
        assertThat("Track should not be null", track, notNullValue());
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, times(1)).getShuffledTrackAtIndex(anyInt());
    }
    
    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithEmptyPlaylist() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(true);
        when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        when(playingPlaylist.getShuffledTrackAtIndex(anyInt())).thenReturn(mock(Track.class));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();
        
        assertThat("Track should be null", track, nullValue());
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }
    
    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithNullPlaylist() {
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();
        
        assertThat("Track should be null", track, nullValue());
    }
    
    @Test
    public void shouldClearSelectedTrack() {
        ReflectionTestUtils.setField(spyPlaylistManager, "selectedTrack", mock(Track.class));
        
        spyPlaylistManager.clearSelectedTrack();
        
        Track track = (Track)ReflectionTestUtils.getField(spyPlaylistManager, "selectedTrack");
        
        assertThat("Track should be null", track, nullValue());
    }
    
    @Test
    public void shouldSetShuffleNoIgnorePlaylistNoCurrentTrack() {
        when(mockMediaManager.isPaused()).thenReturn(false);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        spyPlaylistManager.setShuffle(true, false);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        
        assertThat("Shuffle should be true", shuffle, equalTo(true));
        assertThat("Playing playlist should be null", playingPlaylist, nullValue());
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mockMediaManager.isPaused()).thenReturn(true);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", mockTrack);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        spyPlaylistManager.setShuffle(true, false);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        
        assertThat("Shuffle should be true", shuffle, equalTo(true));
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mockMediaManager.isPaused()).thenReturn(false);
        when(mockMediaManager.isPlaying()).thenReturn(true);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", mockTrack);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        spyPlaylistManager.setShuffle(true, false);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        
        assertThat("Shuffle should be true", shuffle, equalTo(true));
        assertThat("Playing playlist should not be null", playingPlaylist, notNullValue());
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mockMediaManager.isPaused()).thenReturn(true);
        when(mockMediaManager.isPlaying()).thenReturn(false);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);
        
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", mockTrack);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        
        spyPlaylistManager.setShuffle(false, false);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Shuffle should be false", shuffle, equalTo(false));
        assertThat("Playing playlist should be null", playingPlaylist, nullValue());
        assertThat("Current playlist index should be 5", currentPlaylistIndex, equalTo(5));
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mockMediaManager.isPaused()).thenReturn(false);
        when(mockMediaManager.isPlaying()).thenReturn(true);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);
        
        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentTrack", mockTrack);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        
        spyPlaylistManager.setShuffle(false, false);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Shuffle should be false", shuffle, equalTo(false));
        assertThat("Playing playlist should be null", playingPlaylist, nullValue());
        assertThat("Current playlist index should be 5", currentPlaylistIndex, equalTo(5));
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetShuffleIgnorePlaylist() {
        when(mockMediaManager.isPaused()).thenReturn(false);
        when(mockMediaManager.isPlaying()).thenReturn(false);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", false);
        
        spyPlaylistManager.setShuffle(true, true);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Shuffle should be true", shuffle, equalTo(true));
        assertThat("Playing playlist should be null", playingPlaylist, nullValue());
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetNoShuffleIgnorePlaylist() {
        when(mockMediaManager.isPaused()).thenReturn(false);
        when(mockMediaManager.isPlaying()).thenReturn(false);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "playingPlaylist", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "shuffle", true);
        
        spyPlaylistManager.setShuffle(false, true);
        
        boolean shuffle = (Boolean)ReflectionTestUtils.getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist)ReflectionTestUtils.getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Shuffle should be false", shuffle, equalTo(false));
        assertThat("Playing playlist should be null", playingPlaylist, nullValue());
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }
    
    @Test
    public void shouldSetRepeat() {
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        spyPlaylistManager.setRepeat(Repeat.ALL);
        
        Repeat repeat = (Repeat)ReflectionTestUtils.getField(spyPlaylistManager, "repeat");
        
        assertThat("Repeat should be ALL", repeat, equalTo(Repeat.ALL));
    }
    
    @Test
    public void shouldUpdateRepeatFromOff() {
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.OFF);
        
        spyPlaylistManager.updateRepeat();
        
        Repeat repeat = (Repeat)ReflectionTestUtils.getField(spyPlaylistManager, "repeat");
        
        assertThat("Repeat should be ALL", repeat, equalTo(Repeat.ALL));
    }
    
    @Test
    public void shouldUpdateRepeatFromAll() {
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ALL);
        
        spyPlaylistManager.updateRepeat();
        
        Repeat repeat = (Repeat)ReflectionTestUtils.getField(spyPlaylistManager, "repeat");
        
        assertThat("Repeat should be ONE", repeat, equalTo(Repeat.ONE));
    }
    
    @Test
    public void shouldUpdateRepeatFromOne() {
        ReflectionTestUtils.setField(spyPlaylistManager, "repeat", Repeat.ONE);
        
        spyPlaylistManager.updateRepeat();
        
        Repeat repeat = (Repeat)ReflectionTestUtils.getField(spyPlaylistManager, "repeat");
        
        assertThat("Repeat should be OFF", repeat, equalTo(Repeat.OFF));
    }
    
    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenMediaPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(true);

        ReflectionTestUtils.setField(spyPlaylistManager, "selectedTrack", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getTrackId()).thenReturn("123");
        when(mockTrack.getPlaylistId()).thenReturn(456);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);
        
        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, mockTrack);
        
        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Selected track should not be null", selectedTrack, notNullValue());
        assertThat("Selected track should have a track ID of 123", selectedTrack.getTrackId(), equalTo("123"));
        assertThat("Current playlist ID should be 0", currentPlaylistId, equalTo(0));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
    }
    
    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenNoMediaPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(false);

        ReflectionTestUtils.setField(spyPlaylistManager, "selectedTrack", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        
        Track mockTrack = mock(Track.class);
        when(mockTrack.getTrackId()).thenReturn("123");
        when(mockTrack.getPlaylistId()).thenReturn(456);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);
        
        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, mockTrack);
        
        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Selected track should not be null", selectedTrack, notNullValue());
        assertThat("Selected track should have a track ID of 123", selectedTrack.getTrackId(), equalTo("123"));
        assertThat("Current playlist ID should be 456", currentPlaylistId, equalTo(456));
        assertThat("Current playlist index should be 5", currentPlaylistIndex, equalTo(5));
    }
    
    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadTrackIsNull() {
        ReflectionTestUtils.setField(spyPlaylistManager, "selectedTrack", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, (Object[])null);
        
        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Selected track should be null", selectedTrack, nullValue());
        assertThat("Current playlist ID should be 0", currentPlaylistId, equalTo(0));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
    }
    
    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadArrayIsEmpty() {
        ReflectionTestUtils.setField(spyPlaylistManager, "selectedTrack", null);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistId", 0);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, new Object[] {});
        
        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Selected track should be null", selectedTrack, nullValue());
        assertThat("Current playlist ID should be 0", currentPlaylistId, equalTo(0));
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
    }
    
    @Test
    public void shouldPlayNextTrackOnEndOfMediaEvent() {
        doReturn(true).when(spyPlaylistManager).playNextTrack(false);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 5);
        
        spyPlaylistManager.eventReceived(Event.END_OF_MEDIA, (Object[])null);
        
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Current playlist index should be 5", currentPlaylistIndex, equalTo(5));
    }
    
    @Test
    public void shouldNotPlayNextTrackOnEndOfMediaEventIfNoTracksLeftInPlaylist() {
        doReturn(false).when(spyPlaylistManager).playNextTrack(false);
        ReflectionTestUtils.setField(spyPlaylistManager, "currentPlaylistIndex", 5);
        
        spyPlaylistManager.eventReceived(Event.END_OF_MEDIA, (Object[])null);
        
        int currentPlaylistIndex = (Integer)ReflectionTestUtils.getField(spyPlaylistManager, "currentPlaylistIndex");
        
        assertThat("Current playlist index should be 0", currentPlaylistIndex, equalTo(0));
    }
}
