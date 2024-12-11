package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.nonNull;
import static javafx.scene.media.MediaPlayer.Status.PAUSED;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.BUFFER_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.END_OF_MEDIA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.EQUALIZER_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PAUSED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_STOPPED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MUTE_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TIME_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_QUEUED_FOR_PLAYING;

import jakarta.annotation.PostConstruct;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService extends EventAwareObject {

  private final ApplicationProperties applicationProperties;
  private final CacheService cacheService;

  @Getter
  private boolean muted;

  @Getter
  private double volume;

  @Getter
  private Equalizer equalizer;

  private MediaPlayer currentPlayer;
  private Track currentTrack;
  private Media currentMedia;
  private Duration currentDuration;

  @PostConstruct
  public void initialise() {
    log.info("Initialising {}", getClass().getSimpleName());

    muted = false;
    volume = applicationProperties.getDefaultVolume();
    equalizer = new Equalizer(10);
  }

  void playTrack(Track track) {
    log.debug("Playing track : {} - {} - {} - {}", track.getArtistName(), track.getAlbumName(),
        track.getTrackName(), track.getLocation());

    currentTrack = track;
//    currentMedia = new Media(cacheService.constructInternalUrl(TRACK, track.getTrackId(),
//        track.getLocation().replace("%2Emp3", ".mp3")));
    currentMedia = new Media(track.getLocation().replace("%2Emp3", ".mp3"));

    createNewMediaPlayer();

    currentPlayer.play();

    fireEvent(TRACK_QUEUED_FOR_PLAYING, currentTrack);
  }

  void pausePlayback() {
    log.debug("Pausing playback");

    if (nonNull(currentPlayer)) {
      currentPlayer.pause();
    }
  }

  void resumePlayback() {
    log.debug("Resuming playback");

    if (nonNull(currentPlayer)) {
      currentPlayer.play();
    }
  }

  void stopPlayback() {
    log.debug("Stopping playback");

    if (nonNull(currentPlayer)) {
      currentPlayer.stop();
    }
  }

  public void setVolumePercent(double volumePercent) {
    this.volume = volumePercent / 100.0;

    boolean currentMuted = muted;

    muted = !(volume > 0);

    if (muted != currentMuted) {
      fireEvent(MUTE_UPDATED);
    }

    if (nonNull(currentPlayer)) {
      currentPlayer.setVolume(volume);
    }
  }

  public void setMuted() {
    muted = !muted;

    if (nonNull(currentPlayer)) {
      currentPlayer.setVolume(muted ? 0 : volume);
    }
  }

  public void setSeekPositionPercent(double seekPositionPercent) {
    if (nonNull(currentPlayer)) {
      currentPlayer.seek(currentDuration.multiply(seekPositionPercent / 100.0));
    }
  }

  public double getPlayingTimeSeconds() {
    if (nonNull(currentPlayer)) {
      return currentPlayer.getCurrentTime().toSeconds();
    }

    return 0;
  }

  public double getPlayingTimePercent() {
    if (nonNull(currentPlayer)) {
      return (currentPlayer.getCurrentTime().toMillis() / currentPlayer.getTotalDuration().toMillis()) * 100.0;
    }

    return 0;
  }

  void setEqualizerGain(int band, double value) {
    log.debug("Setting EQ gain : Band - {}, Value - {}", band, value);

    equalizer.setGain(band, value);

    if (nonNull(currentPlayer)) {
      currentPlayer.getAudioEqualizer().getBands().get(band).setGain(value);
    }
  }

  public boolean isPlaying() {
    if (nonNull(currentPlayer)) {
      return currentPlayer.getStatus() == PLAYING;
    }

    return false;
  }

  public boolean isPaused() {
    if (nonNull(currentPlayer)) {
      return currentPlayer.getStatus() == PAUSED;
    }

    return false;
  }

  void cleanUpResources() {
    log.debug("Cleaning up resources");

    stopPlayback();

    if (nonNull(currentPlayer)) {
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

      fireEvent(TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
    });

    currentPlayer.setOnPlaying(() -> {
      fireEvent(MEDIA_PLAYING, currentTrack);
      fireEvent(BUFFER_UPDATED, currentDuration, currentPlayer.getBufferProgressTime());
    });

    currentPlayer.setOnPaused(() -> fireEvent(MEDIA_PAUSED));
    currentPlayer.setOnStopped(() -> fireEvent(MEDIA_STOPPED));
    currentPlayer.setOnEndOfMedia(() -> fireEvent(END_OF_MEDIA));

    currentPlayer.setOnError(() -> {
      log.warn("Error occurred playing media - " + currentPlayer.getError());

      fireEvent(END_OF_MEDIA);
    });

    currentPlayer.currentTimeProperty().addListener(observable -> {
      if (nonNull(currentPlayer)) {
        fireEvent(TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
      }
    });

    currentPlayer.bufferProgressTimeProperty().addListener(observable -> {
      if (nonNull(currentPlayer)) {
        fireEvent(BUFFER_UPDATED, currentDuration, currentPlayer.getBufferProgressTime());
      }
    });
  }

  protected MediaPlayer constructMediaPlayer(Media media) {
    return new MediaPlayer(media);
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    if (event == EQUALIZER_UPDATED) {
      setEqualizerGain((Integer) payload[0], (Double) payload[1]);
    }
  }
}
