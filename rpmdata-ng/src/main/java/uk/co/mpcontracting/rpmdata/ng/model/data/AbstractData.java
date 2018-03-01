package uk.co.mpcontracting.rpmdata.ng.model.data;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractData {
    protected static final String BAND_IDENTIFIER   = "B";
    protected static final String ALBUM_IDENTIFIER  = "A";
    protected static final String TRACK_IDENTIFIER  = "T";
    protected static final String SEPARATOR         = "|@|";
    
    private String dataIdentifier;
    @Getter @Setter private String id;
    @Getter private String name;

    protected AbstractData(String dataIdentifier, String name) {
        this.dataIdentifier = dataIdentifier;
        this.name = trim(name);
    }
    
    protected abstract String getDataRowInternal();
    
    protected String trim(String string) {
        if (string == null) {
            return "";
        }
        
        return string.replaceAll("[\\s\\u00A0]+$", "").trim();
    }

    public String getDataRow() {
        return dataIdentifier + SEPARATOR + id + SEPARATOR + name + SEPARATOR + getDataRowInternal();
    }
}

