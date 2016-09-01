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
    public void setPlaylistId(int playlistId) {
    	this.playlistId = playlistId;
    	
    	for (Track track : tracks) {
    		track.setPlaylistId(playlistId);
    	}
    	
    	for (Track track : shuffledTracks) {
    		track.setPlaylistId(playlistId);
    	}
    }
    
    @Synchronized
    public Track getTrackAtIndex(int index) {
    	if (index < tracks.size()) {
    		return tracks.get(index);
    	}
    	
    	return null;
    }
    
    @Synchronized
    public Track getPlaylistTrack(Track track) {
    	for (Track playlistTrack : tracks) {
    		if (playlistTrack.equals(track)) {
    			return playlistTrack;
    		}
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
    
    @Synchronized
    public void swapTracks(Track source, Track target) {
		int trackSourceIndex = tracks.indexOf(source);
		int trackTargetIndex = tracks.indexOf(target);
		int shuffledSourceIndex = shuffledTracks.indexOf(source);
		int shuffledTargetIndex = shuffledTracks.indexOf(target);
		
		if (trackSourceIndex > -1 && trackTargetIndex > -1 && shuffledSourceIndex > -1 && shuffledTargetIndex > -1) {
			if (trackSourceIndex <= trackTargetIndex) {
		        Collections.rotate(tracks.subList(trackSourceIndex, trackTargetIndex + 1), -1);
		    } else {
		        Collections.rotate(tracks.subList(trackTargetIndex, trackSourceIndex + 1), 1);
		    }

			Collections.swap(shuffledTracks, shuffledSourceIndex, shuffledTargetIndex);
			
			for (int i = 0; i < tracks.size(); i++) {
				tracks.get(i).setPlaylistIndex(i);
			}
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
