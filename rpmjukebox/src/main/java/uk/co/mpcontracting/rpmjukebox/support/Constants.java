package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.input.DataFormat;

public interface Constants {
	public static final String PROP_DATAFILE_URL			= "datafile.url";
	public static final String SETTINGS_FILE				= "rpm-jukebox.xml";
	public static final double DEFAULT_VOLUME				= 0.8;
	public static final int SEARCH_PLAYLIST_ID              = -1;
    public static final DataFormat DND_TRACK_DATA_FORMAT    = new DataFormat("dyn.dnd.track.data.format");
}
