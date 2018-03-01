package uk.co.mpcontracting.rpmdata.ng.model.data;

public class Track extends AbstractData {
    private String location;
    private boolean preferred;
    
    public Track(String id, String name, String location) {
        super(TRACK_IDENTIFIER, name);
        
        setId(id);
        
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
