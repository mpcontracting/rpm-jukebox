package uk.co.mpcontracting.rpmjukebox.support;

import javafx.scene.input.DataFormat;

public interface Constants {

    String I18N_MESSAGE_BUNDLE = "i18n.message-bundle";
    String UNSPECIFIED_GENRE = "";
    DataFormat DND_TRACK_DATA_FORMAT = new DataFormat("dyn.dnd.track.data.format");

    // Message bundle keys
    String MESSAGE_WINDOW_TITLE = "window.title";
    String MESSAGE_MENU_FILE_EXIT = "menu.file.exit";
    String MESSAGE_MENU_CONTROLS_PLAY = "menu.controls.playPause.play";
    String MESSAGE_MENU_CONTROLS_PAUSE = "menu.controls.playPause.pause";
    String MESSAGE_SPLASH_INITIALISING_CACHE = "splash.initialisingCache";
    String MESSAGE_SPLASH_CHECKING_DATA = "splash.checkingData";
    String MESSAGE_SPLASH_DOWNLOAD_INDEX = "splash.downloadIndex";
    String MESSAGE_SPLASH_LOADING_SYSTEM_SETTINGS = "splash.loadingSystemSettings";
    String MESSAGE_SPLASH_INITIALISING_SEARCH = "splash.initialisingSearch";
    String MESSAGE_SPLASH_LOADING_USER_SETTINGS = "splash.loadingUserSettings";
    String MESSAGE_SPLASH_INITIALISING_VIEWS = "splash.initialisingViews";
    String MESSAGE_SPLASH_ALREADY_RUNNING = "splash.alreadyRunning";
    String MESSAGE_PLAYLIST_DEFAULT = "playlist.default";
    String MESSAGE_PLAYLIST_SEARCH = "playlist.searchResults";
    String MESSAGE_PLAYLIST_FAVOURITES = "playlist.favourites";
    String MESSAGE_PLAYLIST_CONTEXT_NEW_PLAYLIST = "playlist.context.newPlaylist";
    String MESSAGE_PLAYLIST_CONTEXT_DELETE_PLAYLIST = "playlist.context.deletePlaylist";
    String MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE = "playlist.delete.areYouSure";
    String MESSAGE_TRACK_TABLE_CONTEXT_CREATE_PLAYLIST_FROM_ALBUM = "trackTable.context.createPlaylistFromAlbum";
    String MESSAGE_TRACK_TABLE_CONTEXT_DELETE_TRACK_FROM_PLAYLIST = "trackTable.context.deleteTrackFromPlaylist";
    String MESSAGE_DOWNLOAD_INDEX = "message.downloadIndex";
    String MESSAGE_NEW_VERSION_AVAILABLE = "message.newVersionAvailable";
    String MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER = "file.chooser.playlist.filter";
    String MESSAGE_IMPORT_PLAYLIST_TITLE = "import.playlist.title";
    String MESSAGE_EXPORT_PLAYLIST_TITLE = "export.playlist.title";
    String MESSAGE_SETTINGS_COPYRIGHT_2 = "settings.copyright.2";
    String MESSAGE_YEAR_FILTER_NONE = "yearFilter.none";

    // Styles
    String STYLE_LOVE_BUTTON_ON = "loveButtonOn";
    String STYLE_LOVE_BUTTON_OFF = "loveButtonOff";
    String STYLE_VALID_BORDER = "-fx-border-color: -jb-border-color";
    String STYLE_INVALID_BORDER = "-fx-border-color: -jb-error-color";

    // Images
    String IMAGE_SPLASH_SCREEN = "/images/splash-screen.png";
    String IMAGE_WINDOW_ICON = "/images/window-icon.png";
    String IMAGE_NO_ARTWORK = "/images/no-artwork.png";
    String IMAGE_DRAG_N_DROP = "/images/drag-n-drop.png";
    String IMAGE_VOLUME_OFF = "/images/volume-off.png";
    String IMAGE_VOLUME_ON = "/images/volume-on.png";
    String IMAGE_SHUFFLE_OFF = "/images/shuffle-off.png";
    String IMAGE_SHUFFLE_ON = "/images/shuffle-on.png";
    String IMAGE_REPEAT_OFF = "/images/repeat-off.png";
    String IMAGE_REPEAT_ALL = "/images/repeat-all.png";
    String IMAGE_REPEAT_ONE = "/images/repeat-one.png";
    String IMAGE_PLAY = "/images/play.png";
    String IMAGE_PAUSE = "/images/pause.png";

    // Reserved playlists
    int PLAYLIST_ID_SEARCH = -1;
    int PLAYLIST_ID_FAVOURITES = -2;
}
