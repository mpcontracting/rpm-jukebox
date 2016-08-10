package uk.co.mpcontracting.rpmjukebox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = {"playlistId"})
public class Playlist {
    @Getter private int playlistId;
    @Getter @Setter private String name;
    @Getter private List<Track> tracks;
    
    public Playlist(int playlistId, String name) {
        this.playlistId = playlistId;
        this.name = name;
        
        tracks = Collections.synchronizedList(new ArrayList<Track>());
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
    	if (!tracks.contains(track)) {
    		tracks.add(track);
    	}
    }
    
    public void removeTrack(Track track) {
    	if (tracks.contains(track)) {
    		tracks.remove(track);
    	}
    }
    
    public void clear() {
    	tracks.clear();
    }
}

