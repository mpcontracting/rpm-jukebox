package uk.co.mpcontracting.rpmjukebox.service;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractEventAwareObjectTest;

import static javafx.scene.media.MediaPlayer.Status.PAUSED;
import static javafx.scene.media.MediaPlayer.Status.PLAYING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.co.mpcontracting.rpmjukebox.event.Event.*;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

class MediaServiceTest extends AbstractEventAwareObjectTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private CacheService cacheService;

  @Mock
  private MediaPlayer mediaPlayer;

  @Mock
  private Duration currentDuration;

  @Mock
  private ReadOnlyObjectProperty<Duration> currentTimeProperty;

  @Mock
  private ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty;

  @Mock
  private AudioEqualizer audioEqualizer;

  @Mock
  private ObservableList<EqualizerBand> equalizerBands;

  @Mock
  private EqualizerBand equalizerBand;

  @Mock
  private Equalizer equalizer;

  @Mock
  private Track track;

  private MediaService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new MediaService(applicationProperties, cacheService));
    underTest.initialise();

    setField(underTest, "currentPlayer", mediaPlayer);
    setField(underTest, "currentDuration", currentDuration);

    lenient().doReturn(mediaPlayer).when(underTest).constructMediaPlayer(any());

    lenient().when(mediaPlayer.currentTimeProperty()).thenReturn(currentTimeProperty);
    lenient().when(mediaPlayer.bufferProgressTimeProperty()).thenReturn(bufferProgressTimeProperty);
    lenient().when(mediaPlayer.getAudioEqualizer()).thenReturn(audioEqualizer);
    lenient().when(audioEqualizer.getBands()).thenReturn(equalizerBands);
    lenient().when(equalizerBands.get(anyInt())).thenReturn(equalizerBand);
  }

  @Test
  void shouldPlayTrack() {
    String source = "http://localhost:43125/cache?cacheType=TRACK&id=trackId&url=http%3A%2F%2Fwww.example.com%2Fmedia.mp3";

    when(track.getTrackId()).thenReturn("trackId");
    when(track.getLocation()).thenReturn("http://www.example.com/media%2Emp3");
    when(cacheService.constructInternalUrl(any(), anyString(), anyString())).thenReturn(source);

    underTest.playTrack(track);

    Media currentMedia = getField(underTest, "currentMedia", Media.class);

    assertThat(currentMedia.getSource()).isEqualTo(source);
    assertThat(getField(underTest, "currentTrack", Track.class)).isSameAs(track);
    verify(mediaPlayer).play();
    verify(eventProcessor).fireEvent(TRACK_QUEUED_FOR_PLAYING, track);
  }

  @Test
  void shouldPausePlayback() {
    underTest.pausePlayback();

    verify(mediaPlayer).pause();
  }

  @Test
  void shouldNotPausePlaybackWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    underTest.pausePlayback();

    verify(mediaPlayer, never()).pause();
  }

  @Test
  void shouldResumePlayback() {
    underTest.resumePlayback();

    verify(mediaPlayer).play();
  }

  @Test
  void shouldNotResumePlaybackWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    underTest.resumePlayback();

    verify(mediaPlayer, never()).play();
  }

  @Test
  void shouldStopPlayback() {
    underTest.stopPlayback();

    verify(mediaPlayer).stop();
  }

  @Test
  void shouldNotStopPlaybackWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    underTest.stopPlayback();

    verify(mediaPlayer, never()).stop();
  }

  @Test
  void shouldSetVolumePercent() {
    setField(underTest, "muted", true);
    setField(underTest, "volume", 0d);

    underTest.setVolumePercent(50d);

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0.5d);
    assertThat(getField(underTest, "muted", Boolean.class)).isFalse();
    verify(mediaPlayer).setVolume(anyDouble());
    verify(eventProcessor).fireEvent(MUTE_UPDATED);
  }

  @Test
  void shouldSetVolumePercentZero() {
    setField(underTest, "muted", false);
    setField(underTest, "volume", 0.5d);

    underTest.setVolumePercent(0d);

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0d);
    assertThat(getField(underTest, "muted", Boolean.class)).isTrue();
    verify(mediaPlayer).setVolume(anyDouble());
    verify(eventProcessor).fireEvent(MUTE_UPDATED);
  }

  @Test
  void shouldNotSetVolumePercentWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);
    setField(underTest, "muted", true);
    setField(underTest, "volume", 0d);

    underTest.setVolumePercent(50d);

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0.5d);
    assertThat(getField(underTest, "muted", Boolean.class)).isFalse();
    verify(mediaPlayer, never()).setVolume(anyDouble());
    verify(eventProcessor).fireEvent(MUTE_UPDATED);
  }

  @Test
  void shouldNotFireMuteUpdatedEventIfMutedFlagHasNotChanged() {
    setField(underTest, "muted", true);
    setField(underTest, "volume", 0d);

    underTest.setVolumePercent(0d);

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0d);
    assertThat(getField(underTest, "muted", Boolean.class)).isTrue();
    verify(mediaPlayer).setVolume(anyDouble());
    verify(eventProcessor, never()).fireEvent(MUTE_UPDATED);
  }

  @Test
  void shouldSetMutedOn() {
    setField(underTest, "muted", false);
    setField(underTest, "volume", 0.5d);

    underTest.setMuted();

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0.5d);
    assertThat(getField(underTest, "muted", Boolean.class)).isTrue();
    verify(mediaPlayer).setVolume(0d);
  }

  @Test
  void shouldSetMutedOff() {
    setField(underTest, "muted", true);
    setField(underTest, "volume", 0.5d);

    underTest.setMuted();

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0.5d);
    assertThat(getField(underTest, "muted", Boolean.class)).isFalse();
    verify(mediaPlayer).setVolume(0.5d);
  }

  @Test
  void shouldNotSetMutedWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);
    setField(underTest, "muted", true);
    setField(underTest, "volume", 0.5d);

    underTest.setMuted();

    assertThat(getField(underTest, "volume", Double.class)).isEqualTo(0.5d);
    assertThat(getField(underTest, "muted", Boolean.class)).isFalse();
    verify(mediaPlayer, never()).setVolume(anyDouble());
  }

  @Test
  void shouldSetSeekPositionPercent() {
    Duration duration = new Duration(1000);
    when(currentDuration.multiply(anyDouble())).thenReturn(duration);

    underTest.setSeekPositionPercent(0.5);

    verify(mediaPlayer).seek(duration);
  }

  @Test
  void shouldNotSetSeekPositionPercentWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    underTest.setSeekPositionPercent(0.5);

    verify(mediaPlayer, never()).seek(any());
  }

  @Test
  void shouldGetPlayingTimeSeconds() {
    when(mediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

    double result = underTest.getPlayingTimeSeconds();

    assertThat(result).isEqualTo(2d);
  }

  @Test
  void shouldGetPlayingTimeSecondsWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    double result = underTest.getPlayingTimeSeconds();

    assertThat(result).isEqualTo(0d);
  }

  @Test
  void shouldGetPlayingTimePercent() {
    when(mediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));
    when(mediaPlayer.getTotalDuration()).thenReturn(new Duration(4000));

    double result = underTest.getPlayingTimePercent();

    assertThat(result).isEqualTo(50d);
  }

  @Test
  void shouldGetPlayingTimePercentWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    double result = underTest.getPlayingTimePercent();

    assertThat(result).isEqualTo(0d);
  }

  @Test
  void shouldSetEqualizerGain() {
    setField(underTest, "equalizer", equalizer);

    underTest.setEqualizerGain(5, 50d);

    verify(equalizer).setGain(5, 50d);
    verify(equalizerBands).get(5);
    verify(equalizerBand).setGain(50d);
  }

  @Test
  void shouldNotSetEqualizerGainWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);
    setField(underTest, "equalizer", equalizer);

    underTest.setEqualizerGain(5, 50d);

    verify(equalizer).setGain(5, 50d);
    verify(equalizerBands, never()).get(anyInt());
    verify(equalizerBand, never()).setGain(anyDouble());
  }

  @Test
  void shouldGetIsPlaying() {
    when(mediaPlayer.getStatus()).thenReturn(PLAYING);

    boolean result = underTest.isPlaying();

    assertThat(result).isTrue();
    verify(mediaPlayer).getStatus();
  }

  @Test
  void shouldGetIsNotPlaying() {
    when(mediaPlayer.getStatus()).thenReturn(PAUSED);

    boolean result = underTest.isPlaying();

    assertThat(result).isFalse();
    verify(mediaPlayer).getStatus();
  }

  @Test
  void shouldNotGetIsPlayingWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    boolean result = underTest.isPlaying();

    assertThat(result).isFalse();
    verify(mediaPlayer, never()).getStatus();
  }

  @Test
  void shouldGetIsPaused() {
    when(mediaPlayer.getStatus()).thenReturn(PAUSED);

    boolean result = underTest.isPaused();

    assertThat(result).isTrue();
    verify(mediaPlayer).getStatus();
  }

  @Test
  void shouldGetIsNotPaused() {
    when(mediaPlayer.getStatus()).thenReturn(PLAYING);

    boolean result = underTest.isPaused();

    assertThat(result).isFalse();
    verify(mediaPlayer).getStatus();
  }

  @Test
  void shouldNotGetIsPausedWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    boolean result = underTest.isPaused();

    assertThat(result).isFalse();
    verify(mediaPlayer, never()).getStatus();
  }

  @Test
  void shouldCleanUpResources() {
    underTest.cleanUpResources();

    verify(underTest).stopPlayback();
    verify(mediaPlayer).dispose();
  }

  @Test
  void shouldCleanUpResourcesWhenCurrentPlayerIsNull() {
    setField(underTest, "currentPlayer", null);

    underTest.cleanUpResources();

    verify(underTest).stopPlayback();
    verify(mediaPlayer, never()).dispose();
  }

  @Test
  void shouldConstructConcreteMediaPlayer() {
    MediaPlayer mediaPlayer = underTest.constructMediaPlayer(new Media("http://www.example.com/example.mp3"));

    assertThat(mediaPlayer).isNotNull();
  }

  @Test
  void shouldSetEqualizerGainOnEqualizerUpdatedEvent() {
    doNothing().when(underTest).setEqualizerGain(anyInt(), anyDouble());

    underTest.eventReceived(EQUALIZER_UPDATED, 1, 1d);

    verify(underTest).setEqualizerGain(anyInt(), anyDouble());
  }
}