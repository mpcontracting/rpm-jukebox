package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.scene.control.CheckMenuItem;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.manager.*;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;
import uk.co.mpcontracting.rpmjukebox.view.MenuView;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.event.Event.*;

public class MenuControllerTest extends AbstractGUITest {

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private MenuController menuController;

    @Autowired
    private MenuView menuView;

    @Mock
    private RpmJukebox mockRpmJukebox;

    @Mock
    private MainPanelController mockMainPanelController;

    @Mock
    private TrackTableController mockTrackTableController;

    @Mock
    private ApplicationManager mockApplicationManager;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private PlaylistManager mockPlaylistManager;

    @Mock
    private MediaManager mockMediaManager;

    @SneakyThrows
    @PostConstruct
    public void constructView() {
        init(menuView);
    }

    @Before
    public void setup() {
        setField(menuController, "eventManager", getMockEventManager());
        setField(menuController, "rpmJukebox", mockRpmJukebox);
        setField(menuController, "mainPanelController", mockMainPanelController);
        setField(menuController, "trackTableController", mockTrackTableController);
        setField(menuController, "applicationManager", mockApplicationManager);
        setField(menuController, "settingsManager", mockSettingsManager);
        setField(menuController, "playlistManager", mockPlaylistManager);
        setField(menuController, "mediaManager", mockMediaManager);

        when(mockSettingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        menuController.initialize();
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

        verify(mockApplicationManager, times(1)).stop();
        verify(mockRpmJukebox, times(1)).stop();
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
        when(mockPlaylistManager.isShuffle()).thenReturn(true);

        clickOnMenuItem("#checkMenuControlsShuffleOff");

        verify(mockPlaylistManager, times(1)).setShuffle(false, false);
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
        when(mockPlaylistManager.isShuffle()).thenReturn(false);

        clickOnMenuItem("#checkMenuControlsShuffleOff");

        verify(mockPlaylistManager, never()).setShuffle(false, false);
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
        when(mockPlaylistManager.isShuffle()).thenReturn(false);

        clickOnMenuItem("#checkMenuControlsShuffleOn");

        verify(mockPlaylistManager, times(1)).setShuffle(true, false);
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
        when(mockPlaylistManager.isShuffle()).thenReturn(true);

        clickOnMenuItem("#checkMenuControlsShuffleOn");

        verify(mockPlaylistManager, never()).setShuffle(true, false);
        verify(getMockEventManager(), never()).fireEvent(MENU_CONTROLS_SHUFFLE);

        assertThat(checkMenuControlsShuffleOff.isSelected()).isFalse();
        assertThat(checkMenuControlsShuffleOn.isSelected()).isTrue();
    }
}