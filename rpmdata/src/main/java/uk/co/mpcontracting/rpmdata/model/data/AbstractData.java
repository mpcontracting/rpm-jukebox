package uk.co.mpcontracting.rpmdata.model.data;

public abstract class AbstractData {
	protected static final String BAND_IDENTIFIER	= "B";
	protected static final String ALBUM_IDENTIFIER	= "A";
	protected static final String TRACK_IDENTIFIER	= "T";
	protected static final String SEPARATOR 		= "|@|";
	
	private String dataIdentifier;
	private int id;
	private String name;

	protected AbstractData(String dataIdentifier, int id, String name) {
		this.dataIdentifier = dataIdentifier;
		this.id = id;
		this.name = trim(name);
	}
	
	protected abstract String getDataRowInternal();
	
	protected String trim(String string) {
		if (string == null) {
			return "";
		}
		
		return string.replaceAll("[\\s\\u00A0]+$", "").trim();
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDataRow() {
		return dataIdentifier + SEPARATOR + id + SEPARATOR + name + SEPARATOR + getDataRowInternal();
	}
}
