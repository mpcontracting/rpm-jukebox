package uk.co.mpcontracting.rpmdata.model.data;

public class Track extends AbstractData {
	private String location;
	private boolean preferred;
	
	public Track(int id, String name, String location) {
		super(TRACK_IDENTIFIER, id, name);
		
		this.location = trim(location);
		
		preferred = false;
	}
	
	public void setPreferred() {
		preferred = true;
	}

	@Override
	protected String getDataRowInternal() {
		return location + SEPARATOR + (preferred ? "true" : "false");
	}
}
