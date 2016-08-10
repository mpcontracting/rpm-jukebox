package uk.co.mpcontracting.rpmjukebox.manager;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class MediaManager extends EventAwareObject implements InitializingBean, Constants {
	
	private final Object mediaPlayerLock = new Object();
	
	private MediaPlayer currentPlayer;
	private Track currentTrack;
	private Media currentMedia;
	private Duration currentDuration;
	@Getter private Equalizer equalizer;
	@Getter private double volume;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising MediaManager");
		
		volume = DEFAULT_VOLUME;
		equalizer = new Equalizer(10);
	}
	
	public void playTrack(Track track) {
		log.info("Playing track : " + track.getArtistName() + " - " + track.getAlbumName() + " - " + track.getTrackName() + " - " + track.getLocation());

		currentTrack = track;
		currentMedia = new Media(track.getLocation());

		createNewMediaPlayer();

		currentPlayer.play();
		
		fireEvent(Event.TRACK_QUEUED_FOR_PLAYING, currentTrack);
	}

	public void pausePlayback() {
		log.info("Pausing playback");

		if (currentPlayer != null) {
			currentPlayer.pause();
		}
	}

	public void resumePlayback() {
		log.info("Resuming playback");

		if (currentPlayer != null) {
			currentPlayer.play();
		}
	}

	public void stopPlayback() {
		log.info("Stopping playback");

		if (currentPlayer != null) {
			currentPlayer.stop();
		}
	}

	public void setVolumePercent(double volumePercent) {
		this.volume = volumePercent / 100.0;

		if (currentPlayer != null) {
			currentPlayer.setVolume(volume);
		}
	}

	public void setSeekPositionPercent(double seekPositionPercent) {
		if (currentPlayer != null) {
			currentPlayer.seek(currentDuration.multiply(seekPositionPercent / 100.0));
		}
	}

	public void setEqualizerGain(int band, double value) {
		if (currentPlayer != null) {
			log.info("Setting EQ gain : Band - " + band + ", Value - " + value);

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
		log.info("Cleaning up resources");

		stopPlayback();
		currentPlayer = null;
	}
	
	@Synchronized("mediaPlayerLock")
	private void createNewMediaPlayer() {
		cleanUpResources();

		boolean repeat = false;

		currentPlayer = new MediaPlayer(currentMedia);
		currentPlayer.setVolume(volume);
		currentPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);

		for (int i = 0; i < equalizer.getNumberOfBands(); i++) {
			currentPlayer.getAudioEqualizer().getBands().get(i).setGain(equalizer.getGain(i));
		}

		currentPlayer.setOnReady(new Runnable() {
			@Override
			public void run() {
				currentPlayer.seek(currentPlayer.getStartTime());
				currentDuration = currentPlayer.getMedia().getDuration();

				fireEvent(Event.TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
			}
		});

		currentPlayer.setOnPlaying(new Runnable() {
			@Override
			public void run() {
				fireEvent(Event.MEDIA_PLAYING, currentTrack);
			}
		});

		currentPlayer.setOnPaused(new Runnable() {
			@Override
			public void run() {
				fireEvent(Event.MEDIA_PAUSED);
			}
		});

		currentPlayer.setOnStopped(new Runnable() {
			@Override
			public void run() {
				fireEvent(Event.MEDIA_STOPPED);
			}
		});

		currentPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				fireEvent(Event.END_OF_MEDIA);
			}
		});

		currentPlayer.currentTimeProperty().addListener(
			new InvalidationListener() {
				public void invalidated(Observable observable) {
					if (currentPlayer != null) {
						fireEvent(Event.TIME_UPDATED, currentDuration, currentPlayer.getCurrentTime());
					}
				}
			}
		);

		currentPlayer.bufferProgressTimeProperty().addListener(
			new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					if (currentPlayer != null) {
						fireEvent(Event.BUFFER_UPDATED, currentDuration, currentPlayer.getBufferProgressTime());
					}
				}
			}
		);
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
