package uk.co.mpcontracting.rpmjukebox.event;

public enum Event {
	APPLICATION_INITIALISED,
    TIME_UPDATED, 
    EQUALIZER_UPDATED,
    BUFFER_UPDATED,
    MEDIA_PLAYING, 
    MEDIA_PAUSED, 
    MEDIA_STOPPED,
    END_OF_MEDIA,
    PLAYLIST_CREATED,
    PLAYLIST_DELETED,
    PLAYLIST_CONTENT_UPDATED,
    PLAYLIST_SELECTED,
    TRACK_QUEUED_FOR_PLAYING;
}
