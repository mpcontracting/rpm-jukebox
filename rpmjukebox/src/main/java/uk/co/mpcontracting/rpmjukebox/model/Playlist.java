package uk.co.mpcontracting.rpmjukebox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = {"playlistId"})
public class Playlist implements Constants, Iterable<Track> {
    @Getter private int playlistId;
    @Getter @Setter private String name;
    private List<Track> tracks;

    public Playlist(int playlistId, String name) {
        this.playlistId = playlistId;
        this.name = name;
        
        tracks = Collections.synchronizedList(new ArrayList<Track>());
    }
    
    public Track getTrackAtIndex(int index) {
    	return tracks.get(index);
    }

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
    
    public void setTracks(List<Track> tracks) {
    	clear();
    	
    	if (tracks != null) {
    		for (Track track : tracks) {
    			addTrack(track);
    		}
    	}
    }
    
    public void addTrack(Track track) {
    	if (tracks.size() < MAX_PLAYLIST_SIZE && !tracks.contains(track)) {
    		track.setPlaylistId(playlistId);
    		track.setPlaylistIndex(tracks.size());
    		tracks.add(track);
    	}
    }
    
    public void removeTrack(Track track) {
    	if (tracks.contains(track)) {
    		tracks.remove(track);
    	}
    }
    
    @Override
	public Iterator<Track> iterator() {
		return tracks.iterator();
	}
    
    public int size() {
    	return tracks.size();
    }
    
    public boolean isEmpty() {
    	return tracks.isEmpty();
    }
    
    public void clear() {
    	tracks.clear();
    }
}

