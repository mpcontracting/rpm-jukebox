package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class MediaManagerTest extends AbstractTest {

    @Autowired
    private MediaManager mediaManager;

    @Mock
    private CacheManager mockCacheManager;

    @Mock
    private MediaPlayer mockMediaPlayer;

    @Mock
    private Media mockMedia;

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
    public void setup() throws Exception {
        spyMediaManager = spy(mediaManager);

        ReflectionTestUtils.setField(spyMediaManager, "cacheManager", mockCacheManager);
        ReflectionTestUtils.setField(spyMediaManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", mockMediaPlayer);
        ReflectionTestUtils.setField(spyMediaManager, "currentDuration", mockCurrentDuration);

        doReturn(mockMedia).when(spyMediaManager).constructMedia(any());
        doReturn(mockMediaPlayer).when(spyMediaManager).constructMediaPlayer(any());

        when(mockMediaPlayer.currentTimeProperty()).thenReturn(mockCurrentTimeProperty);
        when(mockMediaPlayer.bufferProgressTimeProperty()).thenReturn(mockBufferProgressTimeProperty);
        when(mockMediaPlayer.getAudioEqualizer()).thenReturn(mockAudioEqualizer);
        when(mockAudioEqualizer.getBands()).thenReturn(mockEqualizerBands);
        when(mockEqualizerBands.get(anyInt())).thenReturn(mockEqualizerBand);
    }

    @Test
    public void shouldPlayTrack() {
        spyMediaManager.playTrack(mockTrack);

        assertThat("Current track is the mock track",
            ReflectionTestUtils.getField(spyMediaManager, "currentTrack") == mockTrack, equalTo(true));
        verify(mockMediaPlayer, times(1)).play();
        verify(getMockEventManager(), times(1)).fireEvent(Event.TRACK_QUEUED_FOR_PLAYING, mockTrack);
    }

    @Test
    public void shouldPausePlayback() {
        spyMediaManager.pausePlayback();

        verify(mockMediaPlayer, times(1)).pause();
    }

    @Test
    public void shouldNotPausePlaybackWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);

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
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);

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
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.stopPlayback();

        verify(mockMediaPlayer, never()).stop();
    }

    @Test
    public void shouldSetVolumePercent() {
        ReflectionTestUtils.setField(spyMediaManager, "muted", true);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(50d);

        assertThat("Volume should be 0.5", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"),
            equalTo(0.5d));
        assertThat("Muted should be false", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(false));
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(getMockEventManager(), times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetVolumePercentZero() {
        ReflectionTestUtils.setField(spyMediaManager, "muted", false);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setVolumePercent(0d);

        assertThat("Volume should be 0", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"), equalTo(0d));
        assertThat("Muted should be true", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(true));
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(getMockEventManager(), times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotSetVolumePercentWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        ReflectionTestUtils.setField(spyMediaManager, "muted", true);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(50d);

        assertThat("Volume should be 0.5", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"),
            equalTo(0.5d));
        assertThat("Muted should be false", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(false));
        verify(mockMediaPlayer, never()).setVolume(anyDouble());
        verify(getMockEventManager(), times(1)).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldNotFireMuteUpdatedEventIfMutedFlagHasNotChanged() {
        ReflectionTestUtils.setField(spyMediaManager, "muted", true);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0d);

        spyMediaManager.setVolumePercent(0d);

        assertThat("Volume should be 0", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"), equalTo(0d));
        assertThat("Muted should be true", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(true));
        verify(mockMediaPlayer, times(1)).setVolume(anyDouble());
        verify(getMockEventManager(), never()).fireEvent(Event.MUTE_UPDATED);
    }

    @Test
    public void shouldSetMutedOn() {
        ReflectionTestUtils.setField(spyMediaManager, "muted", false);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat("Volume should be 0.5", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"),
            equalTo(0.5d));
        assertThat("Muted should be true", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(true));
        verify(mockMediaPlayer, times(1)).setVolume(0d);
    }

    @Test
    public void shouldSetMutedOff() {
        ReflectionTestUtils.setField(spyMediaManager, "muted", true);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat("Volume should be 0.5", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"),
            equalTo(0.5d));
        assertThat("Muted should be false", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(false));
        verify(mockMediaPlayer, times(1)).setVolume(0.5d);
    }

    @Test
    public void shouldNotSetMutedWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        ReflectionTestUtils.setField(spyMediaManager, "muted", true);
        ReflectionTestUtils.setField(spyMediaManager, "volume", 0.5d);

        spyMediaManager.setMuted();

        assertThat("Volume should be 0.5", (double)ReflectionTestUtils.getField(spyMediaManager, "volume"),
            equalTo(0.5d));
        assertThat("Muted should be false", (boolean)ReflectionTestUtils.getField(spyMediaManager, "muted"),
            equalTo(false));
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
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.setSeekPositionPercent(0.5);

        verify(mockMediaPlayer, never()).seek(any());
    }

    @Test
    public void shouldGetPlayingTimeSeconds() {
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

        double result = spyMediaManager.getPlayingTimeSeconds();

        assertThat("Playing time seconds should be 2", result, equalTo(2d));
    }

    @Test
    public void shouldGetPlayingTimeSecondsWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));

        double result = spyMediaManager.getPlayingTimeSeconds();

        assertThat("Playing time seconds should be 0", result, equalTo(0d));
    }

    @Test
    public void shouldGetPlayingTimePercent() {
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));
        when(mockMediaPlayer.getTotalDuration()).thenReturn(new Duration(4000));

        double result = spyMediaManager.getPlayingTimePercent();

        assertThat("Playing time seconds should be 50", result, equalTo(50d));
    }

    @Test
    public void shouldGetPlayingTimePercentWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        when(mockMediaPlayer.getCurrentTime()).thenReturn(new Duration(2000));
        when(mockMediaPlayer.getTotalDuration()).thenReturn(new Duration(4000));

        double result = spyMediaManager.getPlayingTimePercent();

        assertThat("Playing time seconds should be 0", result, equalTo(0d));
    }

    @Test
    public void shouldSetEqualizerGain() {
        ReflectionTestUtils.setField(spyMediaManager, "equalizer", mockEqualizer);

        spyMediaManager.setEqualizerGain(5, 50d);

        verify(mockEqualizer, times(1)).setGain(5, 50d);
        verify(mockEqualizerBands, times(1)).get(5);
        verify(mockEqualizerBand, times(1)).setGain(50d);
    }

    @Test
    public void shouldNotSetEqualizerGainWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        ReflectionTestUtils.setField(spyMediaManager, "equalizer", mockEqualizer);

        spyMediaManager.setEqualizerGain(5, 50d);

        verify(mockEqualizer, times(1)).setGain(5, 50d);
        verify(mockEqualizerBands, never()).get(anyInt());
        verify(mockEqualizerBand, never()).setGain(anyDouble());
    }

    @Test
    public void shouldGetIsPlaying() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = spyMediaManager.isPlaying();

        verify(mockMediaPlayer, times(1)).getStatus();
        assertThat("Playing status should be true", result, equalTo(true));
    }

    @Test
    public void shouldGetIsNotPlaying() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = spyMediaManager.isPlaying();

        verify(mockMediaPlayer, times(1)).getStatus();
        assertThat("Playing status should be false", result, equalTo(false));
    }

    @Test
    public void shouldNotGetIsPlayingWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = spyMediaManager.isPlaying();

        verify(mockMediaPlayer, never()).getStatus();
        assertThat("Playing status should be false", result, equalTo(false));
    }

    @Test
    public void shouldGetIsPaused() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = spyMediaManager.isPaused();

        verify(mockMediaPlayer, times(1)).getStatus();
        assertThat("Playing status should be true", result, equalTo(true));
    }

    @Test
    public void shouldGetIsNotPaused() {
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PLAYING);

        boolean result = spyMediaManager.isPaused();

        verify(mockMediaPlayer, times(1)).getStatus();
        assertThat("Playing status should be false", result, equalTo(false));
    }

    @Test
    public void shouldNotGetIsPausedWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);
        when(mockMediaPlayer.getStatus()).thenReturn(Status.PAUSED);

        boolean result = spyMediaManager.isPaused();

        verify(mockMediaPlayer, never()).getStatus();
        assertThat("Playing status should be false", result, equalTo(false));
    }

    @Test
    public void shouldCleanUpResources() {
        spyMediaManager.cleanUpResources();

        verify(spyMediaManager, times(1)).stopPlayback();
        verify(mockMediaPlayer, times(1)).dispose();
    }

    @Test
    public void shouldCleanUpResourcesWhenCurrentPlayerIsNull() {
        ReflectionTestUtils.setField(spyMediaManager, "currentPlayer", null);

        spyMediaManager.cleanUpResources();

        verify(spyMediaManager, times(1)).stopPlayback();
        verify(mockMediaPlayer, never()).dispose();
    }

    @Test
    public void shouldConstructConcreteMediaPlayer() {
        MediaPlayer mediaPlayer = mediaManager
            .constructMediaPlayer(mediaManager.constructMedia("http://www.example.com/example.mp3"));

        assertThat("Media player should not be null", mediaPlayer, notNullValue());
    }

    @Test
    public void shouldSetEqualizerGainOnEqualizerUpdatedEvent() throws Exception {
        doNothing().when(spyMediaManager).setEqualizerGain(anyInt(), anyDouble());

        spyMediaManager.eventReceived(Event.EQUALIZER_UPDATED, 1, 1d);

        verify(spyMediaManager, times(1)).setEqualizerGain(anyInt(), anyDouble());
    }
}
