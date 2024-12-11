package uk.co.mpcontracting.rpmjukebox.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.controller.MenuController.VOLUME_DELTA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.END_OF_MEDIA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PAUSED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_STOPPED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_NEXT;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_PLAY_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_PREVIOUS;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_REPEAT;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_SHUFFLE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_DOWN;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_MUTE;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_CONTROLS_VOLUME_UP;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_ADD_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_DELETE_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_EDIT_RANDOM_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_EXPORT_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_IMPORT_PLAYLIST;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_FILE_SETTINGS;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MENU_VIEW_EQUALIZER;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CONTENT_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CREATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_DELETED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_QUEUED_FOR_PLAYING;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ALL;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.OFF;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ONE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_MENU_CONTROLS_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_MENU_CONTROLS_PLAY;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.WINDOWS;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.ApplicationLifecycleService;
import uk.co.mpcontracting.rpmjukebox.service.MediaService;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;
import uk.co.mpcontracting.rpmjukebox.view.MenuView;

class MenuControllerTest extends AbstractGuiTest {

  @MockBean
  private RpmJukebox rpmJukebox;

  @MockBean
  private MainPanelController mainPanelController;

  @MockBean
  private TrackTableController trackTableController;

  @MockBean
  private ApplicationLifecycleService applicationLifecycleService;

  @MockBean
  private MediaService mediaService;

  @MockBean
  private PlaylistService playlistService;

  @MockBean
  private SettingsService settingsService;

  @Autowired
  private StringResourceService stringResourceService;

  @Autowired
  private MenuView menuView;

  @Autowired
  private MenuController underTest;

  @SneakyThrows
  @PostConstruct
  void postConstruct() {
    init(menuView);
  }

  @BeforeEach
  void beforeEach() {
    when(settingsService.getOsType()).thenReturn(WINDOWS);

    underTest.initialize();
  }

  @Test
  void shouldHandleFileImportPlaylistAction() {
    clickOnMenuItem("#menuFileImportPlaylist");

    verify(eventProcessor).fireEvent(MENU_FILE_IMPORT_PLAYLIST);
  }

  @Test
  void shouldHandleFileExportPlaylistAction() {
    clickOnMenuItem("#menuFileExportPlaylist");

    verify(eventProcessor).fireEvent(MENU_FILE_EXPORT_PLAYLIST);
  }

  @Test
  void shouldHandleFileSettingsAction() {
    clickOnMenuItem("#menuFileSettings");

    verify(eventProcessor).fireEvent(MENU_FILE_SETTINGS);
  }

  @Test
  @SneakyThrows
  void shouldHandleFileExitAction() {
    clickOnMenuItem("#menuFileExit");

    verify(applicationLifecycleService).stop();
    verify(rpmJukebox).stop();
  }

  @Test
  void shouldHandleEditAddPlaylistAction() {
    clickOnMenuItem("#menuEditAddPlaylist");

    verify(eventProcessor).fireEvent(MENU_EDIT_ADD_PLAYLIST);
  }

  @Test
  void shouldHandleEditDeletePlaylistAction() {
    clickOnMenuItem("#menuEditDeletePlaylist");

    verify(eventProcessor).fireEvent(MENU_EDIT_DELETE_PLAYLIST);
  }

  @Test
  void shouldHandleEditCreatePlaylistFromAlbumAction() {
    clickOnMenuItem("#menuEditCreatePlaylistFromAlbum");

    verify(eventProcessor).fireEvent(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);
  }

  @Test
  void shouldHandleEditRandomPlaylistAction() {
    clickOnMenuItem("#menuEditRandomPlaylist");

    verify(eventProcessor).fireEvent(MENU_EDIT_RANDOM_PLAYLIST);
  }

  @Test
  void shouldHandleControlsPlayPauseAction() {
    clickOnMenuItem("#menuControlsPlayPause");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_PLAY_PAUSE);
  }

  @Test
  void shouldHandleControlsPreviousAction() {
    clickOnMenuItem("#menuControlsPrevious");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_PREVIOUS);
  }

  @Test
  void shouldHandleControlsNextAction() {
    clickOnMenuItem("#menuControlsNext");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_NEXT);
  }

  @Test
  void shouldHandleControlsShuffleOffActionIfShuffleIsOn() {
    CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
    CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

    checkMenuControlsShuffleOff.setSelected(true);
    checkMenuControlsShuffleOn.setSelected(false);
    when(playlistService.isShuffle()).thenReturn(true);

    clickOnMenuItem("#checkMenuControlsShuffleOff");

    verify(playlistService).setShuffle(false, false);
    verify(eventProcessor).fireEvent(MENU_CONTROLS_SHUFFLE);

    assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
    assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
  }

  @Test
  void shouldHandleControlsShuffleOffActionIfShuffleIsOff() {
    CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
    CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

    checkMenuControlsShuffleOff.setSelected(false);
    checkMenuControlsShuffleOn.setSelected(true);
    when(playlistService.isShuffle()).thenReturn(false);

    clickOnMenuItem("#checkMenuControlsShuffleOff");

    verify(playlistService, never()).setShuffle(false, false);
    verify(eventProcessor, never()).fireEvent(MENU_CONTROLS_SHUFFLE);

    assertThat(checkMenuControlsShuffleOff.isSelected()).isTrue();
    assertThat(checkMenuControlsShuffleOn.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsShuffleOnActionIfShuffleIsOff() {
    CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
    CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

    checkMenuControlsShuffleOff.setSelected(false);
    checkMenuControlsShuffleOn.setSelected(true);
    when(playlistService.isShuffle()).thenReturn(false);

    clickOnMenuItem("#checkMenuControlsShuffleOn");

    verify(playlistService).setShuffle(true, false);
    verify(eventProcessor).fireEvent(MENU_CONTROLS_SHUFFLE);

    assertThat(checkMenuControlsShuffleOff.isSelected()).isTrue();
    assertThat(checkMenuControlsShuffleOn.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsShuffleOnActionIfShuffleIsOn() {
    CheckMenuItem checkMenuControlsShuffleOff = findCheckMenuItem("#checkMenuControlsShuffleOff");
    CheckMenuItem checkMenuControlsShuffleOn = findCheckMenuItem("#checkMenuControlsShuffleOn");

    checkMenuControlsShuffleOff.setSelected(true);
    checkMenuControlsShuffleOn.setSelected(false);
    when(playlistService.isShuffle()).thenReturn(true);

    clickOnMenuItem("#checkMenuControlsShuffleOn");

    verify(playlistService, never()).setShuffle(true, false);
    verify(eventProcessor, never()).fireEvent(MENU_CONTROLS_SHUFFLE);

    assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
    assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
  }

  @Test
  void shouldHandleControlsRepeatOffActionIfRepeatIsAll() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(true);
    checkMenuControlsRepeatAll.setSelected(false);
    checkMenuControlsRepeatOne.setSelected(false);
    when(playlistService.getRepeat()).thenReturn(ALL);

    clickOnMenuItem("#checkMenuControlsRepeatOff");

    verify(playlistService).setRepeat(OFF);
    verify(eventProcessor).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isTrue();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsRepeatOffActionIfRepeatIsOff() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(false);
    checkMenuControlsRepeatAll.setSelected(true);
    checkMenuControlsRepeatOne.setSelected(false);
    when(playlistService.getRepeat()).thenReturn(OFF);

    clickOnMenuItem("#checkMenuControlsRepeatOff");

    verify(playlistService, never()).setRepeat(OFF);
    verify(eventProcessor, never()).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isTrue();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsRepeatAllActionIfRepeatIsOne() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(false);
    checkMenuControlsRepeatAll.setSelected(true);
    checkMenuControlsRepeatOne.setSelected(false);
    when(playlistService.getRepeat()).thenReturn(ONE);

    clickOnMenuItem("#checkMenuControlsRepeatAll");

    verify(playlistService).setRepeat(ALL);
    verify(eventProcessor).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isTrue();
  }

  @Test
  void shouldHandleControlsRepeatAllActionIfRepeatIsAll() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(false);
    checkMenuControlsRepeatAll.setSelected(false);
    checkMenuControlsRepeatOne.setSelected(true);
    when(playlistService.getRepeat()).thenReturn(ALL);

    clickOnMenuItem("#checkMenuControlsRepeatAll");

    verify(playlistService, never()).setRepeat(OFF);
    verify(eventProcessor, never()).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isTrue();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsRepeatOneActionIfRepeatIsOff() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(false);
    checkMenuControlsRepeatAll.setSelected(false);
    checkMenuControlsRepeatOne.setSelected(true);
    when(playlistService.getRepeat()).thenReturn(OFF);

    clickOnMenuItem("#checkMenuControlsRepeatOne");

    verify(playlistService).setRepeat(ONE);
    verify(eventProcessor).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isTrue();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isFalse();
  }

  @Test
  void shouldHandleControlsRepeatOneActionIfRepeatIsOne() {
    CheckMenuItem checkMenuControlsRepeatOff = findCheckMenuItem("#checkMenuControlsRepeatOff");
    CheckMenuItem checkMenuControlsRepeatAll = findCheckMenuItem("#checkMenuControlsRepeatAll");
    CheckMenuItem checkMenuControlsRepeatOne = findCheckMenuItem("#checkMenuControlsRepeatOne");

    checkMenuControlsRepeatOff.setSelected(true);
    checkMenuControlsRepeatAll.setSelected(false);
    checkMenuControlsRepeatOne.setSelected(false);
    when(playlistService.getRepeat()).thenReturn(ONE);

    clickOnMenuItem("#checkMenuControlsRepeatOne");

    verify(playlistService, never()).setRepeat(ONE);
    verify(eventProcessor, never()).fireEvent(MENU_CONTROLS_REPEAT);

    assertThat(checkMenuControlsRepeatOff.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatAll.isSelected()).isFalse();
    assertThat(checkMenuControlsRepeatOne.isSelected()).isTrue();
  }

  @Test
  void shouldHandleControlsVolumeUpAction() {
    clickOnMenuItem("#menuControlsVolumeUp");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_VOLUME_UP, VOLUME_DELTA);
  }

  @Test
  void shouldHandleControlsVolumeDownAction() {
    clickOnMenuItem("#menuControlsVolumeDown");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_VOLUME_DOWN, VOLUME_DELTA);
  }

  @Test
  void shouldHandleControlsVolumeMuteAction() {
    clickOnMenuItem("#menuControlsVolumeMute");

    verify(eventProcessor).fireEvent(MENU_CONTROLS_VOLUME_MUTE);
  }

  @Test
  void shouldHandleViewEqualizerAction() {
    clickOnMenuItem("#menuViewEqualizer");

    verify(eventProcessor).fireEvent(MENU_VIEW_EQUALIZER);
  }

  @Test
  @SneakyThrows
  void shouldReceiveApplicationInitialised() {
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

    when(playlistService.isShuffle()).thenReturn(true);
    when(playlistService.getRepeat()).thenReturn(ALL);

    Platform.runLater(() -> underTest.eventReceived(APPLICATION_INITIALISED));

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
  void shouldReceiveMediaPlaying() {
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
    MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
    MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

    menuControlsPlayPause.setText("Test text");
    menuControlsPlayPause.setDisable(true);
    menuControlsPrevious.setDisable(true);
    menuControlsNext.setDisable(true);

    Platform.runLater(() -> underTest.eventReceived(MEDIA_PLAYING));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuControlsPlayPause.getText()).isEqualTo(stringResourceService.getString(MESSAGE_MENU_CONTROLS_PAUSE));
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
    assertThat(menuControlsPrevious.isDisable()).isFalse();
    assertThat(menuControlsNext.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceiveMediaPaused() {
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
    MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
    MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

    menuControlsPlayPause.setText("Test text");
    menuControlsPlayPause.setDisable(true);
    menuControlsPrevious.setDisable(false);
    menuControlsNext.setDisable(false);

    Platform.runLater(() -> underTest.eventReceived(MEDIA_PAUSED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuControlsPlayPause.getText()).isEqualTo(stringResourceService.getString(MESSAGE_MENU_CONTROLS_PLAY));
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
    assertThat(menuControlsPrevious.isDisable()).isTrue();
    assertThat(menuControlsNext.isDisable()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReceiveMediaStopped() {
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
    MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
    MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

    menuControlsPlayPause.setText("Test text");
    menuControlsPlayPause.setDisable(true);
    menuControlsPrevious.setDisable(false);
    menuControlsNext.setDisable(false);

    Platform.runLater(() -> underTest.eventReceived(MEDIA_STOPPED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuControlsPlayPause.getText()).isEqualTo(stringResourceService.getString(MESSAGE_MENU_CONTROLS_PLAY));
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
    assertThat(menuControlsPrevious.isDisable()).isTrue();
    assertThat(menuControlsNext.isDisable()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReceiveEndOfMedia() {
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");
    MenuItem menuControlsPrevious = findMenuItem("#menuControlsPrevious");
    MenuItem menuControlsNext = findMenuItem("#menuControlsNext");

    menuControlsPlayPause.setText("Test text");
    menuControlsPlayPause.setDisable(true);
    menuControlsPrevious.setDisable(false);
    menuControlsNext.setDisable(false);

    Platform.runLater(() -> underTest.eventReceived(END_OF_MEDIA));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuControlsPlayPause.getText()).isEqualTo(stringResourceService.getString(MESSAGE_MENU_CONTROLS_PLAY));
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
    assertThat(menuControlsPrevious.isDisable()).isTrue();
    assertThat(menuControlsNext.isDisable()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistCreated() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);
    when(mainPanelController.isPlaylistUnplayable()).thenReturn(false);

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_CREATED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistCreatedWhenPlaylistIsNotPlayable() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(false);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);
    when(mainPanelController.isPlaylistUnplayable()).thenReturn(true);

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_CREATED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistCreatedWhenTrackIsNotSelected() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(false);
    menuControlsPlayPause.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(null);
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);
    when(mainPanelController.isPlaylistUnplayable()).thenReturn(false);

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_CREATED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isTrue();
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistDeleted() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);
    when(mainPanelController.isPlaylistUnplayable()).thenReturn(false);

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_DELETED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistSelected() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));
    when(mediaService.isPlaying()).thenReturn(false);
    when(mediaService.isPaused()).thenReturn(false);
    when(mainPanelController.isPlaylistUnplayable()).thenReturn(false);

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_SELECTED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceivePlaylistContentUpdated() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    menuEditCreatePlaylistFromAlbum.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

    Platform.runLater(() -> underTest.eventReceived(PLAYLIST_CONTENT_UPDATED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceiveTrackSelected() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(true);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

    Platform.runLater(() -> underTest.eventReceived(TRACK_SELECTED));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReceiveTrackQueuedForPlaying() {
    MenuItem menuEditCreatePlaylistFromAlbum = findMenuItem("#menuEditCreatePlaylistFromAlbum");
    MenuItem menuControlsPlayPause = findMenuItem("#menuControlsPlayPause");

    menuEditCreatePlaylistFromAlbum.setDisable(true);
    menuControlsPlayPause.setDisable(false);

    when(trackTableController.getSelectedTrack()).thenReturn(mock(Track.class));

    Platform.runLater(() -> underTest.eventReceived(TRACK_QUEUED_FOR_PLAYING));

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(menuEditCreatePlaylistFromAlbum.isDisable()).isFalse();
    assertThat(menuControlsPlayPause.isDisable()).isTrue();
  }
}