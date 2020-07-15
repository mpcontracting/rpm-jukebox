package uk.co.mpcontracting.rpmjukebox.manager;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class MediaManagerTest {

    @Mock
    private EventManager eventManager;

    @Mock
    private AppProperties appProperties;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private MediaPlayer mediaPlayer;

    @Mock
    private AudioEqualizer audioEqualizer;

    @Mock
    private ObservableList<EqualizerBand> equalizerBands;

    @Mock
    private EqualizerBand equalizerBand;

    @Mock
    private Duration currentDuration;

    @Mock
    private ReadOnlyObjectProperty<Duration> currentTimeProperty;

    @Mock
    private ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty;

    @Mock
    private Equalizer equalizer;

    @Mock
    private Track track;

    private MediaManager underTest;

    @Before
    public void setup() {
        underTest = spy(new MediaManager(appProperties));
        underTest.wireCacheManager(cacheManager);
        underTest.initialise();

        setField(underTest, "eventManager", eventManager);
        setField(underTest, "currentPlayer", mediaPlayer);
        setField(underTest, "currentDuration", currentDuration);

        doReturn(mediaPlayer).when(underTest).constructMediaPlayer(any());

        when(mediaPlayer.currentTimeProperty()).thenReturn(currentTimeProperty);
        when(mediaPlayer.bufferProgressTimeProperty()).thenReturn(bufferProgressTimeProperty);
        when(mediaPlayer.getAudioEqualizer()).thenReturn(audioEqualizer);
        when(audioEqualizer.getBands()).thenReturn(equalizerBands);
        when(equalizerBands.get(anyInt())).thenReturn(equalizerBand);
    }

    @Test
    public void shouldPlayTrack() {
        String source = "http://localhost:43125/cache?cacheType=TRACK&id=trackId&url=http%3A%2F%2Fwww.example.com%2Fmedia.mp3";

        when(track.getTrackId()).thenReturn("trackId");
        when(track.getLocation()).thenReturn("http://www.example.com/media%2Emp3");
        when(cacheManager.constructInternalUrl(any(), anyString(), anyString())).thenReturn(source);

        underTest.playTrack(track);

        Media currentMedia = (Media) getField(underTest, "currentMedia");

        assertThat(currentMedia.getSource()).isEqualTo(source);
        assertThat(getField(underTest, "currentTrack")).isSameAs(track);
        verify(mediaPlayer, times(1)).play();
        verify(eventManager, times(1)).fireEvent(Event.TRACK_QUEUED_FOR_PLAYING, track);
    }

    @Test
    public void shouldPausePlayback() {
        underTest.pausePlayback();

        verify(mediaPlayer, times(1)).pause();
    }

    @Test
    public void shouldNotPausePlaybackWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        underTest.pausePlayback();

        verify(mediaPlayer, never()).pause();
    }

    @Test
    public void shouldResumePlayback() {
        underTest.resumePlayback();

        verify(mediaPlayer, times(1)).play();
    }

    @Test
    public void shouldNotResumePlaybackWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        underTest.resumePlayback();

        verify(mediaPlayer, never()).play();
    }

    @Test
    public void shouldStopPlayback() {
        underTest.stopPlayback();

        verify(mediaPlayer, times(1)).stop();
    }

    @Test
    public void shouldNotStopPlaybackWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        underTest.stopPlayback();

        verify(mediaPlayer, never()).stop();
    }

    @Test
    public void shouldSetVolumePercent() {
        setField(underTest, "muted", true);
        setField(underTest, "volume", 0d);

        underTest.setVolumePercent(50d);

        assertThat((double) getField(underTest, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(underTest, "muted")).isFalse();
        verify(mediaPlayer, times(1)).setVolume(anyDouble());
        verify(eventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetVolumePercentZero() {
        setField(underTest, "muted", false);
        setField(underTest, "volume", 0.5d);

        underTest.setVolumePercent(0d);

        assertThat((double) getField(underTest, "volume")).isEqualTo(0d);
        assertThat((boolean) getField(underTest, "muted")).isTrue();
        verify(mediaPlayer, times(1)).setVolume(anyDouble());
        verify(eventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotSetVolumePercentWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);
        setField(underTest, "muted", true);
        setField(underTest, "volume", 0d);

        underTest.setVolumePercent(50d);

        assertThat((double) getField(underTest, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(underTest, "muted")).isFalse();
        verify(mediaPlayer, never()).setVolume(anyDouble());
        verify(eventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotFireMuteUpdatedEventIfMutedFlagHasNotChanged() {
        setField(underTest, "muted", true);
        setField(underTest, "volume", 0d);

        underTest.setVolumePercent(0d);

        assertThat((double) getField(underTest, "volume")).isEqualTo(0d);
        assertThat((boolean) getField(underTest, "muted")).isTrue();
        verify(mediaPlayer, times(1)).setVolume(anyDouble());
        verify(eventManager, never()).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetMutedOn() {
        setField(underTest, "muted", false);
        setField(underTest, "volume", 0.5d);

        underTest.setMuted();

        assertThat((double) getField(underTest, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(underTest, "muted")).isTrue();
        verify(mediaPlayer, times(1)).setVolume(0d);
    }

    @Test
    public void shouldSetMutedOff() {
        setField(underTest, "muted", true);
        setField(underTest, "volume", 0.5d);

        underTest.setMuted();

        assertThat((double) getField(underTest, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(underTest, "muted")).isFalse();
        verify(mediaPlayer, times(1)).setVolume(0.5d);
    }

    @Test
    public void shouldNotSetMutedWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);
        setField(underTest, "muted", true);
        setField(underTest, "volume", 0.5d);

        underTest.setMuted();

        assertThat((double) getField(underTest, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(underTest, "muted")).isFalse();
        verify(mediaPlayer, never()).setVolume(anyDouble());
    }

    @Test
    public void shouldSetSeekPositionPercent() {
        Duration duration = new Duration(1000);
        when(this.currentDuration.multiply(anyDouble())).thenReturn(duration);

        underTest.setSeekPositionPercent(0.5);

        verify(mediaPlayer, times(1)).seek(duration);
    }

    @Test
    public void shouldNotSetSeekPositionPercentWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        underTest.setSeekPositionPercent(0.5);

        verify(mediaPlayer, never()).seek(any());
    }

    @Test
    public void shouldGetPlayingTimeSeconds() {
        when(mediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

        double result = underTest.getPlayingTimeSeconds();

        assertThat(result).isEqualTo(2d);
    }

    @Test
    public void shouldGetPlayingTimeSecondsWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        double result = underTest.getPlayingTimeSeconds();

        assertThat(result).isEqualTo(0d);
    }

    @Test
    public void shouldGetPlayingTimePercent() {
        when(mediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));
        when(mediaPlayer.getTotalDuration()).thenReturn(new Duration(4000));

        double result = underTest.getPlayingTimePercent();

        assertThat(result).isEqualTo(50d);
    }

    @Test
    public void shouldGetPlayingTimePercentWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        double result = underTest.getPlayingTimePercent();

        assertThat(result).isEqualTo(0d);
    }

    @Test
    public void shouldSetEqualizerGain() {
        setField(underTest, "equalizer", equalizer);

        underTest.setEqualizerGain(5, 50d);

        verify(equalizer, times(1)).setGain(5, 50d);
        verify(equalizerBands, times(1)).get(5);
        verify(equalizerBand, times(1)).setGain(50d);
    }

    @Test
    public void shouldNotSetEqualizerGainWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);
        setField(underTest, "equalizer", equalizer);

        underTest.setEqualizerGain(5, 50d);

        verify(equalizer, times(1)).setGain(5, 50d);
        verify(equalizerBands, never()).get(anyInt());
        verify(equalizerBand, never()).setGain(anyDouble());
    }

    @Test
    public void shouldGetIsPlaying() {
        when(mediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = underTest.isPlaying();

        assertThat(result).isTrue();
        verify(mediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldGetIsNotPlaying() {
        when(mediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = underTest.isPlaying();

        assertThat(result).isFalse();
        verify(mediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldNotGetIsPlayingWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        boolean result = underTest.isPlaying();

        assertThat(result).isFalse();
        verify(mediaPlayer, never()).getStatus();
    }

    @Test
    public void shouldGetIsPaused() {
        when(mediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = underTest.isPaused();

        assertThat(result).isTrue();
        verify(mediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldGetIsNotPaused() {
        when(mediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = underTest.isPaused();

        assertThat(result).isFalse();
        verify(mediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldNotGetIsPausedWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        boolean result = underTest.isPaused();

        assertThat(result).isFalse();
        verify(mediaPlayer, never()).getStatus();
    }

    @Test
    public void shouldCleanUpResources() {
        underTest.cleanUpResources();

        verify(underTest, times(1)).stopPlayback();
        verify(mediaPlayer, times(1)).dispose();
    }

    @Test
    public void shouldCleanUpResourcesWhenCurrentPlayerIsNull() {
        setField(underTest, "currentPlayer", null);

        underTest.cleanUpResources();

        verify(underTest, times(1)).stopPlayback();
        verify(mediaPlayer, never()).dispose();
    }

    @Test
    public void shouldConstructConcreteMediaPlayer() {
        MediaPlayer mediaPlayer = underTest.constructMediaPlayer(new Media("http://www.example.com/example.mp3"));

        assertThat(mediaPlayer).isNotNull();
    }

    @Test
    public void shouldSetEqualizerGainOnEqualizerUpdatedEvent() {
        doNothing().when(underTest).setEqualizerGain(anyInt(), anyDouble());

        underTest.eventReceived(Event.EQUALIZER_UPDATED, 1, 1d);

        verify(underTest, times(1)).setEqualizerGain(anyInt(), anyDouble());
    }
}
