package uk.co.mpcontracting.rpmjukebox.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class PlaylistManager extends EventAwareObject implements InitializingBean, Constants {
	
	@Autowired
	private MediaManager mediaManager;

	private Map<Integer, Playlist> playlistMap;
	
	private Integer currentPlaylistId;
	private Integer currentPlaylistIndex;
	private Track currentTrack;
	private boolean repeat;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising PlaylistManager");
		
		playlistMap = new LinkedHashMap<Integer, Playlist>();
		playlistMap.put(SEARCH_PLAYLIST_ID, new Playlist(SEARCH_PLAYLIST_ID, "Search Results"));
		currentPlaylistId = SEARCH_PLAYLIST_ID;
		currentPlaylistIndex = 0;
		repeat = false;
	}
	
	public List<Playlist> getPlaylists() {
		log.info("Getting playlists");

		List<Playlist> playlists = new ArrayList<Playlist>();

		synchronized (playlistMap) {
			for (Playlist playlist : playlistMap.values()) {
				playlists.add(playlist);
			}
		}

		return Collections.unmodifiableList(playlists);
	}

	@Override
	public void eventReceived(Event event, Object... payload) {
		
	}
}
