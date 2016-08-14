package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.input.DataFormat;

public interface Constants {
	public static final String PROP_DATAFILE_URL			= "datafile.url";
	public static final String LAST_INDEXED_FILE			= "last-indexed";
	public static final String SETTINGS_FILE				= "rpm-jukebox.xml";
	public static final String MESSAGE_BUNDLE				= "i18n.message-bundle";
	public static final String ARTIST_INDEX_DIRECTORY		= "artistIndex";
	public static final String TRACK_INDEX_DIRECTORY		= "trackIndex";
	public static final String UNSPECIFIED_GENRE 			= "Unspecified";
	public static final double DEFAULT_VOLUME				= 0.8;
	
	public static final int MAX_SEARCH_HITS 				= 250;
	public static final int MAX_PLAYLIST_SIZE				= 1000;
	public static final int RANDOM_PLAYLIST_SIZE			= 50;
	public static final int PREVIOUS_SECONDS_CUTOFF			= 3;
    public static final DataFormat DND_TRACK_DATA_FORMAT    = new DataFormat("dyn.dnd.track.data.format");
    
    // Message bundle keys
    public static final String MESSAGE_CHECKING_DATA		= "message.checkingData";
    public static final String MESSAGE_DOWNLOAD_INDEX		= "message.downloadIndex";
    public static final String MESSAGE_PLAYLIST_SEARCH		= "playlist.searchResults";
    public static final String MESSAGE_PLAYLIST_FAVOURITES	= "playlist.favourites";
    
    // Styles
    public static final String STYLE_LOVE_BUTTON_ON			= "loveButtonOn";
    public static final String STYLE_LOVE_BUTTON_OFF		= "loveButtonOff";
    
    // Images
    public static final String IMAGE_WINDOW_ICON			= "/images/window-icon.png";
    public static final String IMAGE_NO_ARTWORK				= "/images/no-artwork.png";
    public static final String IMAGE_SHUFFLE_OFF			= "/images/shuffle-off.png";
    public static final String IMAGE_SHUFFLE_ON				= "/images/shuffle-on.png";
    public static final String IMAGE_REPEAT_OFF				= "/images/repeat-off.png";
    public static final String IMAGE_REPEAT_ALL				= "/images/repeat-all.png";
    public static final String IMAGE_REPEAT_ONE				= "/images/repeat-one.png";
    public static final String IMAGE_PLAY					= "/images/play.png";
    public static final String IMAGE_PAUSE					= "/images/pause.png";
    
    // Reserved playlists
    public static final int PLAYLIST_ID_SEARCH              = -1;
    public static final int PLAYLIST_ID_FAVOURITES			= -2;
}
