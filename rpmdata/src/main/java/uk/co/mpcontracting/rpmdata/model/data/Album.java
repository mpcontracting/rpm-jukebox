package uk.co.mpcontracting.rpmdata.model.data;

import java.util.ArrayList;
import java.util.List;

public class Album extends AbstractData {
	private String image;
	private int year;
	private String preferredTrackName;
	private List<Track> tracks;
	
	public Album(int id, String name, String image, int year, String preferredTrackName) {
		super(ALBUM_IDENTIFIER, id, name);
		
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
	
	public List<Track> getTracks() {
		return tracks;
	}

	@Override
	protected String getDataRowInternal() {
		return image + SEPARATOR + Integer.toString(year) + SEPARATOR + preferredTrackName;
	}
}
