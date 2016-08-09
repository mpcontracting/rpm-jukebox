package uk.co.mpcontracting.rpmdata.model.data;

import uk.co.mpcontracting.rpmdata.support.HashGenerator;

public class LocationData {
	private String bandId;
	private String albumId;
	private String trackId;
	
	public LocationData(String location) throws Exception {
		if (location == null) {
			throw new IllegalArgumentException("Cannot process IDs from a null location");
		}
		
		int trackSlash = location.lastIndexOf('/');
		int albumSlash = location.lastIndexOf('/', trackSlash - 1);
		int bandSlash = location.lastIndexOf('/', albumSlash - 1);

		bandId = location.substring(bandSlash + 1, albumSlash);
		albumId = location.substring(albumSlash + 1, trackSlash);
		trackId = HashGenerator.generateHash(location.substring(bandSlash + 1));
	}
	
	public String getBandId() {
		return bandId;
	}
	
	public String getAlbumId() {
		return albumId;
	}
	
	public String getTrackId() {
		return trackId;
	}
	
	public boolean isValid() {
		return bandId != null && bandId.trim().length() > 0 &&
			albumId != null && albumId.trim().length() > 0 &&
			trackId != null && trackId.trim().length() > 0;
	}
}
