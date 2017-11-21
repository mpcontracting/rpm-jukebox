package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
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

import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class PlaylistManagerTest extends AbstractTest implements Constants {

    @Autowired
    private PlaylistManager playlistManager;
    
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
        ReflectionTestUtils.setField(spyPlaylistManager, "searchManager", mockSearchManager);
        ReflectionTestUtils.setField(spyPlaylistManager, "mediaManager", mockMediaManager);
        ReflectionTestUtils.setField(spyPlaylistManager, "trackTableController", mockTrackTableController);
        
        List<Playlist> playlists = Arrays.asList(
            new Playlist(PLAYLIST_ID_SEARCH, "Search", 10),
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10)
        );
        
        Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
    }
    
    @Test
    public void shouldSetPlaylists() {
        List<Playlist> playlists = Arrays.asList(
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10),
            new Playlist(3, "Playlist 3", 10)
        );
        
        spyPlaylistManager.setPlaylists(playlists);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager, "playlistMap");

        // Playlist map should already contain search and favourite playlists
        assertThat("Playlist map should have 4 entries", playlistMap.size(), equalTo(4));
    }
    
    @Test
    public void shouldGetPlaylists() {
        List<Playlist> playlists = Arrays.asList(
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10),
            new Playlist(3, "Playlist 3", 10)
        );
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        List<Playlist> result = spyPlaylistManager.getPlaylists();
        
        // Playlist map should already contain search and favourite playlists
        assertThat("Playlist map should have 4 entries", result, hasSize(4));
        assertThat("Playlist map should be unmodifiable", result.getClass().isInstance(Collections.unmodifiableList(new ArrayList<Playlist>())), equalTo(true));
    }
    
    @Test
    public void shouldAddPlaylist() {
        List<Playlist> playlists = Arrays.asList(
            new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
            new Playlist(1, "Playlist 1", 10),
            new Playlist(3, "Playlist 3", 10)
        );
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> playlistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager, "playlistMap");
        playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));
        
        ReflectionTestUtils.setField(spyPlaylistManager, "playlistMap", playlistMap);
        
        Playlist playlist = new Playlist(999, "New Playlist", 10);
        
        spyPlaylistManager.addPlaylist(playlist);
        
        @SuppressWarnings("unchecked")
        Map<Integer, Playlist> newPlaylistMap = (Map<Integer, Playlist>)ReflectionTestUtils.getField(spyPlaylistManager, "playlistMap");
        
        // Playlist map should already contain search and favourite playlists
        assertThat("Playlist map should have 5 entries", playlistMap.size(), equalTo(5));
        assertThat("Playlist ID should be 2", playlist.getPlaylistId(), equalTo(2));
    }
}
