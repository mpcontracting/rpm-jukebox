package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaylistManager extends EventAwareObject implements Constants {

    private final AppProperties appProperties;
    private final MessageManager messageManager;

    private SearchManager searchManager;
    private MediaManager mediaManager;
    private TrackTableController trackTableController;

    private final Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();

    @Getter
    private int currentPlaylistId;
    private int currentPlaylistIndex;
    private Track currentTrack;
    @Getter
    private Playlist playingPlaylist;
    @Getter
    private boolean shuffle;
    @Getter
    private Repeat repeat;

    @Getter
    private Track selectedTrack;

    @Autowired
    public void wireSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    public void wireMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @Autowired
    public void wireTrackTableController(TrackTableController trackTableController) {
        this.trackTableController = trackTableController;
    }

    @PostConstruct
    public void initialise() {
        log.info("Initialising PlaylistManager");

        playlistMap.put(PLAYLIST_ID_SEARCH, new Playlist(PLAYLIST_ID_SEARCH,
                messageManager.getMessage(MESSAGE_PLAYLIST_SEARCH),
                appProperties.getMaxPlaylistSize()));
        playlistMap.put(PLAYLIST_ID_FAVOURITES, new Playlist(PLAYLIST_ID_FAVOURITES,
                messageManager.getMessage(MESSAGE_PLAYLIST_FAVOURITES),
                appProperties.getMaxPlaylistSize()));
        currentPlaylistId = PLAYLIST_ID_SEARCH;
        currentPlaylistIndex = 0;
        currentTrack = null;
        playingPlaylist = null;
        shuffle = false;
        repeat = Repeat.OFF;

        selectedTrack = null;
    }

    public void setPlaylists(List<Playlist> playlists) {
        log.debug("Setting playlists");

        synchronized (playlistMap) {
            playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));
        }
    }

    public List<Playlist> getPlaylists() {
        log.debug("Getting playlists");

        List<Playlist> playlists;

        synchronized (playlistMap) {
            playlists = new ArrayList<>(playlistMap.values());
        }

        return unmodifiableList(playlists);
    }

    public void addPlaylist(Playlist playlist) {
        log.debug("Adding playlist - {}", playlist);

        int playlistId = 1;

        synchronized (playlistMap) {
            // Find the first ID available
            while (playlistMap.get(playlistId) != null) {
                playlistId++;
            }

            playlist.setPlaylistId(playlistId);
            playlistMap.put(playlistId, playlist);

            log.debug("Added playlist - " + playlistId);
        }
    }

    public void createPlaylist() {
        createPlaylist(messageManager.getMessage(MESSAGE_PLAYLIST_DEFAULT), true);
    }

    private Playlist createPlaylist(String name, boolean autoEdit) {
        log.debug("Creating playlist - {}", name);

        int playlistId = 1;
        Playlist playlist;

        synchronized (playlistMap) {
            // Find the first ID available
            while (playlistMap.get(playlistId) != null) {
                playlistId++;
            }

            playlist = new Playlist(playlistId, name, appProperties.getMaxPlaylistSize());
            playlistMap.put(playlistId, playlist);

            log.debug("Created playlist - " + playlistId);
        }

        fireEvent(Event.PLAYLIST_CREATED, playlistId, autoEdit);

        return playlist;
    }

    public void createPlaylistFromAlbum(Track track) {
        log.debug("Creating playlist from album : Track - {} - {} - {}", track.getArtistName(), track.getAlbumName(),
                track.getTrackName());

        searchManager.getAlbumById(track.getAlbumId())
                .filter(tracks -> !tracks.isEmpty())
                .ifPresent(tracks -> createPlaylist(track.getArtistName() + " - " + track.getAlbumName(),
                        false).setTracks(tracks));
    }

    public Optional<Playlist> getPlaylist(int playlistId) {
        log.debug("Getting playlist - {}", playlistId);

        synchronized (playlistMap) {
            return ofNullable(playlistMap.get(playlistId));
        }
    }

    public void deletePlaylist(int playlistId) {
        log.debug("Deleting playlist - {}", playlistId);

        if (playlistId < 0) {
            return;
        }

        // Selected playlist is the position in the list and is
        // re-calculated after every delete from the list
        int selectedPlaylistId = 0;

        synchronized (playlistMap) {
            for (int nextPlaylistId : playlistMap.keySet()) {

                if (nextPlaylistId == playlistId) {
                    break;
                }

                selectedPlaylistId = nextPlaylistId;
            }

            playlistMap.remove(playlistId);

            if (playlistMap.get(selectedPlaylistId) == null) {
                selectedPlaylistId = 0;
            }
        }

        fireEvent(Event.PLAYLIST_DELETED, selectedPlaylistId);
    }

    public void setPlaylistTracks(int playlistId, List<Track> tracks) {
        log.debug("Setting playlist tracks - {}", playlistId);

        synchronized (playlistMap) {
            playlistMap.get(playlistId).setTracks(tracks);
        }

        fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
    }

    public void addTrackToPlaylist(int playlistId, Track track) {
        log.debug("Adding track : Playlist - {}, Track - {} - {} - {}", playlistId, track.getArtistName(),
                track.getAlbumName(), track.getTrackName());

        synchronized (playlistMap) {
            playlistMap.get(playlistId).addTrack(track);
        }

        fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
    }

    public void removeTrackFromPlaylist(int playlistId, Track track) {
        log.debug("Removing track : Playlist - {}, Track - {} - {} - {}", playlistId, track.getArtistName(),
                track.getAlbumName(), track.getTrackName());

        synchronized (playlistMap) {
            playlistMap.get(playlistId).removeTrack(track);

            currentPlaylistIndex = 0;
        }

        fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId);
    }

    public void moveTracksInPlaylist(int playlistId, Track source, Track target) {
        log.debug("Moving tracks : Playist - {}, Source - {}, Target {}", playlistId, source.getTrackName(),
                target.getTrackName());

        synchronized (playlistMap) {
            Playlist playlist = playlistMap.get(playlistId);
            playlist.swapTracks(source, target);

            fireEvent(Event.TRACK_SELECTED, playlist.getPlaylistTrack(source));
        }

        fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistId, source);
    }

    public boolean isTrackInPlaylist(int playlistId, String trackId) {
        if (trackId == null) {
            return false;
        }

        synchronized (playlistMap) {
            return ofNullable(playlistMap.get(playlistId))
                    .filter(playlist -> playlist.isTrackInPlaylist(trackId)).isPresent();
        }
    }

    public void playPlaylist(int playlistId) {
        log.debug("Playing playlist - {}", playlistId);

        synchronized (playlistMap) {
            currentPlaylistId = playlistId;
            currentPlaylistIndex = 0;
            playingPlaylist = playlistMap.get(currentPlaylistId).clone();
        }

        fireEvent(Event.PLAYLIST_SELECTED, playlistId);

        playCurrentTrack(false);
    }

    public void playTrack(Track track) {
        log.debug("Playing track : Playlist - {}, Index - {}, Track - {} - {} - {}", track.getPlaylistId(),
                track.getPlaylistIndex(), track.getArtistName(), track.getAlbumName(), track.getTrackName());

        synchronized (playlistMap) {
            currentPlaylistId = track.getPlaylistId();
            currentPlaylistIndex = track.getPlaylistIndex();
            playingPlaylist = playlistMap.get(currentPlaylistId).clone();
        }

        playCurrentTrack(true);
    }

    public void playCurrentTrack(boolean overrideShuffle) {
        log.debug("Playing current track");

        synchronized (playlistMap) {
            // If the playing playlist is null, initialise it from
            // the current playlist ID
            if (playingPlaylist == null) {
                playingPlaylist = playlistMap.get(currentPlaylistId).clone();
            }

            if (playingPlaylist != null && !playingPlaylist.isEmpty()) {
                if (shuffle && !overrideShuffle) {
                    log.debug("Getting shuffled track - {}", currentPlaylistIndex);
                    currentTrack = playingPlaylist.getShuffledTrackAtIndex(currentPlaylistIndex);
                } else {
                    log.debug("Getting non-shuffled track - {}", currentPlaylistIndex);
                    currentTrack = playingPlaylist.getTrackAtIndex(currentPlaylistIndex);
                }

                // If we're shuffling and overriding the shuffle, make
                // sure the current track is placed in the current position
                // in the shuffled stack
                if (shuffle && overrideShuffle) {
                    playingPlaylist.setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
                }

                mediaManager.playTrack(currentTrack);
            }
        }
    }

    public void pauseCurrentTrack() {
        log.debug("Pausing current track");

        mediaManager.pausePlayback();
    }

    public void resumeCurrentTrack() {
        log.debug("Resuming current track");

        // If the selected track is a different track, or is in a different
        // playlist then play that instead of resuming the current track
        Track selectedTrack = trackTableController.getSelectedTrack();

        if (selectedTrack != null) {
            log.debug("Playing playlist - {}, Track playlist - {}, Track - {}", playingPlaylist.getPlaylistId(),
                    selectedTrack.getPlaylistId(), selectedTrack.getTrackName());

            if (playingPlaylist.getPlaylistId() != selectedTrack.getPlaylistId() ||
                    (currentTrack != null && !currentTrack.equals(selectedTrack))) {
                playTrack(selectedTrack);

                return;
            }
        }

        mediaManager.resumePlayback();
    }

    void restartTrack() {
        log.debug("Restarting current track");

        mediaManager.setSeekPositionPercent(0);
    }

    public boolean playPreviousTrack(boolean overrideRepeatOne) {
        log.debug("Playing previous track");

        // Repeat ONE (not overridden on previous/next button press)
        if (!overrideRepeatOne && repeat == Repeat.ONE) {
            mediaManager.setSeekPositionPercent(0);

            return true;
        }

        // Still tracks in playlist
        if (playingPlaylist != null) {
            if (currentPlaylistIndex > 0) {
                currentPlaylistIndex--;

                playCurrentTrack(false);

                return true;
            }

            // No more tracks in playlist but repeat ALL or overridden from
            // previous/next button press and repeat ONE
            if (repeat == Repeat.ALL || (overrideRepeatOne && repeat == Repeat.ONE)) {
                currentPlaylistIndex = playingPlaylist.size() - 1;

                playCurrentTrack(false);

                return true;
            }
        }

        currentPlaylistIndex = 0;
        mediaManager.stopPlayback();

        return false;
    }

    public boolean playNextTrack(boolean overrideRepeatOne) {
        log.debug("Playing next track");

        // Repeat ONE (not overridden on previous/next button press)
        if (!overrideRepeatOne && repeat == Repeat.ONE) {
            mediaManager.setSeekPositionPercent(0);

            return true;
        }

        // Still tracks in playlist
        if (playingPlaylist != null) {
            if (currentPlaylistIndex < (playingPlaylist.size() - 1)) {
                currentPlaylistIndex++;

                playCurrentTrack(false);

                return true;
            }

            // No more tracks in playlist but repeat ALL or overridden from
            // previous/next button press and repeat ONE
            if (repeat == Repeat.ALL || (overrideRepeatOne && repeat == Repeat.ONE)) {
                currentPlaylistIndex = 0;

                playCurrentTrack(false);

                return true;
            }
        }

        mediaManager.stopPlayback();

        return false;
    }

    public Track getTrackAtPlayingPlaylistIndex() {
        if (playingPlaylist != null && !playingPlaylist.isEmpty()) {
            if (shuffle) {
                log.debug("Getting shuffled track");
                return playingPlaylist.getShuffledTrackAtIndex(currentPlaylistIndex);
            } else {
                log.debug("Getting non-shuffled track");
                return playingPlaylist.getTrackAtIndex(currentPlaylistIndex);
            }
        }

        return null;
    }

    public void clearSelectedTrack() {
        selectedTrack = null;
    }

    public void setShuffle(boolean shuffle, boolean ignorePlaylist) {
        log.debug("Setting shuffle - " + shuffle);

        synchronized (playlistMap) {
            this.shuffle = shuffle;

            if (shuffle && !ignorePlaylist) {
                log.debug("Shuffling current playlist - {}", currentPlaylistId);

                Playlist playlist = playlistMap.get(currentPlaylistId);
                playlist.shuffle();

                // If we're playing or pausing a track, make sure that track is
                // placed in the current position in the shuffled stack
                if (currentTrack != null && (mediaManager.isPlaying() || mediaManager.isPaused())) {
                    playlist.setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
                    playingPlaylist = playlist.clone();
                }
            } else if (!shuffle && !ignorePlaylist) {
                // If we're playing or pausing a track, we need to reset our
                // position in the current playlist
                if (currentTrack != null && (mediaManager.isPlaying() || mediaManager.isPaused())) {
                    currentPlaylistIndex = currentTrack.getPlaylistIndex();
                }
            }
        }
    }

    public void setRepeat(Repeat repeat) {
        log.debug("Setting repeat - {}", repeat);

        synchronized (playlistMap) {
            this.repeat = repeat;
        }
    }

    public void updateRepeat() {
        log.debug("Updating repeat from - {}", repeat);

        switch (repeat) {
            case OFF: {
                repeat = Repeat.ALL;
                break;
            }
            case ALL: {
                repeat = Repeat.ONE;
                break;
            }
            case ONE: {
                repeat = Repeat.OFF;
                break;
            }
        }

        log.debug("Updated to - {}", repeat);
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            case TRACK_SELECTED: {
                if (payload != null && payload.length > 0) {
                    Track track = (Track) payload[0];

                    selectedTrack = track;

                    // If we're not playing a track, the selected track is
                    // queued up next
                    if (!mediaManager.isPlaying()) {
                        currentPlaylistId = track.getPlaylistId();
                        currentPlaylistIndex = track.getPlaylistIndex();
                    }
                }

                break;
            }
            case END_OF_MEDIA: {
                log.debug("End of track reached, looking for next track in playlist");

                if (!playNextTrack(false)) {
                    log.debug("End of playlist reached, stopping");

                    currentPlaylistIndex = 0;
                }

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
