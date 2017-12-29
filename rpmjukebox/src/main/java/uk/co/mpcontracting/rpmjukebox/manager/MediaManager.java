package uk.co.mpcontracting.rpmjukebox.manager;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class MediaManager extends EventAwareObject implements Constants {

    @Autowired
    private CacheManager cacheManager;

    @Value("${default.volume}")
    @Getter
    private double volume;

    private MediaPlayer currentPlayer;
    private Track currentTrack;
    private Media currentMedia;
    private Duration currentDuration;
    @Getter
    private Equalizer equalizer;
    @Getter
    private boolean muted;

    @PostConstruct
    public void initialise() {
        log.info("Initialising MediaManager");

        muted = false;
        equalizer = new Equalizer(10);
    }

    public void playTrack(Track track) {
        log.debug("Playing track : {} - {} - {} - {}", track.getArtistName(), track.getAlbumName(),
            track.getTrackName(), track.getLocation());

        currentTrack = track;
        currentMedia = constructMedia(
            cacheManager.constructInternalUrl(CacheType.TRACK, track.getTrackId(), track.getLocation()));

        createNewMediaPlayer();

        currentPlayer.play();

        fireEvent(Event.TRACK_QUEUED_FOR_PLAYING, currentTrack);
    }

    public void pausePlayback() {
        log.debug("Pausing playback");

        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    public void resumePlayback() {
        log.debug("Resuming playback");

        if (currentPlayer != null) {
            currentPlayer.play();
        }
    }

    public void stopPlayback() {
        log.debug("Stopping playback");

        if (currentPlayer != null) {
            currentPlayer.stop();
        }
    }

    public void setVolumePercent(double volumePercent) {
        this.volume = volumePercent / 100.0;

        boolean currentMuted = muted;

        if (volume > 0) {
            muted = false;
        } else {
            muted = true;
        }

        if (muted != currentMuted) {
            fireEvent(Event.MUTE_UPDATED);
        }

        if (currentPlayer != null) {
            currentPlayer.setVolume(volume);
        }
    }

    public void setMuted() {
        muted = !muted;

        if (currentPlayer != null) {
            currentPlayer.setVolume(muted ? 0 : volume);
        }
    }

    public void setSeekPositionPercent(double seekPositionPercent) {
        if (currentPlayer != null) {
            currentPlayer.seek(currentDuration.multiply(seekPositionPercent / 100.0));
        }
    }

    public double getPlayingTimeSeconds() {
        if (currentPlayer != null) {
            return currentPlayer.getCurrentTime().toSeconds();
        }

        return 0;
    }

    public double getPlayingTimePercent() {
        if (currentPlayer != null) {
            return (currentPlayer.getCurrentTime().toMillis() / currentPlayer.getTotalDuration().toMillis()) * 100.0;
        }

        return 0;
    }

    public void setEqualizerGain(int band, double value) {
        log.debug("Setting EQ gain : Band - {}, Value - {}", band, value);

        equalizer.setGain(band, value);

        if (currentPlayer != null) {
            currentPlayer.getAudioEqualizer().getBands().get(band).setGain(value);
        }
    }

    public boolean isPlaying() {
        if (currentPlayer != null) {
            return currentPlayer.getStatus() == Status.PLAYING;
        }

        return false;
    }

    public boolean isPaused() {
        if (currentPlayer != null) {
            return currentPlayer.getStatus() == Status.PAUSED;
        }

        return false;
    }

    public void cleanUpResources() {
        log.debug("Cleaning up resources");

        stopPlayback();

        if (currentPlayer != null) {
            currentPlayer.dispose();
        }

        currentPlayer = null;

        // Kick the garbage collector
        System.gc();
    }

    @Synchronized
    private void createNewMediaPlayer() {
        cleanUpResources();

        currentPlayer = constructMediaPlayer(currentMedia);
        currentPlayer.setVolume(muted ? 0 : volume);
        currentPlayer.setCycleCount(1);

        for (int i = 0; i < equalizer.getNumberOfBands(); i++) {
            currentPlayer.getAudioEqualizer().getBands().get(i).setGain(equalizer.getGain(i));
        }

        currentPlayer.setOnReady(() -> {
            currentPlayer.seek(currentPlayer.getStartTime());
            currentDuration = currentPlayer.getMedia().getDuration();

            fireEvent(Event.TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
        });

        currentPlayer.setOnPlaying(() -> {
            fireEvent(Event.MEDIA_PLAYING, currentTrack);
            fireEvent(Event.BUFFER_UPDATED, currentDuration, currentPlayer.getBufferProgressTime());
        });

        currentPlayer.setOnPaused(() -> {
            fireEvent(Event.MEDIA_PAUSED);
        });

        currentPlayer.setOnStopped(() -> {
            fireEvent(Event.MEDIA_STOPPED);
        });

        currentPlayer.setOnEndOfMedia(() -> {
            fireEvent(Event.END_OF_MEDIA);
        });

        currentPlayer.setOnError(() -> {
            log.warn("Error occurred playing media - " + currentPlayer.getError());

            fireEvent(Event.END_OF_MEDIA);
        });

        currentPlayer.currentTimeProperty().addListener(observable -> {
            if (currentPlayer != null) {
                fireEvent(Event.TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
            }
        });

        currentPlayer.bufferProgressTimeProperty().addListener(observable -> {
            if (currentPlayer != null) {
                fireEvent(Event.BUFFER_UPDATED, currentDuration, currentPlayer.getBufferProgressTime());
            }
        });
    }

    // Package level for testing purposes
    Media constructMedia(String source) {
        return new Media(source);
    }

    // Package level for testing purposes
    MediaPlayer constructMediaPlayer(Media media) {
        return new MediaPlayer(media);
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            case EQUALIZER_UPDATED: {
                setEqualizerGain((Integer)payload[0], (Double)payload[1]);

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
