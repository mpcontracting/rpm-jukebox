package uk.co.mpcontracting.rpmdata.ng.model.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class Album extends AbstractData {
    private String image;
    private int year;
    private String preferredTrackName;
    @Getter private List<Track> tracks;
    
    public Album(String name, String image, int year, String preferredTrackName) {
        super(ALBUM_IDENTIFIER, name);
        
        this.image = image;
        this.year = year;
        this.preferredTrackName = trim(preferredTrackName);
        
        tracks = new ArrayList<Track>();
    }

    public void addTrack(Track track) {
        if (preferredTrackName != null && preferredTrackName.equalsIgnoreCase(trim(track.getName()))) {
            track.setPreferred();
        }
        
        tracks.add(track);
    }

    @Override
    protected String getDataRowInternal() {
        return image + SEPARATOR + Integer.toString(year) + SEPARATOR + preferredTrackName;
    }
}
