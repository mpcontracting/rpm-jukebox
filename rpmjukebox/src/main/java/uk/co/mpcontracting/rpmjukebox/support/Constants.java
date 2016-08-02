package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.input.DataFormat;

public interface Constants {
	public static final String PROP_DATAFILE_URL			= "datafile.url";
	public static final String SETTINGS_FILE				= "rpm-jukebox.xml";
	public static final String ARTIST_INDEX_DIRECTORY		= "artistIndex";
	public static final String TRACK_INDEX_DIRECTORY		= "trackIndex";
	public static final String UNSPECIFIED_GENRE 			= "Unspecified";
	public static final double DEFAULT_VOLUME				= 0.8;
	public static final int SEARCH_PLAYLIST_ID              = -1;
	public static final int MAX_SEARCH_HITS 				= 200;
	public static final int RANDOM_PLAYLIST_SIZE			= 50;
    public static final DataFormat DND_TRACK_DATA_FORMAT    = new DataFormat("dyn.dnd.track.data.format");
}
