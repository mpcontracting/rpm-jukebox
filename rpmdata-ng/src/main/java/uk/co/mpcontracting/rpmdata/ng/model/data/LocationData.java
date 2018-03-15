package uk.co.mpcontracting.rpmdata.ng.model.data;

import lombok.Getter;
import uk.co.mpcontracting.rpmdata.ng.support.HashGenerator;

public class LocationData {
    @Getter private String bandId;
    @Getter private String albumId;
    @Getter private String trackId;
    
    public LocationData(String bandId, String albumId, String location) throws Exception {
        if (location == null) {
            throw new IllegalArgumentException("Cannot process IDs from a null location");
        }
        
        int trackSlash = location.lastIndexOf('/') + 1;

        this.bandId = bandId;
        this.albumId = albumId;
        trackId = HashGenerator.generateHash(location.substring(trackSlash));
    }

    public boolean isValid() {
        return bandId != null && bandId.trim().length() > 0 &&
            albumId != null && albumId.trim().length() > 0 &&
            trackId != null && trackId.trim().length() > 0;
    }
}