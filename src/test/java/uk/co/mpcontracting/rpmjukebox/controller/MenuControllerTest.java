package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.manager.*;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.MenuView;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.controller.MenuController.VOLUME_DELTA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.*;
import static uk.co.mpcontracting.rpmjukebox.support.Constants.MESSAGE_MENU_CONTROLS_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.support.Constants.MESSAGE_MENU_CONTROLS_PLAY;

public class MenuControllerTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner threadRunner;

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private MenuController underTest;

    @Autowired
    private MenuView menuView;

    @Mock
    private RpmJukebox rpmJukebox;

    @Mock
    private MainPanelController mainPanelController;

    @Mock
    private TrackTableController trackTableController;

    @Mock
    private ApplicationManager applicationManager;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private PlaylistManager playlistManager;

    @Mock
    private MediaManager mediaManager;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(menuView);
    }

    @Before
    public void setup() {
        setField(underTest, "eventManager", getMockEventManager());
        setField(underTest, "rpmJukebox", rpmJukebox);
        setField(underTest, "mainPanelController", mainPanelController);
        setField(underTest, "trackTableController", trackTableController);
        setField(underTest, "applicationManager", applicationManager);
        setField(underTest, "settingsManager", settingsManager);
        setField(underTest, "playlistManager", playlistManager);
        setField(underTest, "mediaManager", mediaManager);

        when(settingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        underTest.initialize();
    }

    @Test
    public void shouldHandleFileImportPlaylistAction() {
        clickOnMenuItem("#menuFileImportPlaylist");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_FILE_IMPORT_PLAYLIST);
    }

    @Test
    public void shouldHandleFileExportPlaylistAction() {
        clickOnMenuItem("#menuFileExportPlaylist");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_FILE_EXPORT_PLAYLIST);
    }

    @Test
    public void shouldHandleFileSettingsAction() {
        clickOnMenuItem("#menuFileSettings");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_FILE_SETTINGS);
    }

    @Test
    @SneakyThrows
    public void shouldHandleFileExitAction() {
        clickOnMenuItem("#menuFileExit");

        verify(applicationManager, times(1)).stop();
        verify(rpmJukebox, times(1)).stop();
    }

    @Test
    public void shouldHandleEditAddPlaylistAction() {
        clickOnMenuItem("#menuEditAddPlaylist");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_EDIT_ADD_PLAYLIST);
    }

    @Test
    public void shouldHandleEditDeletePlaylistAction() {
        clickOnMenuItem("#menuEditDeletePlaylist");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_EDIT_DELETE_PLAYLIST);
    }

    @Test
    public void shouldHandleEditCreatePlaylistFromAlbumAction() {
        clickOnMenuItem("#menuEditCreatePlaylistFromAlbum");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);
    }

    @Test
    public void shouldHandleEditRandomPlaylistAction() {
        clickOnMenuItem("#menuEditRandomPlaylist");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_EDIT_RANDOM_PLAYLIST);
    }

    @Test
    public void shouldHandleControlsPlayPauseAction() {
        clickOnMenuItem("#menuControlsPlayPause");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_PLAY_PAUSE);
    }

    @Test
    public void shouldHandleControlsPreviousAction() {
        clickOnMenuItem("#menuControlsPrevious");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_PREVIOUS);
    }

    @Test
    public void shouldHandleControlsNextAction() {
        clickOnMenuItem("#menuControlsNext");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_NEXT);
    }

    @Test
    public void shouldHandleControlsShuffleOffActionIfShuffleIsOn() {
        CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
        CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

        checkMenuControlsShuffleOff.setSelected(true);
        checkMenuControlsShuffleOn.setSelected(false);
        when(playlistManager.isShuffle()).thenReturn(true);

        clickOnMenuItem("#checkMenuControlsShuffleOff");

        verify(playlistManager, times(1)).setShuffle(false, false);
        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_SHUFFLE);

        assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
    }

    @Test
    public void shouldHandleControlsShuffleOffActionIfShuffleIsOff() {
        CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
        CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

        checkMenuControlsShuffleOff.setSelected(false);
        checkMenuControlsShuffleOn.setSelected(true);
        when(playlistManager.isShuffle()).thenReturn(false);

        clickOnMenuItem("#checkMenuControlsShuffleOff");

        verify(playlistManager, never()).setShuffle(false, false);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_SHUFFLE);

        assertThat(checkMenuControlsShuffleOff.isSelected()).isTrue();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsShuffleOnActionIfShuffleIsOff() {
        CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
        CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

        checkMenuControlsShuffleOff.setSelected(false);
        checkMenuControlsShuffleOn.setSelected(true);
        when(playlistManager.isShuffle()).thenReturn(false);

        clickOnMenuItem("#checkMenuControlsShuffleOn");

        verify(playlistManager, times(1)).setShuffle(true, false);
        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_SHUFFLE);

        assertThat(checkMenuControlsShuffleOff.isSelected()).isTrue();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsShuffleOnActionIfShuffleIsOn() {
        CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
        CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

        checkMenuControlsShuffleOff.setSelected(true);
        checkMenuControlsShuffleOn.setSelected(false);
        when(playlistManager.isShuffle()).thenReturn(true);

        clickOnMenuItem("#checkMenuControlsShuffleOn");

        verify(playlistManager, never()).setShuffle(true, false);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_SHUFFLE);

        assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
    }

    @Test
    public void shouldHandleControlsRepeatOffActionIfRepeatIsAll() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(true);
        checkMenuControlsRepeatAll.setSelected(false);
        checkMenuControlsRepeatOne.setSelected(false);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        clickOnMenuItem("#checkMenuControlsRepeatOff");

        verify(playlistManager, times(1)).setRepeat(Repeat.OFF);
        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsRepeatOffActionIfRepeatIsOff() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(false);
        checkMenuControlsRepeatAll.setSelected(true);
        checkMenuControlsRepeatOne.setSelected(false);
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);

        clickOnMenuItem("#checkMenuControlsRepeatOff");

        verify(playlistManager, never()).setRepeat(Repeat.OFF);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsRepeatAllActionIfRepeatIsOne() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(false);
        checkMenuControlsRepeatAll.setSelected(true);
        checkMenuControlsRepeatOne.setSelected(false);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ONE);

        clickOnMenuItem("#checkMenuControlsRepeatAll");

        verify(playlistManager, times(1)).setRepeat(Repeat.ALL);
        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isTrue();
    }

    @Test
    public void shouldHandleControlsRepeatAllActionIfRepeatIsAll() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(false);
        checkMenuControlsRepeatAll.setSelected(false);
        checkMenuControlsRepeatOne.setSelected(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        clickOnMenuItem("#checkMenuControlsRepeatAll");

        verify(playlistManager, never()).setRepeat(Repeat.OFF);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsRepeatOneActionIfRepeatIsOff() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(false);
        checkMenuControlsRepeatAll.setSelected(false);
        checkMenuControlsRepeatOne.setSelected(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.OFF);

        clickOnMenuItem("#checkMenuControlsRepeatOne");

        verify(playlistManager, times(1)).setRepeat(Repeat.ONE);
        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
    }

    @Test
    public void shouldHandleControlsRepeatOneActionIfRepeatIsOne() {
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

        checkMenuControlsRepeatOff.setSelected(true);
        checkMenuControlsRepeatAll.setSelected(false);
        checkMenuControlsRepeatOne.setSelected(false);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ONE);

        clickOnMenuItem("#checkMenuControlsRepeatOne");

        verify(playlistManager, never()).setRepeat(Repeat.ONE);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_REPEAT);

        assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isTrue();
    }

    @Test
    public void shouldHandleControlsVolumeUpAction() {
        clickOnMenuItem("#menuControlsVolumeUp");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_VOLUME_UP, VOLUME_DELTA);
    }

    @Test
    public void shouldHandleControlsVolumeDownAction() {
        clickOnMenuItem("#menuControlsVolumeDown");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_VOLUME_DOWN, VOLUME_DELTA);
    }

    @Test
    public void shouldHandleControlsVolumeMuteAction() {
        clickOnMenuItem("#menuControlsVolumeMute");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_CONTROLS_VOLUME_MUTE);
    }

    @Test
    public void shouldHandleViewEqualizerAction() {
        clickOnMenuItem("#menuViewEqualizer");

        verify(getMockEventManager(), times(1)).fireEvent(MENU_VIEW_EQUALIZER);
    }

    @Test
    @SneakyThrows
    public void shouldReceiveApplicationInitialised() {
        MenuItem menuFileImportPlaylist = findMenuItem("#menuFileImportPlaylist");
        MenuItem menuFileExportPlaylist = findMenuItem("#menuFileExportPlaylist");
        MenuItem menuFileSettings = findMenuItem("#menuFileSettings");
        MenuItem menuEditAddPlaylist = findMenuItem("#menuEditAddPlaylist");
        MenuItem menuEditDeletePlaylist = findMenuItem("#menuEditDeletePlaylist");
        MenuItem menuEditRandomPlaylist = findMenuItem("#menuEditRandomPlaylist");
        CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
        CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");
        CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
        CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
        CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");
        MenuItem menuControlsVolumeUp = findMenuItem("#menuControlsVolumeUp");
        MenuItem menuControlsVolumeDown = findMenuItem("#menuControlsVolumeDown");
        MenuItem menuControlsVolumeMute = findMenuItem("#menuControlsVolumeMute");
        MenuItem menuViewEqualizer = findMenuItem("#menuViewEqualizer");

        menuFileImportPlaylist.setDisable(true);
        menuFileExportPlaylist.setDisable(true);
        menuFileSettings.setDisable(true);
        menuEditAddPlaylist.setDisable(true);
        menuEditDeletePlaylist.setDisable(true);
        menuEditRandomPlaylist.setDisable(true);
        checkMenuControlsShuffleOff.setDisable(true);
        checkMenuControlsShuffleOn.setDisable(true);
        checkMenuControlsRepeatOff.setDisable(true);
        checkMenuControlsRepeatAll.setDisable(true);
        checkMenuControlsRepeatOne.setDisable(true);
        menuControlsVolumeUp.setDisable(true);
        menuControlsVolumeDown.setDisable(true);
        menuControlsVolumeMute.setDisable(true);
        menuViewEqualizer.setDisable(true);

        checkMenuControlsShuffleOff.setSelected(false);
        checkMenuControlsShuffleOn.setSelected(false);
        checkMenuControlsRepeatOff.setSelected(false);
        checkMenuControlsRepeatAll.setSelected(false);
        checkMenuControlsRepeatOne.setSelected(false);

        when(playlistManager.isShuffle()).thenReturn(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        threadRunner.runOnGui(() -> underTest.eventReceived(APPLICATION_INITIALISED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuFileImportPlaylist.isDisable()).isFalse();
        assertThat(menuFileExportPlaylist.isDisable()).isFalse();
        assertThat(menuFileSettings.isDisable()).isFalse();
        assertThat(menuEditAddPlaylist.isDisable()).isFalse();
        assertThat(menuEditDeletePlaylist.isDisable()).isFalse();
        assertThat(menuEditRandomPlaylist.isDisable()).isFalse();
        assertThat(checkMenuControlsShuffleOff.isDisable()).isFalse();
        assertThat(checkMenuControlsShuffleOn.isDisable()).isFalse();
        assertThat(checkMenuControlsRepeatOff.isDisable()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isDisable()).isFalse();
        assertThat(checkMenuControlsRepeatOne.isDisable()).isFalse();
        assertThat(menuControlsVolumeUp.isDisable()).isFalse();
        assertThat(menuControlsVolumeDown.isDisable()).isFalse();
        assertThat(menuControlsVolumeMute.isDisable()).isFalse();
        assertThat(menuViewEqualizer.isDisable()).isFalse();

        assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
        assertThat(checkMenuControlsRepeatAll.isSelected()).isTrue();
        assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaPlaying() {
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
        MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
        MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

        menuControlsPlayPause.setText("Test text");
        menuControlsPlayPause.setDisable(true);
        menuControlsPrevious.setDisable(true);
        menuControlsNext.setDisable(true);

        threadRunner.runOnGui(() -> underTest.eventReceived(MEDIA_PLAYING));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuControlsPlayPause.getText()).isEqualTo(messageManager.getMessage(MESSAGE_MENU_CONTROLS_PAUSE));
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
        assertThat(menuControlsPrevious.isDisable()).isFalse();
        assertThat(menuControlsNext.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaPaused() {
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
        MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
        MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

        menuControlsPlayPause.setText("Test text");
        menuControlsPlayPause.setDisable(true);
        menuControlsPrevious.setDisable(false);
        menuControlsNext.setDisable(false);

        threadRunner.runOnGui(() -> underTest.eventReceived(MEDIA_PAUSED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuControlsPlayPause.getText()).isEqualTo(messageManager.getMessage(MESSAGE_MENU_CONTROLS_PLAY));
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
        assertThat(menuControlsPrevious.isDisable()).isTrue();
        assertThat(menuControlsNext.isDisable()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveMediaStopped() {
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
        MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
        MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

        menuControlsPlayPause.setText("Test text");
        menuControlsPlayPause.setDisable(true);
        menuControlsPrevious.setDisable(false);
        menuControlsNext.setDisable(false);

        threadRunner.runOnGui(() -> underTest.eventReceived(MEDIA_STOPPED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuControlsPlayPause.getText()).isEqualTo(messageManager.getMessage(MESSAGE_MENU_CONTROLS_PLAY));
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
        assertThat(menuControlsPrevious.isDisable()).isTrue();
        assertThat(menuControlsNext.isDisable()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveEndOfMedia() {
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
        MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
        MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

        menuControlsPlayPause.setText("Test text");
        menuControlsPlayPause.setDisable(true);
        menuControlsPrevious.setDisable(false);
        menuControlsNext.setDisable(false);

        threadRunner.runOnGui(() -> underTest.eventReceived(END_OF_MEDIA));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuControlsPlayPause.getText()).isEqualTo(messageManager.getMessage(MESSAGE_MENU_CONTROLS_PLAY));
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
        assertThat(menuControlsPrevious.isDisable()).isTrue();
        assertThat(menuControlsNext.isDisable()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreated() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);
        when(mainPanelController.isPlaylistPlayable()).thenReturn(true);

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_CREATED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWhenPlaylistIsNotPlayable() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(false);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);
        when(mainPanelController.isPlaylistPlayable()).thenReturn(false);

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_CREATED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistCreatedWhenTrackIsNotSelected() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(false);
        menuControlsPlayPause.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(null);
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);
        when(mainPanelController.isPlaylistPlayable()).thenReturn(true);

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_CREATED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isTrue();
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistDeleted() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);
        when(mainPanelController.isPlaylistPlayable()).thenReturn(true);

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_DELETED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistSelected() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
        when(mediaManager.isPlaying()).thenReturn(false);
        when(mediaManager.isPaused()).thenReturn(false);
        when(mainPanelController.isPlaylistPlayable()).thenReturn(true);

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_SELECTED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceivePlaylistContentUpdated() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        menuEditCreatePlaylistFromAlbum.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

        threadRunner.runOnGui(() -> underTest.eventReceived(PLAYLIST_CONTENT_UPDATED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackSelected() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(true);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

        threadRunner.runOnGui(() -> underTest.eventReceived(TRACK_SELECTED));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReceiveTrackQueuedForPlaying() {
        MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
        MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

        menuEditCreatePlaylistFromAlbum.setDisable(true);
        menuControlsPlayPause.setDisable(false);

        when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

        threadRunner.runOnGui(() -> underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING));

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
        assertThat(menuControlsPlayPause.isDisable()).isTrue();
    }
}