package uk.co.mpcontracting.rpmjukebox.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
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
	
	@Getter private int currentPlaylistId;
	@Getter private int currentPlaylistIndex;
	private Track currentTrack;
	@Getter private boolean shuffle;
	@Getter private boolean repeat;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising PlaylistManager");
		
		playlistMap = new LinkedHashMap<Integer, Playlist>();
		playlistMap.put(SEARCH_PLAYLIST_ID, new Playlist(SEARCH_PLAYLIST_ID, "Search Results"));
		playlistMap.put(FAVOURITES_PLAYLIST_ID, new Playlist(FAVOURITES_PLAYLIST_ID, "Favourites"));
		currentPlaylistId = SEARCH_PLAYLIST_ID;
		currentPlaylistIndex = 0;
		shuffle = false;
		repeat = false;
	}

	public void setPlaylists(List<Playlist> playlists) {
		log.info("Setting playlists");

		synchronized (playlistMap) {
			playlistMap.clear();

			for (Playlist playlist : playlists) {
				playlistMap.put(playlist.getPlaylistId(), playlist);
			}

			// Ensure the search playlist always exists
			if (playlistMap.get(SEARCH_PLAYLIST_ID) == null) {
				playlistMap.put(SEARCH_PLAYLIST_ID, new Playlist(SEARCH_PLAYLIST_ID, "Search Results"));
			}
		}
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
	
	public void createPlaylist() {
		log.info("Creating playlist");

		int playlistId = 1;
		
		synchronized (playlistMap) {
			// Find the first ID available
			while (playlistMap.get(playlistId) != null) {
				playlistId++;
			}

			playlistMap.put(playlistId, new Playlist(playlistId, "New Playlist"));

			log.info("Created playlist - " + playlistId);
		}

		fireEvent(Event.PLAYLIST_CREATED, playlistId);
	}
	
	public Playlist getPlaylist(int playlistId) {
		log.info("Getting playlist - " + playlistId);

		synchronized (playlistMap) {
			return playlistMap.get(playlistId);
		}
	}
	
	public void deletePlaylist(int playlistId) {
		log.info("Delete playlist - " + playlistId);
		
		if (playlistId == SEARCH_PLAYLIST_ID) {
			return;
		}

		// Selected playlist is the position in the list and is
		// re-calculated after every delete from the list
		int selectedPlaylist = 0;
		
		synchronized (playlistMap) {
			for (int nextPlaylistId : playlistMap.keySet()) {
				if (nextPlaylistId == playlistId) {
					break;
				}
				
				selectedPlaylist++;
			}
			
			playlistMap.remove(playlistId);
			
			if (selectedPlaylist > 0) {
				selectedPlaylist--;
			}
		}

		fireEvent(Event.PLAYLIST_DELETED, selectedPlaylist);
	}
	
	public void setPlaylistTracks(int playlistId, List<Track> tracks) {
		log.info("Setting playlist tracks - " + playlistId);

		synchronized (playlistMap) {
			playlistMap.get(playlistId).setTracks(tracks);
		}

		fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
	}
	
	public void addTrackToPlaylist(int playlistId, Track track) {
		log.info("Adding track : Playlist - " + playlistId + ", Track - " + track.getArtistName() + " - " + track.getAlbumName() + " - " + track.getTrackName());

		synchronized (playlistMap) {
			playlistMap.get(playlistId).addTrack(track);
		}

		fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
	}
	
	public void removeTrackFromPlaylist(int playlistId, Track track) {
		log.info("Removing track : Playlist - " + playlistId + ", Track - " + track.getArtistName() + " - " + track.getAlbumName() + " - " + track.getTrackName());

		synchronized (playlistMap) {
			playlistMap.get(playlistId).removeTrack(track);
		}

		fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
	}

	public boolean isTrackInPlaylist(int playlistId, String trackId) {
		if (trackId == null) {
			return false;
		}
		
		synchronized (playlistMap) {
			Playlist playlist = playlistMap.get(playlistId);
			
			if (playlist == null) {
				return false;
			}
			
			for (Track track : playlist.getTracks()) {
				if (track.getTrackId().equals(trackId)) {
					return true;
				}
			}
			
			return false;
		}
	}

	public void playPlaylist(int playlistId) {
		log.info("Playing playlist - " + playlistId);

		currentPlaylistId = playlistId;
		currentPlaylistIndex = 0;

		fireEvent(Event.PLAYLIST_SELECTED, playlistId);

		playCurrentTrack();
	}
	
	public void playTrack(Track track) {
		log.info("Playing track : Playlist - " + track.getPlaylistId() + ", Index - " + track.getPlaylistIndex() + ", Track - " + track.getArtistName() + 
			" - " + track.getAlbumName() + " - " + track.getTrackName());

		currentPlaylistId = track.getPlaylistId();
		currentPlaylistIndex = track.getPlaylistIndex();
		
		playCurrentTrack();
	}
	
	public void playCurrentTrack() {
		log.info("Playing current track");

		synchronized (playlistMap) {
			Playlist currentPlaylist = playlistMap.get(currentPlaylistId);

			if (!playlistMap.get(currentPlaylistId).getTracks().isEmpty()) {
				currentTrack = currentPlaylist.getTracks().get(currentPlaylistIndex);

				mediaManager.playTrack(currentTrack);
			}
		}
	}
	
	public void pauseCurrentTrack() {
		log.info("Pausing current track");

		mediaManager.pausePlayback();
	}

	public void resumeCurrentTrack() {
		log.info("Resuming current track");

		mediaManager.resumePlayback();
	}
	
	public void restartTrack() {
		log.info("Restarting current track");
		
		mediaManager.setSeekPositionPercent(0);
	}
	
	public boolean playPreviousTrack() {
		log.info("Playing previous track");
		
		Playlist currentPlaylist = null;
		
		synchronized (playlistMap) {
			currentPlaylist = playlistMap.get(currentPlaylistId);
		}
		
		if (currentPlaylist != null) {
			if (currentPlaylistIndex > 0) {
				currentPlaylistIndex--;
				
				playCurrentTrack();
				
				return true;
			}
		}
		
		if (repeat) {
			currentPlaylistIndex = currentPlaylist.getTracks().size();
			
			playCurrentTrack();
			
			return true;
		}
		
		currentPlaylistIndex = 0;
		mediaManager.stopPlayback();

		return false;
	}

	public boolean playNextTrack() {
		log.info("Playing next track");

		Playlist currentPlaylist = null;

		synchronized (playlistMap) {
			currentPlaylist = playlistMap.get(currentPlaylistId);
		}

		if (currentPlaylist != null) {
			if (currentPlaylistIndex < (currentPlaylist.getTracks().size() - 1)) {
				currentPlaylistIndex++;

				playCurrentTrack();

				return true;
			}

			if (repeat) {
				currentPlaylistIndex = 0;

				playCurrentTrack();

				return true;
			}
		}

		currentPlaylistIndex = 0;
		mediaManager.stopPlayback();

		return false;
	}
	
	public void setSuffle(boolean shuffle) {
		log.info("Setting shuffle - " + shuffle);
		
		synchronized (playlistMap) {
			this.shuffle = shuffle;
		}
	}

	public void setRepeat(boolean repeat) {
		log.info("Setting repeat - " + repeat);

		synchronized (playlistMap) {
			this.repeat = repeat;
		}
	}

	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case TRACK_SELECTED: {
				if (payload != null && payload.length > 0) {
					Track track = (Track)payload[0];
					
					// If we're not playing a track, the selected track is queued up next
					if (!mediaManager.isPlaying()) {
						currentPlaylistId = track.getPlaylistId();
						currentPlaylistIndex = track.getPlaylistIndex();
					}
				}

				break;
			}
			case END_OF_MEDIA: {
				log.info("End of track reached, looking for next track in playlist");

				if (!playNextTrack()) {
					log.info("End of playlist reached, stopping");

					currentPlaylistIndex = 0;
				}

				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
