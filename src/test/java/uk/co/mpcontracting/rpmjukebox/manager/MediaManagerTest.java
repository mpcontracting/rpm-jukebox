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
    private EventManager mockEventManager;

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private CacheManager mockCacheManager;

    @Mock
    private MediaPlayer mockMediaPlayer;

    @Mock
    private AudioEqualizer mockAudioEqualizer;

    @Mock
    private ObservableList<EqualizerBand> mockEqualizerBands;

    @Mock
    private EqualizerBand mockEqualizerBand;

    @Mock
    private Duration mockCurrentDuration;

    @Mock
    private ReadOnlyObjectProperty<Duration> mockCurrentTimeProperty;

    @Mock
    private ReadOnlyObjectProperty<Duration> mockBufferProgressTimeProperty;

    @Mock
    private Equalizer mockEqualizer;

    @Mock
    private Track mockTrack;

    private MediaManager spyMediaManager;

    @Before
    public void setup() {
        spyMediaManager = spy(new MediaManager(mockAppProperties));
        spyMediaManager.wireCacheManager(mockCacheManager);
        spyMediaManager.initialise();

        setField(spyMediaManager, "eventManager", mockEventManager);
        setField(spyMediaManager, "currentPlayer", mockMediaPlayer);
        setField(spyMediaManager, "currentDuration", mockCurrentDuration);

        doReturn(mockMediaPlayer).when(spyMediaManager).constructMediaPlayer(any());

        when(mockMediaPlayer.currentTimeProperty()).thenReturn(mockCurrentTimeProperty);
        when(mockMediaPlayer.bufferProgressTimeProperty()).thenReturn(mockBufferProgressTimeProperty);
        when(mockMediaPlayer.getAudioEqualizer()).thenReturn(mockAudioEqualizer);
        when(mockAudioEqualizer.getBands()).thenReturn(mockEqualizerBands);
        when(mockEqualizerBands.get(anyInt())).thenReturn(mockEqualizerBand);
    }

    @Test
    public void shouldPlayTrack() {
        String source = "http://localhost:43125/cache?cacheType=TRACK&id=trackId&url=http%3A%2F%2Fwww.example.com%2Fmedia.mp3";

        when(mockTrack.getTrackId()).thenReturn("trackId");
        when(mockTrack.getLocation()).thenReturn("http://www.example.com/media%2Emp3");
        when(mockCacheManager.constructInternalUrl(any(), anyString(), anyString())).thenReturn(source);

        spyMediaManager.playTrack(mockTrack);

        Media currentMedia = (Media) getField(spyMediaManager, "currentMedia");

        assertThat(currentMedia.getSource()).isEqualTo(source);
        assertThat(getField(spyMediaManager, "currentTrack")).isSameAs(mockTrack);
        verify(mockMediaPlayer, times(1)).play();
        verify(mockEventManager, times(1)).fireEvent(Event.TRACK_QUEUED_FOR_PLAYING, mockTrack);
    }

    @Test
    public void shouldPausePlayback() {
        spyMediaManager.pausePlayback();

        verify(mockMediaPlayer, times(1)).pause();
    }

    @Test
    public void shouldNotPausePlaybackWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.pausePlayback();

        verify(mockMediaPlayer, never()).pause();
    }

    @Test
    public void shouldResumePlayback() {
        spyMediaManager.resumePlayback();

        verify(mockMediaPlayer, times(1)).play();
    }

    @Test
    public void shouldNotResumePlaybackWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.resumePlayback();

        verify(mockMediaPlayer, never()).play();
    }

    @Test
    public void shouldStopPlayback() {
        spyMediaManager.stopPlayback();

        verify(mockMediaPlayer, times(1)).stop();
    }

    @Test
    public void shouldNotStopPlaybackWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.stopPlayback();

        verify(mockMediaPlayer, never()).stop();
    }

    @Test
    public void shouldSetVolumePercent() {
        setField(spyMediaManager, "muted", true);
        setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(50d);

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isFalse();
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(mockEventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetVolumePercentZero() {
        setField(spyMediaManager, "muted", false);
        setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setVolumePercent(0d);

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isTrue();
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(mockEventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotSetVolumePercentWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);
        setField(spyMediaManager, "muted", true);
        setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(50d);

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isFalse();
        verify(mockMediaPlayer, never()).setVolume(anyDouble());
        verify(mockEventManager, times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotFireMuteUpdatedEventIfMutedFlagHasNotChanged() {
        setField(spyMediaManager, "muted", true);
        setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(0d);

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isTrue();
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(mockEventManager, never()).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetMutedOn() {
        setField(spyMediaManager, "muted", false);
        setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isTrue();
        verify(mockMediaPlayer, times(1)).setVolume(0d);
    }

    @Test
    public void shouldSetMutedOff() {
        setField(spyMediaManager, "muted", true);
        setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isFalse();
        verify(mockMediaPlayer, times(1)).setVolume(0.5d);
    }

    @Test
    public void shouldNotSetMutedWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);
        setField(spyMediaManager, "muted", true);
        setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat((double) getField(spyMediaManager, "volume")).isEqualTo(0.5d);
        assertThat((boolean) getField(spyMediaManager, "muted")).isFalse();
        verify(mockMediaPlayer, never()).setVolume(anyDouble());
    }

    @Test
    public void shouldSetSeekPositionPercent() {
        Duration duration = new Duration(1000);
        when(mockCurrentDuration.multiply(anyDouble())).thenReturn(duration);

        spyMediaManager.setSeekPositionPercent(0.5);

        verify(mockMediaPlayer, times(1)).seek(duration);
    }

    @Test
    public void shouldNotSetSeekPositionPercentWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.setSeekPositionPercent(0.5);

        verify(mockMediaPlayer, never()).seek(any());
    }

    @Test
    public void shouldGetPlayingTimeSeconds() {
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

        double result = spyMediaManager.getPlayingTimeSeconds();

        assertThat(result).isEqualTo(2d);
    }

    @Test
    public void shouldGetPlayingTimeSecondsWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);
        //when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

        double result = spyMediaManager.getPlayingTimeSeconds();

        assertThat(result).isEqualTo(0d);
    }

    @Test
    public void shouldGetPlayingTimePercent() {
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));
        when(mockMediaPlayer.getTotalDuration()).thenReturn(new Duration(4000));

        double result = spyMediaManager.getPlayingTimePercent();

        assertThat(result).isEqualTo(50d);
    }

    @Test
    public void shouldGetPlayingTimePercentWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        double result = spyMediaManager.getPlayingTimePercent();

        assertThat(result).isEqualTo(0d);
    }

    @Test
    public void shouldSetEqualizerGain() {
        setField(spyMediaManager, "equalizer", mockEqualizer);

        spyMediaManager.setEqualizerGain(5, 50d);

        verify(mockEqualizer, times(1)).setGain(5, 50d);
        verify(mockEqualizerBands, times(1)).get(5);
        verify(mockEqualizerBand, times(1)).setGain(50d);
    }

    @Test
    public void shouldNotSetEqualizerGainWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);
        setField(spyMediaManager, "equalizer", mockEqualizer);

        spyMediaManager.setEqualizerGain(5, 50d);

        verify(mockEqualizer, times(1)).setGain(5, 50d);
        verify(mockEqualizerBands, never()).get(anyInt());
        verify(mockEqualizerBand, never()).setGain(anyDouble());
    }

    @Test
    public void shouldGetIsPlaying() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = spyMediaManager.isPlaying();

        assertThat(result).isTrue();
        verify(mockMediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldGetIsNotPlaying() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = spyMediaManager.isPlaying();

        assertThat(result).isFalse();
        verify(mockMediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldNotGetIsPlayingWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        boolean result = spyMediaManager.isPlaying();

        assertThat(result).isFalse();
        verify(mockMediaPlayer, never()).getStatus();
    }

    @Test
    public void shouldGetIsPaused() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = spyMediaManager.isPaused();

        assertThat(result).isTrue();
        verify(mockMediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldGetIsNotPaused() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = spyMediaManager.isPaused();

        assertThat(result).isFalse();
        verify(mockMediaPlayer, times(1)).getStatus();
    }

    @Test
    public void shouldNotGetIsPausedWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        boolean result = spyMediaManager.isPaused();

        assertThat(result).isFalse();
        verify(mockMediaPlayer, never()).getStatus();
    }

    @Test
    public void shouldCleanUpResources() {
        spyMediaManager.cleanUpResources();

        verify(spyMediaManager, times(1)).stopPlayback();
        verify(mockMediaPlayer, times(1)).dispose();
    }

    @Test
    public void shouldCleanUpResourcesWhenCurrentPlayerIsNull() {
        setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.cleanUpResources();

        verify(spyMediaManager, times(1)).stopPlayback();
        verify(mockMediaPlayer, never()).dispose();
    }

    @Test
    public void shouldConstructConcreteMediaPlayer() {
        MediaPlayer mediaPlayer = spyMediaManager.constructMediaPlayer(new Media("http://www.example.com/example.mp3"));

        assertThat(mediaPlayer).isNotNull();
    }

    @Test
    public void shouldSetEqualizerGainOnEqualizerUpdatedEvent() {
        doNothing().when(spyMediaManager).setEqualizerGain(anyInt(), anyDouble());

        spyMediaManager.eventReceived(Event.EQUALIZER_UPDATED, 1, 1d);

        verify(spyMediaManager, times(1)).setEqualizerGain(anyInt(), anyDouble());
    }
}
