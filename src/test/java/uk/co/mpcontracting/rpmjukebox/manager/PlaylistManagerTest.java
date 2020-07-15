package uk.co.mpcontracting.rpmjukebox.manager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getNonNullField;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistManagerTest implements Constants {

    @Mock
    private EventManager eventManager;

    @Mock
    private AppProperties appProperties;

    @Mock
    private MessageManager messageManager;

    @Mock
    private SearchManager searchManager;

    @Mock
    private MediaManager mediaManager;

    @Mock
    private TrackTableController trackTableController;

    private PlaylistManager underTest;

    @Before
    public void setup() {
        underTest = spy(new PlaylistManager(appProperties, messageManager));
        underTest.wireSearchManager(searchManager);
        underTest.wireMediaManager(mediaManager);
        underTest.wireTrackTableController(trackTableController);

        setField(underTest, "eventManager", eventManager);

        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
                new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10));

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        when(appProperties.getMaxPlaylistSize()).thenReturn(50);
        when(messageManager.getMessage(Constants.MESSAGE_PLAYLIST_DEFAULT)).thenReturn("New Playlist");
    }

    @Test
    public void shouldSetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        underTest.setPlaylists(playlists);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");

        assertThat(playlistMap).hasSize(4);
    }

    @Test
    public void shouldGetPlaylists() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        List<Playlist> result = underTest.getPlaylists();

        assertThat(playlistMap).hasSize(4);
        assertThat(result).isInstanceOf(Collections.unmodifiableList(new ArrayList<>()).getClass());
    }

    @Test
    public void shouldAddPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        Playlist playlist = new Playlist(999, "New Playlist", 10);

        underTest.addPlaylist(playlist);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getField(underTest,
                "playlistMap");

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
    }

    @Test
    public void shouldCreatePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        underTest.createPlaylist();

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
        assertThat(playlist.getName()).isEqualTo("New Playlist");
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, true);
    }

    @Test
    public void shouldCreatePlaylistFromAlbum() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        Track track = mock(Track.class);
        when(track.getArtistName()).thenReturn("Artist");
        when(track.getAlbumName()).thenReturn("Album");

        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        when(searchManager.getAlbumById(any())).thenReturn(of(tracks));

        underTest.createPlaylistFromAlbum(track);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(5);
        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(2);
        assertThat(playlist.getName()).isEqualTo("Artist - Album");
        assertThat(playlist.getTracks()).hasSize(10);
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CREATED, 2, false);
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithNullTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        when(searchManager.getAlbumById(any())).thenReturn(empty());

        underTest.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(4);
        assertThat(playlist).isNull();
    }

    @Test
    public void shouldNotCreatePlaylistFromAlbumWithEmptyTracks() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        when(searchManager.getAlbumById(any())).thenReturn(of(emptyList()));

        underTest.createPlaylistFromAlbum(mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = newPlaylistMap.get(2);

        assertThat(newPlaylistMap).hasSize(4);
        assertThat(playlist).isNull();
    }

    @Test
    public void shouldGetPlaylist() {
        Playlist playlist = underTest.getPlaylist(PLAYLIST_ID_SEARCH).orElse(null);

        assertThat(playlist).isNotNull();
        assertThat(playlist.getPlaylistId()).isEqualTo(PLAYLIST_ID_SEARCH);
    }

    @Test
    public void shouldDeletePlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10), new Playlist(4, "Playlist 4", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        underTest.deletePlaylist(3);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");

        assertThat(newPlaylistMap).hasSize(4);
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldNotDeleteAReservedPlaylist() {
        List<Playlist> playlists = Arrays.asList(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
                new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

        setField(underTest, "playlistMap", playlistMap);

        underTest.deletePlaylist(PLAYLIST_ID_FAVOURITES);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");

        assertThat(newPlaylistMap).hasSize(4);
        verify(eventManager, never()).fireEvent(Event.PLAYLIST_DELETED, 1);
    }

    @Test
    public void shouldSetPlaylistTracks() {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        underTest.setPlaylistTracks(PLAYLIST_ID_FAVOURITES, tracks);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(playlist.getTracks()).hasSize(10);
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldAddTrackToPlaylist() {
        underTest.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, mock(Track.class));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(playlist.getTracks()).hasSize(1);
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldRemoveTrackFromPlaylist() {
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(generateTrack(i));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);

        setField(underTest, "playlistMap", playlistMap);

        underTest.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, generateTrack(5));

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist newPlaylist = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES);

        assertThat(newPlaylist.getTracks()).hasSize(9);
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldMoveTracksInPlaylist() {
        Track track1 = generateTrack(1);
        Track track2 = generateTrack(2);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Arrays.asList(track1, track2));

        setField(underTest, "playlistMap", playlistMap);

        underTest.moveTracksInPlaylist(PLAYLIST_ID_FAVOURITES, track1, track2);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        List<Track> tracks = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES).getTracks();

        assertThat(tracks).hasSize(2);
        assertThat(tracks.get(0).getTrackId()).isEqualTo("7892");
        assertThat(tracks.get(1).getTrackId()).isEqualTo("7891");
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES,
                track1);
    }

    @Test
    public void shouldReturnIfTrackIsInPlaylist() {
        Track track = generateTrack(1);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Collections.singletonList(track));

        setField(underTest, "playlistMap", playlistMap);

        boolean result = underTest.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, "7891");

        assertThat(result).isTrue();
    }

    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithNullTrackId() {
        Track track = generateTrack(1);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Collections.singletonList(track));

        setField(underTest, "playlistMap", playlistMap);

        boolean result = underTest.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, null);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldReturnFalseIfTrackIsInPlaylistWithUnknownPlaylistId() {
        Track track = generateTrack(1);

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(Collections.singletonList(track));

        setField(underTest, "playlistMap", playlistMap);

        boolean result = underTest.isTrackInPlaylist(999, "1");

        assertThat(result).isFalse();
    }

    @Test
    public void shouldPlayPlaylist() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tracks.add(mock(Track.class));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>) getNonNullField(underTest, "playlistMap");
        Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
        playlist.setTracks(tracks);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistIndex", 10);
        setField(underTest, "playingPlaylist", null);

        underTest.playPlaylist(PLAYLIST_ID_FAVOURITES);

        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");
        Playlist playingPlaylist = underTest.getPlayingPlaylist();

        assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentPlaylistIndex).isEqualTo(0);
        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        verify(underTest, times(1)).playCurrentTrack(anyBoolean());
        verify(eventManager, times(1)).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldPlayTrack() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Track track = mock(Track.class);
        when(track.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
        when(track.getPlaylistIndex()).thenReturn(10);

        underTest.playTrack(track);

        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");
        Playlist playingPlaylist = underTest.getPlayingPlaylist();

        assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentPlaylistIndex).isEqualTo(10);
        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        verify(underTest, times(1)).playCurrentTrack(anyBoolean());
        verify(eventManager, never()).fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverride() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            originalPlaylist.addTrack(generateTrack(i));
        }

        Playlist playlist = spy(originalPlaylist);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", false);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(false);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo("789" + currentPlaylistIndex);
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackShuffleNoOverride() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            originalPlaylist.addTrack(generateTrack(i));
        }

        Playlist playlist = spy(originalPlaylist);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", true);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(false);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getShuffledTrackAtIndex(currentPlaylistIndex);
        verify(mediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleOverride() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            originalPlaylist.addTrack(generateTrack(i));
        }

        Playlist playlist = spy(originalPlaylist);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", false);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(true);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo("789" + currentPlaylistIndex);
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackShuffleOverride() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            originalPlaylist.addTrack(generateTrack(i));
        }

        Playlist playlist = spy(originalPlaylist);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", true);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(true);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo("789" + currentPlaylistIndex);
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(playingPlaylist, times(1)).setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
        verify(mediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldPlayCurrentTrackNoShuffleNoOverrideExistingPlaylist() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);

        for (int i = 0; i < 10; i++) {
            originalPlaylist.addTrack(generateTrack(i));
        }

        Playlist playlist = spy(originalPlaylist);
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", false);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", playlist);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(false);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNotNull();
        assertThat(currentTrack.getTrackId()).isEqualTo("789" + currentPlaylistIndex);
        verify(playingPlaylist, never()).clone();
        verify(playingPlaylist, times(1)).getTrackAtIndex(currentPlaylistIndex);
        verify(mediaManager, times(1)).playTrack(currentTrack);
    }

    @Test
    public void shouldNotPlayCurrentTrackNoShuffleNoOverrideEmptyPlaylist() {
        Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
        Playlist playlist = spy(originalPlaylist);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);

        int currentPlaylistIndex = 5;

        setField(underTest, "shuffle", false);
        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentTrack", null);

        underTest.playCurrentTrack(false);

        Playlist playingPlaylist = underTest.getPlayingPlaylist();
        Track currentTrack = (Track) getField(underTest, "currentTrack");

        assertThat(playingPlaylist).isNotNull();
        assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
        assertThat(currentTrack).isNull();
        verify(playingPlaylist, times(1)).clone();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
        verify(mediaManager, never()).playTrack(any());
    }

    @Test
    public void shouldPauseCurrentTrack() {
        underTest.pauseCurrentTrack();

        verify(mediaManager, times(1)).pausePlayback();
    }

    @Test
    public void shouldResumeCurrentTrack() {
        Track track = generateTrack(1);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(trackTableController.getSelectedTrack()).thenReturn(track);

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        setField(underTest, "playingPlaylist", playingPlaylist);

        underTest.resumeCurrentTrack();

        verify(underTest, never()).playTrack(any());
        verify(mediaManager, times(1)).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithoutSelectedTrack() {
        Track track = generateTrack(1);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(trackTableController.getSelectedTrack()).thenReturn(null);

        setField(underTest, "playingPlaylist", mock(Playlist.class));

        underTest.resumeCurrentTrack();

        verify(underTest, never()).playTrack(any());
        verify(mediaManager, times(1)).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithDifferentCurrentTrack() {
        doNothing().when(underTest).playTrack(any());

        Track track = generateTrack(1);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(trackTableController.getSelectedTrack()).thenReturn(track);

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentTrack", generateTrack(2));

        underTest.resumeCurrentTrack();

        verify(underTest, times(1)).playTrack(track);
        verify(mediaManager, never()).resumePlayback();
    }

    @Test
    public void shouldResumeCurrentTrackWithDifferentPlaylist() {
        doNothing().when(underTest).playTrack(any());

        Track track = generateTrack(1);
        track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
        when(trackTableController.getSelectedTrack()).thenReturn(track);

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentTrack", generateTrack(1));

        underTest.resumeCurrentTrack();

        verify(underTest, times(1)).playTrack(track);
        verify(mediaManager, never()).resumePlayback();
    }

    @Test
    public void shouldRestartCurrentTrack() {
        underTest.restartTrack();

        verify(mediaManager, times(1)).setSeekPositionPercent(0d);
    }

    @Test
    public void shouldPlayPreviousTrackWithNullPlaylist() {
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, times(1)).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        setField(underTest, "playingPlaylist", mock(Playlist.class));
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.ONE);

        boolean result = underTest.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "repeat", Repeat.ONE);

        boolean result = underTest.playPreviousTrack(true);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "repeat", Repeat.ALL);

        boolean result = underTest.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        setField(underTest, "playingPlaylist", mock(Playlist.class));
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(1);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        setField(underTest, "playingPlaylist", mock(Playlist.class));
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playPreviousTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, times(1)).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNullPlaylist() {
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, times(1)).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
        setField(underTest, "playingPlaylist", mock(Playlist.class));
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.ONE);

        boolean result = underTest.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(2);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, times(1)).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 4);
        setField(underTest, "repeat", Repeat.ONE);

        boolean result = underTest.playNextTrack(true);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 4);
        setField(underTest, "repeat", Repeat.ALL);

        boolean result = underTest.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        doNothing().when(underTest).playCurrentTrack(anyBoolean());

        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 2);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isTrue();
        assertThat(currentPlaylistIndex).isEqualTo(3);
        verify(underTest, times(1)).playCurrentTrack(false);
        verify(mediaManager, never()).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.size()).thenReturn(5);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "currentPlaylistIndex", 4);
        setField(underTest, "repeat", Repeat.OFF);

        boolean result = underTest.playNextTrack(false);
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(result).isFalse();
        assertThat(currentPlaylistIndex).isEqualTo(4);
        verify(underTest, never()).playCurrentTrack(false);
        verify(mediaManager, times(1)).stopPlayback();
        verify(mediaManager, never()).setSeekPositionPercent(anyDouble());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylist() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(false);
        when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "shuffle", false);

        Track track = underTest.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNotNull();
        verify(playingPlaylist, times(1)).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithEmptyPlaylist() {
        Playlist playingPlaylist = mock(Playlist.class);
        when(playingPlaylist.isEmpty()).thenReturn(true);

        setField(underTest, "playingPlaylist", playingPlaylist);
        setField(underTest, "shuffle", false);

        Track track = underTest.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNull();
        verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
        verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    }

    @Test
    public void shouldGetTrackAtCurrentPlayingPlaylistWithNullPlaylist() {
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", false);

        Track track = underTest.getTrackAtPlayingPlaylistIndex();

        assertThat(track).isNull();
    }

    @Test
    public void shouldClearSelectedTrack() {
        setField(underTest, "selectedTrack", mock(Track.class));

        underTest.clearSelectedTrack();

        Track track = (Track) getField(underTest, "selectedTrack");

        assertThat(track).isNull();
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistNoCurrentTrack() {
        Playlist mockPlaylist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentTrack", null);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", false);

        underTest.setShuffle(true, false);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNull();
        verify(mockPlaylist, times(1)).shuffle();
        verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mediaManager.isPaused()).thenReturn(true);
        when(mediaManager.isPlaying()).thenReturn(false);

        Track track = mock(Track.class);

        Playlist playlist = mock(Playlist.class);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentTrack", track);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", false);

        underTest.setShuffle(true, false);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNotNull();
        verify(playlist, times(1)).shuffle();
        verify(playlist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mediaManager.isPlaying()).thenReturn(true);

        Track track = mock(Track.class);

        Playlist playlist = mock(Playlist.class);
        when(playlist.clone()).thenReturn(playlist);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentTrack", track);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", false);

        underTest.setShuffle(true, false);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNotNull();
        verify(playlist, times(1)).shuffle();
        verify(playlist, times(1)).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
        when(mediaManager.isPaused()).thenReturn(true);
        when(mediaManager.isPlaying()).thenReturn(false);

        Track track = mock(Track.class);
        when(track.getPlaylistIndex()).thenReturn(5);

        Playlist playlist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "currentTrack", track);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", true);

        underTest.setShuffle(false, false);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(5);
        verify(playlist, never()).shuffle();
        verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
        when(mediaManager.isPlaying()).thenReturn(true);

        Track track = mock(Track.class);
        when(track.getPlaylistIndex()).thenReturn(5);

        Playlist playlist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "currentTrack", track);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", true);

        underTest.setShuffle(false, false);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(5);
        verify(playlist, never()).shuffle();
        verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetShuffleIgnorePlaylist() {
        Playlist playlist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", false);

        underTest.setShuffle(true, true);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) getField(underTest, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(shuffle).isTrue();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(playlist, never()).shuffle();
        verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetNoShuffleIgnorePlaylist() {
        Playlist playlist = mock(Playlist.class);

        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

        setField(underTest, "playlistMap", playlistMap);
        setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
        setField(underTest, "currentPlaylistIndex", 0);
        setField(underTest, "playingPlaylist", null);
        setField(underTest, "shuffle", true);

        underTest.setShuffle(false, true);

        boolean shuffle = (Boolean) getNonNullField(underTest, "shuffle");
        Playlist playingPlaylist = (Playlist) ReflectionTestUtils.getField(underTest, "playingPlaylist");
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(shuffle).isFalse();
        assertThat(playingPlaylist).isNull();
        assertThat(currentPlaylistIndex).isEqualTo(0);
        verify(playlist, never()).shuffle();
        verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
    }

    @Test
    public void shouldSetRepeat() {
        setField(underTest, "repeat", Repeat.OFF);

        underTest.setRepeat(Repeat.ALL);

        Repeat repeat = (Repeat) getField(underTest, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ALL);
    }

    @Test
    public void shouldUpdateRepeatFromOff() {
        setField(underTest, "repeat", Repeat.OFF);

        underTest.updateRepeat();

        Repeat repeat = (Repeat) getField(underTest, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ALL);
    }

    @Test
    public void shouldUpdateRepeatFromAll() {
        setField(underTest, "repeat", Repeat.ALL);

        underTest.updateRepeat();

        Repeat repeat = (Repeat) getField(underTest, "repeat");

        assertThat(repeat).isEqualTo(Repeat.ONE);
    }

    @Test
    public void shouldUpdateRepeatFromOne() {
        setField(underTest, "repeat", Repeat.ONE);

        underTest.updateRepeat();

        Repeat repeat = (Repeat) getField(underTest, "repeat");

        assertThat(repeat).isEqualTo(Repeat.OFF);
    }

    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenMediaPlaying() {
        when(mediaManager.isPlaying()).thenReturn(true);

        setField(underTest, "selectedTrack", null);
        setField(underTest, "currentPlaylistId", 0);
        setField(underTest, "currentPlaylistIndex", 0);

        Track track = mock(Track.class);
        when(track.getTrackId()).thenReturn("123");

        underTest.eventReceived(Event.TRACK_SELECTED, track);

        Track selectedTrack = underTest.getSelectedTrack();
        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(selectedTrack).isNotNull();
        assertThat(selectedTrack.getTrackId()).isEqualTo("123");
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldSelectTrackOnTrackSelectedEventWhenNoMediaPlaying() {
        when(mediaManager.isPlaying()).thenReturn(false);

        setField(underTest, "selectedTrack", null);
        setField(underTest, "currentPlaylistId", 0);
        setField(underTest, "currentPlaylistIndex", 0);

        Track track = mock(Track.class);
        when(track.getTrackId()).thenReturn("123");
        when(track.getPlaylistId()).thenReturn(456);
        when(track.getPlaylistIndex()).thenReturn(5);

        underTest.eventReceived(Event.TRACK_SELECTED, track);

        Track selectedTrack = underTest.getSelectedTrack();
        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(selectedTrack).isNotNull();
        assertThat(selectedTrack.getTrackId()).isEqualTo("123");
        assertThat(currentPlaylistId).isEqualTo(456);
        assertThat(currentPlaylistIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadTrackIsNull() {
        setField(underTest, "selectedTrack", null);
        setField(underTest, "currentPlaylistId", 0);
        setField(underTest, "currentPlaylistIndex", 0);

        underTest.eventReceived(Event.TRACK_SELECTED, (Object[]) null);

        Track selectedTrack = underTest.getSelectedTrack();
        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(selectedTrack).isNull();
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldNotSelectTrackOnTrackSelectedEventIfPayloadArrayIsEmpty() {
        setField(underTest, "selectedTrack", null);
        setField(underTest, "currentPlaylistId", 0);
        setField(underTest, "currentPlaylistIndex", 0);

        underTest.eventReceived(Event.TRACK_SELECTED);

        Track selectedTrack = underTest.getSelectedTrack();
        int currentPlaylistId = underTest.getCurrentPlaylistId();
        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(selectedTrack).isNull();
        assertThat(currentPlaylistId).isEqualTo(0);
        assertThat(currentPlaylistIndex).isEqualTo(0);
    }

    @Test
    public void shouldPlayNextTrackOnEndOfMediaEvent() {
        doReturn(true).when(underTest).playNextTrack(false);
        setField(underTest, "currentPlaylistIndex", 5);

        underTest.eventReceived(Event.END_OF_MEDIA, (Object[]) null);

        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(currentPlaylistIndex).isEqualTo(5);
    }

    @Test
    public void shouldNotPlayNextTrackOnEndOfMediaEventIfNoTracksLeftInPlaylist() {
        doReturn(false).when(underTest).playNextTrack(false);
        setField(underTest, "currentPlaylistIndex", 5);

        underTest.eventReceived(Event.END_OF_MEDIA, (Object[]) null);

        int currentPlaylistIndex = (Integer) getNonNullField(underTest, "currentPlaylistIndex");

        assertThat(currentPlaylistIndex).isEqualTo(0);
    }
}
