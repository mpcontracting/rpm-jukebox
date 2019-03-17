package uk.co.mpcontracting.rpmjukebox.manager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistManagerTest implements Constants {

    @Mock
    private EventManager mockEventManager;

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private MessageManager mockMessageManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private MediaManager mockMediaManager;

    @Mock
    private TrackTableController mockTrackTableController;

    private PlaylistManager spyPlaylistManager;

    @Before
    public void setup() {
        spyPlaylistManager = spy(new PlaylistManager(mockAppProperties, mockMessageManager));
        spyPlaylistManager.wireSearchManager(mockSearchManager);
        spyPlaylistManager.wireMediaManager(mockMediaManager);
        spyPlaylistManager.wireTrackTableController(mockTrackTableController);

        setField(spyPlaylistManager, "eventManager", mockEventManager);

        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        when(mockAppProperties.getMaxPlaylistSize()).thenReturn(50);
        when(mockMessageManager.getMessage(Constants.MESSAGE_PLAYLIST_DEFAULT)).thenReturn("New Playlist");
    }

    @Test
    public void shouldSetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        spyPlaylistManager.setPlaylists(playlists);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");

        assertThat(playlistMap).hasSize(4);
    }

    @Test
    public void shouldGetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        List<Playlist> result = spyPlaylistManager.getPlaylists();

        assertThat(playlistMap).hasSize(4);
        assertThat(result).isInstanceOf(Collections.unmodifiableList(new ArrayList<>()).getClass());
    }

    @Test
    public void shouldAddPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        Playlist playlist = new Playlist(999, "New Playlist", 10);

        spyPlaylistManager.addPlaylist(playlist);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
    }

    @Test
    public void shouldCreatePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.createPlaylist();

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
        assertThat(playlist.getName()).isEqualTo("New Playlist");
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, true);
    }

    @Test
    public void shouldCreatePlaylistFromAlbum() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getArtistName()).thenReturn("Artist");
        when(mockTrack.getAlbumName()).thenReturn("Album");

        List<Track> mockAlbum = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mockAlbum.add(mock(Track.class));
        }

        when(mockSearchManager.getAlbumById(any())).thenReturn(mockAlbum);

        spyPlaylistManager.createPlaylistFromAlbum(mockTrack);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
        assertThat(playlist.getName()).isEqualTo("Artist - Album");
        assertThat(playlist.getTracks()).hasSize(10);
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, false);
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithNullTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        when(mockSearchManager.getAlbumById(any())).thenReturn(null);

        spyPlaylistManager.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(4);
        assertThat(playlist).isNull();
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithEmptyTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        when(mockSearchManager.getAlbumById(any())).thenReturn(Collections.emptyList());

        spyPlaylistManager.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(4);
        assertThat(playlist).isNull();
    }

    @Test
    public void shouldGetPlaylist() {
        Playlist playlist = spyPlaylistManager.getPlaylist(PLAYLIST_ID_SEARCH);

        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldDeletePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10), new Playlist(4, "Playlist 4", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.deletePlaylist(3);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");

        assertThat(newPlaylistMap).hasSize(4);
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldNotDeleteAReservedPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.deletePlaylist(PLAYLIST_ID_FAVOURITES);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");

        assertThat(newPlaylistMap).hasSize(4);
        verify(mockEventManager, never()).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldSetPlaylistTracks() {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        spyPlaylistManager.setPlaylistTracks(PLAYLIST_ID_FAVOURITES, tracks);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(playlist.getTracks()).hasSize(10);
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldAddTrackToPlaylist() {
        spyPlaylistManager.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(playlist.getTracks()).hasSize(1);
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldRemoveTrackFromPlaylist() {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES,
            new Track(null, null, null, null, null, null, -1, Integer.toString(5), null, -1, null, false, null));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist newPlaylist = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(playlist.getTracks()).hasSize(9);
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldMoveTracksInPlaylist() {
        Track track1 = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        Track track2 = new Track(null, null, null, null, null, null, -1, "2", null, -1, null, false, null);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track1, track2));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        spyPlaylistManager.moveTracksInPlaylist(PLAYLIST_ID_FAVOURITES, track1, track2);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        List<Track> tracks = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES).getTracks();

        assertThat(tracks).hasSize(2);
        assertThat(tracks.get(0).getTrackId()).isEqualTo("2");
        assertThat(tracks.get(1).getTrackId()).isEqualTo("1");
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES,
            track1);
    }

    @Test
    public void shouldReturnIfTrackIsInPlaylist() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        boolean result = spyPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, "1");

        assertThat(result).isTrue();
    }

    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithNullTrackId() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        boolean result = spyPlaylistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, null);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithUnknownPlaylistId() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track));

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        boolean result = spyPlaylistManager.isTrackInPlaylist(999, "1");

        assertThat(result).isFalse();
    }

    @Test
    public void shouldPlayPlaylist() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getField(spyPlaylistManager,
            "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistIndex", 10);
        setField(spyPlaylistManager, "playingPlaylist", null);

        spyPlaylistManager.playPlaylist(PLAYLIST_ID_FAVOURITES);

        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();

        assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentPlaylistIndex).isEqualTo(0);
        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(anyBoolean());
        verify(mockEventManager, times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldPlayTrack() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Track track = mock(Track.class);
        when(track.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        when(track.getPlaylistIndex()).thenReturn(10);

        spyPlaylistManager.playTrack(track);

        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");
        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();

        assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentPlaylistIndex).isEqualTo(10);
        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(anyBoolean());
        verify(mockEventManager, never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverride() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", false);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(false);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo(Integer.toString(currentPlaylistIndex));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackShuffleNoOverride() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", true);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(false);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getShuffledTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleOverride() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", false);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(true);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo(Integer.toString(currentPlaylistIndex));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackShuffleOverride() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", true);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(true);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo(Integer.toString(currentPlaylistIndex));
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(playingPlaylist, times(1)).setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverrideExistingPlaylist() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            playlist.addTrack(
                new Track(null, null, null, null, null, null, -1, Integer.toString(i), null, -1, null, false, null));
        }

        Playlist spyPlaylist = spy(playlist);
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", false);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", spyPlaylist);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(false);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo(Integer.toString(currentPlaylistIndex));
        verify(playingPlaylist, never()).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mockMediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldNotPlayCurrentTrackNoShuffleNoOverrideEmptyPlaylist() {
        Playlist playlist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        Playlist spyPlaylist = spy(playlist);
        when(spyPlaylist.clone()).thenReturn(spyPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, spyPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(spyPlaylistManager, "shuffle", false);
        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", currentPlaylistIndex);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentTrack", null);

        spyPlaylistManager.playCurrentTrack(false);

        Playlist playingPlaylist = spyPlaylistManager.getPlayingPlaylist();
        Track currentTrack = (Track) getField(spyPlaylistManager, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNull();
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
        verify(mockMediaManager, never()).playTrack(any());
    }

    @Test
    public void shouldPauseCurrentTrack() {
        spyPlaylistManager.pauseCurrentTrack();

        verify(mockMediaManager, times(1)).pausePlayback();
    }

    @Test
    public void shouldResumeCurrentTrack() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);

        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);

        spyPlaylistManager.resumeCurrentTrack();

        verify(spyPlaylistManager, never()).playTrack(any());
        verify(mockMediaManager, times(1)).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithoutSelectedTrack() {
        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(null);

        setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));

        spyPlaylistManager.resumeCurrentTrack();

        verify(spyPlaylistManager, never()).playTrack(any());
        verify(mockMediaManager, times(1)).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithDifferentCurrentTrack() {
        doNothing().when(spyPlaylistManager).playTrack(any());

        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);

        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        setField(spyPlaylistManager, "currentTrack",
            new Track(null, null, null, null, null, null, -1, "2", null, -1, null, false, null));

        spyPlaylistManager.resumeCurrentTrack();

        verify(spyPlaylistManager, times(1)).playTrack(track);
        verify(mockMediaManager, never()).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithDifferentPlaylist() {
        doNothing().when(spyPlaylistManager).playTrack(any());

        Track track = new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(mockTrackTableController.getSelectedTrack()).thenReturn(track);

        Playlist mockPlayingPlaylist = mock(Playlist.class);
        when(mockPlayingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        setField(spyPlaylistManager, "playingPlaylist", mockPlayingPlaylist);
        setField(spyPlaylistManager, "currentTrack",
            new Track(null, null, null, null, null, null, -1, "1", null, -1, null, false, null));

        spyPlaylistManager.resumeCurrentTrack();

        verify(spyPlaylistManager, times(1)).playTrack(track);
        verify(mockMediaManager, never()).resumePlayback();
    }

    @Test
    public void shouldRestartCurrentTrack() {
        spyPlaylistManager.restartTrack();

        verify(mockMediaManager, times(1)).setSeekPositionPercent(0d);
    }

    @Test
    public void shouldPlayPreviousTrackWithNullPlaylist() {
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.ONE);

        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "repeat", Repeat.ONE);

        boolean result = spyPlaylistManager.playPreviousTrack(true);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "repeat", Repeat.ALL);

        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(1);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNullPlaylist() {
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        setField(spyPlaylistManager, "playingPlaylist", mock(Playlist.class));
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.ONE);

        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        setField(spyPlaylistManager, "repeat", Repeat.ONE);

        boolean result = spyPlaylistManager.playNextTrack(true);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        setField(spyPlaylistManager, "repeat", Repeat.ALL);

        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(spyPlaylistManager).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 2);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(3);
        verify(spyPlaylistManager, times(1)).playCurrentTrack(false);
        verify(mockMediaManager, never()).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "currentPlaylistIndex", 4);
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        boolean result = spyPlaylistManager.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(spyPlaylistManager, never()).playCurrentTrack(false);
        verify(mockMediaManager, times(1)).stopPlayback();
        verify(mockMediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistIndexNoShuffle() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(false);
        when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "shuffle", false);

        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNotNull();
        verify(playingPlaylist, times(1)).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistIndexWithShuffle() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(false);
        when(playingPlaylist.getShuffledTrackAtIndex(anyInt())).thenReturn(mock(Track.class));

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "shuffle", true);

        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNotNull();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, times(1)).getShuffledTrackAtIndex(anyInt());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithEmptyPlaylist() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(true);

        setField(spyPlaylistManager, "playingPlaylist", playingPlaylist);
        setField(spyPlaylistManager, "shuffle", false);

        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNull();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithNullPlaylist() {
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", false);

        Track track = spyPlaylistManager.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNull();
    }

    @Test
    public void shouldClearSelectedTrack() {
        setField(spyPlaylistManager, "selectedTrack", mock(Track.class));

        spyPlaylistManager.clearSelectedTrack();

        Track track = (Track) getField(spyPlaylistManager, "selectedTrack");

        assertThat(track).isNull();
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistNoCurrentTrack() {
        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentTrack", null);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", false);

        spyPlaylistManager.setShuffle(true, false);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNull();
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mockMediaManager.isPaused()).thenReturn(true);
        when(mockMediaManager.isPlaying()).thenReturn(false);

        Track mockTrack = mock(Track.class);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentTrack", mockTrack);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", false);

        spyPlaylistManager.setShuffle(true, false);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNotNull();
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(true);

        Track mockTrack = mock(Track.class);

        Playlist mockPlaylist = mock(Playlist.class);
        when(mockPlaylist.clone()).thenReturn(mockPlaylist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentTrack", mockTrack);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", false);

        spyPlaylistManager.setShuffle(true, false);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNotNull();
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mockMediaManager.isPaused()).thenReturn(true);
        when(mockMediaManager.isPlaying()).thenReturn(false);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);

        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "currentTrack", mockTrack);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", true);

        spyPlaylistManager.setShuffle(false, false);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(5);
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(true);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);

        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "currentTrack", mockTrack);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", true);

        spyPlaylistManager.setShuffle(false, false);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(5);
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleIgnorePlaylist() {
        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", false);

        spyPlaylistManager.setShuffle(true, true);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleIgnorePlaylist() {
        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(spyPlaylistManager, "playlistMap", playlistMap);
        setField(spyPlaylistManager, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);
        setField(spyPlaylistManager, "playingPlaylist", null);
        setField(spyPlaylistManager, "shuffle", true);

        spyPlaylistManager.setShuffle(false, true);

        boolean shuffle = (Boolean) getField(spyPlaylistManager, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(spyPlaylistManager, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(mockPlaylist, never()).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetRepeat() {
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        spyPlaylistManager.setRepeat(Repeat.ALL);

        Repeat repeat = (Repeat) getField(spyPlaylistManager, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ALL);
    }

    @Test
    public void shouldUpdateRepeatFromOff() {
        setField(spyPlaylistManager, "repeat", Repeat.OFF);

        spyPlaylistManager.updateRepeat();

        Repeat repeat = (Repeat) getField(spyPlaylistManager, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ALL);
    }

    @Test
    public void shouldUpdateRepeatFromAll() {
        setField(spyPlaylistManager, "repeat", Repeat.ALL);

        spyPlaylistManager.updateRepeat();

        Repeat repeat = (Repeat) getField(spyPlaylistManager, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ONE);
    }

    @Test
    public void shouldUpdateRepeatFromOne() {
        setField(spyPlaylistManager, "repeat", Repeat.ONE);

        spyPlaylistManager.updateRepeat();

        Repeat repeat = (Repeat) getField(spyPlaylistManager, "repeat");

        assertThat(repeat).isEqualTo(Repeat.OFF);
    }

    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenMediaPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(true);

        setField(spyPlaylistManager, "selectedTrack", null);
        setField(spyPlaylistManager, "currentPlaylistId", 0);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getTrackId()).thenReturn("123");

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, mockTrack);

        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(selectedTrack).isNotNull();
        assertThat(selectedTrack.getTrackId()).isEqualTo("123");
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenNoMediaPlaying() {
        when(mockMediaManager.isPlaying()).thenReturn(false);

        setField(spyPlaylistManager, "selectedTrack", null);
        setField(spyPlaylistManager, "currentPlaylistId", 0);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        Track mockTrack = mock(Track.class);
        when(mockTrack.getTrackId()).thenReturn("123");
        when(mockTrack.getPlaylistId()).thenReturn(456);
        when(mockTrack.getPlaylistIndex()).thenReturn(5);

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, mockTrack);

        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(selectedTrack).isNotNull();
        assertThat(selectedTrack.getTrackId()).isEqualTo("123");
        assertThat(currentPlaylistId).isEqualTo(456);
        assertThat(currentPlaylistIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadTrackIsNull() {
        setField(spyPlaylistManager, "selectedTrack", null);
        setField(spyPlaylistManager, "currentPlaylistId", 0);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, (Object[])null);

        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(selectedTrack).isNull();
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadArrayIsEmpty() {
        setField(spyPlaylistManager, "selectedTrack", null);
        setField(spyPlaylistManager, "currentPlaylistId", 0);
        setField(spyPlaylistManager, "currentPlaylistIndex", 0);

        spyPlaylistManager.eventReceived(Event.TRACK_SELECTED, new Object[] {});

        Track selectedTrack = spyPlaylistManager.getSelectedTrack();
        int currentPlaylistId = spyPlaylistManager.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(selectedTrack).isNull();
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldPlayNextTrackOnEndOfMediaEvent() {
        doReturn(true).when(spyPlaylistManager).playNextTrack(false);
        setField(spyPlaylistManager, "currentPlaylistIndex", 5);

        spyPlaylistManager.eventReceived(Event.END_OF_MEDIA, (Object[])null);

        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(currentPlaylistIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotPlayNextTrackOnEndOfMediaEventIfNoTracksLeftInPlaylist() {
        doReturn(false).when(spyPlaylistManager).playNextTrack(false);
        setField(spyPlaylistManager, "currentPlaylistIndex", 5);

        spyPlaylistManager.eventReceived(Event.END_OF_MEDIA, (Object[])null);

        int currentPlaylistIndex = (Integer) getField(spyPlaylistManager, "currentPlaylistIndex");

        assertThat(currentPlaylistIndex).isEqualTo(0);
    }
}
