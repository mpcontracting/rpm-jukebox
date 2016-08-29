package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.input.DataFormat;

public interface Constants {

	public static final String I18N_MESSAGE_BUNDLE										= "i18n.message-bundle";
	public static final String UNSPECIFIED_GENRE 										= "Unspecified";
    public static final DataFormat DND_TRACK_DATA_FORMAT    							= new DataFormat("dyn.dnd.track.data.format");
    
    // Property keys
    public static final String PROP_DATAFILE_URL										= "datafile.url";
    public static final String PROP_DIRECTORY_CONFIG									= "directory.config";
    public static final String PROP_DIRECTORY_LOG										= "directory.log";
    public static final String PROP_DIRECTORY_ARTIST_INDEX								= "directory.artist.index";
    public static final String PROP_DIRECTORY_TRACK_INDEX								= "directory.track.index";
    public static final String PROP_DIRECTORY_CACHE										= "directory.cache";
    public static final String PROP_FILE_LAST_INDEXED									= "file.last.indexed";
    public static final String PROP_FILE_WINDOW_SETTINGS								= "file.window.settings";
    public static final String PROP_FILE_SETTINGS										= "file.settings";
    public static final String PROP_INTERNAL_JETTY_PORT									= "internal.jetty.port";
    public static final String PROP_DEFAULT_VOLUME										= "default.volume";
    public static final String PROP_MAX_SEARCH_HITS										= "max.search.hits";
    public static final String PROP_MAX_PLAYLIST_SIZE									= "max.playlist.size";
    public static final String PROP_RANDOM_PLAYLIST_SIZE								= "random.playlist.size";
    public static final String PROP_PREVIOUS_SECONDS_CUTOFF								= "previous.seconds.cutoff";
    public static final String PROP_PLAYLIST_FILE_EXTENSION								= "playlist.file.extension";
    public static final String PROP_CACHE_SIZE_MB										= "cache.size.mb";
    
    // Message bundle keys
    public static final String MESSAGE_CHECKING_DATA									= "message.checkingData";
    public static final String MESSAGE_DOWNLOAD_INDEX									= "message.downloadIndex";
    public static final String MESSAGE_ALREADY_RUNNING									= "message.alreadyRunning";
    public static final String MESSAGE_PLAYLIST_DEFAULT									= "playlist.default";
    public static final String MESSAGE_PLAYLIST_SEARCH									= "playlist.searchResults";
    public static final String MESSAGE_PLAYLIST_FAVOURITES								= "playlist.favourites";
    public static final String MESSAGE_PLAYLIST_CONTEXT_NEW_PLAYLIST					= "playlist.context.newPlaylist";
    public static final String MESSAGE_PLAYLIST_CONTEXT_DELETE_PLAYLIST					= "playlist.context.deletePlaylist";
    public static final String MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE						= "playlist.delete.areYouSure";
    public static final String MESSAGE_TRACK_TABLE_CONTEXT_CREATE_PLAYLIST_FROM_ALBUM	= "trackTable.context.createPlaylistFromAlbum";
    public static final String MESSAGE_TRACK_TABLE_CONTEXT_DELETE_TRACK_FROM_PLAYLIST	= "trackTable.context.deleteTrackFromPlaylist";
    public static final String MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER						= "file.chooser.playlist.filter";
    public static final String MESSAGE_IMPORT_PLAYLIST_TITLE							= "import.playlist.title";
    public static final String MESSAGE_EXPORT_PLAYLIST_TITLE							= "export.playlist.title";

    // Styles
    public static final String STYLE_LOVE_BUTTON_ON										= "loveButtonOn";
    public static final String STYLE_LOVE_BUTTON_OFF									= "loveButtonOff";
    public static final String STYLE_VALID_BORDER										= "-fx-border-color: -jb-border-color";
    public static final String STYLE_INVALID_BORDER										= "-fx-border-color: -jb-error-color";
    
    // Images
    public static final String IMAGE_WINDOW_ICON										= "/images/window-icon.png";
    public static final String IMAGE_NO_ARTWORK											= "/images/no-artwork.png";
    public static final String IMAGE_SHUFFLE_OFF										= "/images/shuffle-off.png";
    public static final String IMAGE_SHUFFLE_ON											= "/images/shuffle-on.png";
    public static final String IMAGE_REPEAT_OFF											= "/images/repeat-off.png";
    public static final String IMAGE_REPEAT_ALL											= "/images/repeat-all.png";
    public static final String IMAGE_REPEAT_ONE											= "/images/repeat-one.png";
    public static final String IMAGE_PLAY												= "/images/play.png";
    public static final String IMAGE_PAUSE												= "/images/pause.png";
    
    // Reserved playlists
    public static final int PLAYLIST_ID_SEARCH              							= -1;
    public static final int PLAYLIST_ID_FAVOURITES										= -2;
}
