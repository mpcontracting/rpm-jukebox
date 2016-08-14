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
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class PlaylistManager extends EventAwareObject implements InitializingBean, Constants {
	
	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private MediaManager mediaManager;

	private Map<Integer, Playlist> playlistMap;
	
	@Getter private int currentPlaylistId;
	@Getter private int currentPlaylistIndex;
	private Track currentTrack;
	@Getter private boolean shuffle;
	@Getter private Repeat repeat;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising PlaylistManager");
		
		playlistMap = new LinkedHashMap<Integer, Playlist>();
		playlistMap.put(PLAYLIST_ID_SEARCH, new Playlist(PLAYLIST_ID_SEARCH, settingsManager.getMessageBundle().getString(MESSAGE_PLAYLIST_SEARCH)));
		playlistMap.put(PLAYLIST_ID_FAVOURITES, new Playlist(PLAYLIST_ID_FAVOURITES, settingsManager.getMessageBundle().getString(MESSAGE_PLAYLIST_FAVOURITES)));
		currentPlaylistId = PLAYLIST_ID_SEARCH;
		currentPlaylistIndex = 0;
		shuffle = false;
		repeat = Repeat.OFF;
	}

	public void setPlaylists(List<Playlist> playlists) {
		log.info("Setting playlists");

		synchronized (playlistMap) {
			for (Playlist playlist : playlists) {
				playlistMap.put(playlist.getPlaylistId(), playlist);
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
		
		if (playlistId == PLAYLIST_ID_SEARCH) {
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
			
			return playlist.isTrackInPlaylist(trackId);
		}
	}

	public void playPlaylist(int playlistId) {
		log.info("Playing playlist - " + playlistId);

		currentPlaylistId = playlistId;
		currentPlaylistIndex = 0;

		fireEvent(Event.PLAYLIST_SELECTED, playlistId);

		playCurrentTrack(false);
	}
	
	public void playTrack(Track track) {
		log.info("Playing track : Playlist - " + track.getPlaylistId() + ", Index - " + track.getPlaylistIndex() + ", Track - " + track.getArtistName() + 
			" - " + track.getAlbumName() + " - " + track.getTrackName());

		synchronized (playlistMap) {
			currentPlaylistId = track.getPlaylistId();
			currentPlaylistIndex = track.getPlaylistIndex();
		}
		
		playCurrentTrack(true);
	}
	
	public void playCurrentTrack(boolean overrideShuffle) {
		log.info("Playing current track");

		synchronized (playlistMap) {
			Playlist currentPlaylist = playlistMap.get(currentPlaylistId);

			if (!currentPlaylist.isEmpty()) {
				if (shuffle && !overrideShuffle) {
					log.info("Getting shuffled track");
					currentTrack = currentPlaylist.getShuffledTrackAtIndex(currentPlaylistIndex);
				} else {
					log.info("Getting non-shuffled track");
					currentTrack = currentPlaylist.getTrackAtIndex(currentPlaylistIndex);
				}
				
				// If we're shuffling and overriding the shuffle, make
				// sure the current track is placed in the current position
				// in the shuffled stack
				if (shuffle && overrideShuffle) {
					currentPlaylist.setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
				}

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
	
	public boolean playPreviousTrack(boolean overrideRepeatOne) {
		log.info("Playing previous track");
		
		// Repeat ONE (overridden on previous/next button press)
		if (!overrideRepeatOne && repeat == Repeat.ONE) {
			mediaManager.setSeekPositionPercent(0);
			
			return true;
		}
		
		Playlist currentPlaylist = null;
		
		synchronized (playlistMap) {
			currentPlaylist = playlistMap.get(currentPlaylistId);
		}

		// Still tracks in playlist
		if (currentPlaylist != null) {
			if (currentPlaylistIndex > 0) {
				currentPlaylistIndex--;
				
				playCurrentTrack(false);
				
				return true;
			}
			
			// No more tracks in playlist but repeat ALL or overridden from
			// previous/next button press and repeat ONE
			if (repeat == Repeat.ALL || (overrideRepeatOne && repeat == Repeat.ONE)) {
				currentPlaylistIndex = currentPlaylist.size() - 1;
				
				playCurrentTrack(false);
				
				return true;
			}
		}

		currentPlaylistIndex = 0;
		mediaManager.stopPlayback();

		return false;
	}

	public boolean playNextTrack(boolean overrideRepeatOne) {
		log.info("Playing next track");
		
		// Repeat ONE (overridden on previous/next button press)
		if (!overrideRepeatOne && repeat == Repeat.ONE) {
			mediaManager.setSeekPositionPercent(0);
			
			return true;
		}

		Playlist currentPlaylist = null;

		synchronized (playlistMap) {
			currentPlaylist = playlistMap.get(currentPlaylistId);
		}

		// Still tracks in playlist
		if (currentPlaylist != null) {
			if (currentPlaylistIndex < (currentPlaylist.size() - 1)) {
				currentPlaylistIndex++;

				playCurrentTrack(false);

				return true;
			}

			// No more tracks in playlist but repeat ALL or overridden from
			// previous/next button press and repeat ONE
			if (repeat == Repeat.ALL || (overrideRepeatOne && repeat == Repeat.ONE)) {
				currentPlaylistIndex = 0;

				playCurrentTrack(false);

				return true;
			}
		}

		mediaManager.stopPlayback();

		return false;
	}
	
	public void setShuffle(boolean shuffle, boolean ignorePlaylist) {
		log.info("Setting shuffle - " + shuffle);
		
		synchronized (playlistMap) {
			this.shuffle = shuffle;
			
			if (shuffle && !ignorePlaylist) {
				log.info("Shuffling current playlist - " + currentPlaylistId);
				
				playlistMap.get(currentPlaylistId).shuffle();
				
				// If we're playing or pausing a track, make sure that track is placed 
				// in the current position in the shuffled stack
				if (currentTrack != null && (mediaManager.isPlaying() || mediaManager.isPaused())) {
					playlistMap.get(currentPlaylistId).setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
				}
			} else if (!shuffle && !ignorePlaylist) {
				// If we're playing or pausing a track, we need to reset our position
				// in the current playlist
				if (currentTrack != null && (mediaManager.isPlaying() || mediaManager.isPaused())) {
					currentPlaylistIndex = currentTrack.getPlaylistIndex();
				}
			}
		}
	}

	public void setRepeat(Repeat repeat) {
		log.info("Setting repeat - " + repeat);

		synchronized (playlistMap) {
			this.repeat = repeat;
		}
	}
	
	public void updateRepeat() {
		log.info("Updating repeat from - " + repeat);
		
		switch (repeat) {
			case OFF: {
				repeat = Repeat.ALL;
				break;
			}
			case ALL: {
				repeat = Repeat.ONE;
				break;
			}
			case ONE: {
				repeat = Repeat.OFF;
				break;
			}
		}
		
		log.info("Updated to - " + repeat);
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

				if (!playNextTrack(false)) {
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
