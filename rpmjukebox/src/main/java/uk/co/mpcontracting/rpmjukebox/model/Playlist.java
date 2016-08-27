package uk.co.mpcontracting.rpmjukebox.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = {"playlistId"})
public class Playlist implements Constants, Iterable<Track> {
    @Getter private int playlistId;
    @Getter @Setter private String name;
    private int maxPlaylistSize;
    private List<Track> tracks;
    private List<Track> shuffledTracks;
    
    private SecureRandom random;

    public Playlist(int playlistId, String name, int maxPlaylistSize) {
        this.playlistId = playlistId;
        this.name = name;
        this.maxPlaylistSize = maxPlaylistSize;
        
        tracks = new ArrayList<Track>();
        shuffledTracks = new ArrayList<Track>();
        
        random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
    }
    
    @Synchronized
    public Track getTrackAtIndex(int index) {
    	if (index < tracks.size()) {
    		return tracks.get(index);
    	}
    	
    	return null;
    }
    
    @Synchronized
    public Track getShuffledTrackAtIndex(int index) {
    	if (index < shuffledTracks.size()) {
    		return shuffledTracks.get(index);
    	}
    	
    	return null;
    }
    
    @Synchronized
    public void shuffle() {
    	Collections.shuffle(shuffledTracks, random);
    }
    
    @Synchronized
    public void setTrackAtShuffledIndex(Track track, int index) {
    	shuffledTracks.remove(track);
    	shuffledTracks.add(index, track);
    }

    @Synchronized
    public boolean isTrackInPlaylist(String trackId) {
    	if (trackId == null) {
			return false;
		}
    	
    	for (Track track : tracks) {
			if (track.getTrackId().equals(trackId)) {
				return true;
			}
		}
		
		return false;
    }
    
    @Synchronized
    public void setTracks(List<Track> tracks) {
    	clear();

		for (Track track : tracks) {
			addTrack(track);
		}
    }
    
    @Synchronized
    public void addTrack(Track track) {
    	if (tracks.size() < maxPlaylistSize && !tracks.contains(track)) {
    		track.setPlaylistId(playlistId);
    		track.setPlaylistIndex(tracks.size());
    		tracks.add(track);
    		shuffledTracks.add((int)(random.nextDouble() * tracks.size()), track);
    	}
    }
    
    @Synchronized
    public void removeTrack(Track track) {
    	if (tracks.contains(track)) {
    		tracks.remove(track);
    		shuffledTracks.remove(track);
    	}
    }
    
    @Override
    @Synchronized
	public Iterator<Track> iterator() {
		return tracks.iterator();
	}
    
    @Synchronized
    public int size() {
    	return tracks.size();
    }
    
    @Synchronized
    public boolean isEmpty() {
    	return tracks.isEmpty();
    }
    
    @Synchronized
    public void clear() {
    	tracks.clear();
    	shuffledTracks.clear();
    }
}
