package uk.co.mpcontracting.rpmjukebox.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = {"playlistId"})
public class Playlist {
    @Getter private int playlistId;
    @Getter @Setter private String name;
    @Getter private Map<Integer, Track> trackMap;
    
    public Playlist(int playlistId, String name) {
        this.playlistId = playlistId;
        this.name = name;
        
        trackMap = Collections.synchronizedMap(new LinkedHashMap<Integer, Track>());
    }
    
    public void setTracks(List<Track> tracks) {
    	clear();
    	
    	if (tracks != null) {
    		for (Track track : tracks) {
    			addTrack(track);
    		}
    	}
    }
    
    public Collection<Track> getTracks() {
    	return trackMap.values();
    }
    
    public void putTrack(int playlistIndex, Track track) {
    	trackMap.put(playlistIndex, track);
    }
    
    public void addTrack(Track track) {
    	trackMap.put(trackMap.size(), track);
    }
    
    public void clear() {
    	trackMap.clear();
    }
}

