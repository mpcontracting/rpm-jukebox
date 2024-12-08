package uk.co.mpcontracting.rpmjukebox.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;

@Slf4j
@Service
public class MediaService {

  @Getter
  private boolean muted;

  @Getter
  private double volume;

  @Getter
  private Equalizer equalizer;

  public void setVolumePercent(double volumePercent) {

  }

  public void setMuted() {

  }

  public void setSeekPositionPercent(double seekPositionPercent) {

  }

  public double getPlayingTimeSeconds() {
    return 0.0d;
  }

  public double getPlayingTimePercent() {
    return 0.0d;
  }

  public boolean isPlaying() {
    return false;
  }

  public boolean isPaused() {
    return false;
  }
}
