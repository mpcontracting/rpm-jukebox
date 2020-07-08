package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.support.OsType;

import static uk.co.mpcontracting.rpmjukebox.event.Event.*;
import static uk.co.mpcontracting.rpmjukebox.support.Constants.MESSAGE_MENU_FILE_EXIT;

@Slf4j
@FXMLController
@RequiredArgsConstructor
public class MenuController extends EventAwareObject {

    @FXML
    private Menu menuFile;

    @FXML
    private MenuItem menuFileImportPlaylist;

    @FXML
    private MenuItem menuFileExportPlaylist;

    @FXML
    private MenuItem menuFileSettings;

    @FXML
    private MenuItem menuEditAddPlaylist;

    @FXML
    private MenuItem menuEditDeletePlaylist;

    @FXML
    private MenuItem menuEditCreatePlaylistFromAlbum;

    @FXML
    private MenuItem menuEditRandomPlaylist;

    @FXML
    private MenuItem menuControlsPlayPause;

    @FXML
    private MenuItem menuControlsPrevious;

    @FXML
    private MenuItem menuControlsNext;

    @FXML
    private CheckMenuItem checkMenuControlsShuffleOff;

    @FXML
    private CheckMenuItem checkMenuControlsShuffleOn;

    @FXML
    private CheckMenuItem checkMenuControlsRepeatOff;

    @FXML
    private CheckMenuItem checkMenuControlsRepeatAll;

    @FXML
    private CheckMenuItem checkMenuControlsRepeatOne;

    @FXML
    private MenuItem menuControlsVolumeUp;

    @FXML
    private MenuItem menuControlsVolumeDown;

    @FXML
    private MenuItem menuViewEqualizer;

    private final MessageManager messageManager;

    private SettingsManager settingsManager;
    private PlaylistManager playlistManager;

    @Autowired
    private void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    private void wirePlaylistManager(PlaylistManager playlistManager) {
        this.playlistManager = playlistManager;
    }

    @FXML
    public void initialize() {
        log.info("Initialising MenuController");

        if (settingsManager.getOsType() != OsType.OSX) {
            menuFile.getItems().add(new SeparatorMenuItem());

            MenuItem exitMenuItem = new MenuItem(messageManager.getMessage(MESSAGE_MENU_FILE_EXIT));
            exitMenuItem.acceleratorProperty().setValue(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));

            menuFile.getItems().add(exitMenuItem);
        }
    }

    @FXML
    protected void handleFileImportPlaylistAction() {
        log.debug("Handling file import playlist action");

        fireEvent(MENU_FILE_IMPORT_PLAYLIST);
    }

    @FXML
    protected void handleFileExportPlaylistAction() {
        log.debug("Handling file export playlist action");

        fireEvent(MENU_FILE_EXPORT_PLAYLIST);
    }

    @FXML
    protected void handleFileSettingsAction() {
        log.debug("Handling file settings action");

        fireEvent(MENU_FILE_SETTINGS);
    }

    @FXML
    protected void handleEditAddPlaylistAction() {
        log.debug("Handling edit add playlist action");

        fireEvent(MENU_EDIT_ADD_PLAYLIST);
    }

    @FXML
    protected void handleEditDeletePlaylistAction() {
        log.debug("Handling edit delete playlist action");

        fireEvent(MENU_EDIT_DELETE_PLAYLIST);
    }

    @FXML
    protected void handleEditCreatePlaylistFromAlbumAction() {
        log.debug("Handling edit create playlist from album action");

        fireEvent(MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM);
    }

    @FXML
    protected void handleEditRandomPlaylistAction() {
        log.debug("Handling edit random playlist action");

        fireEvent(MENU_EDIT_RANDOM_PLAYLIST);
    }

    @FXML
    protected void handleControlsPlayPauseAction() {
        log.debug("Handling controls play pause action");

        fireEvent(MENU_CONTROLS_PLAY_PAUSE);
    }

    @FXML
    protected void handleControlsPreviousAction() {
        log.debug("Handling controls previous action");

        fireEvent(MENU_CONTROLS_PREVIOUS);
    }

    @FXML
    protected void handleControlsNextAction() {
        log.debug("Handling controls next action");

        fireEvent(MENU_CONTROLS_NEXT);
    }

    @FXML
    protected void handleControlsShuffleOffAction() {
        log.debug("Handling controls shuffle off action");

        if (playlistManager.isShuffle()) {
            playlistManager.setShuffle(false, false);

            fireEvent(MENU_CONTROLS_SHUFFLE);
        }

        updateShuffle();
    }

    @FXML
    protected void handleControlsShuffleOnAction() {
        log.debug("Handling controls shuffle on action");

        if (!playlistManager.isShuffle()) {
            playlistManager.setShuffle(true, false);

            fireEvent(MENU_CONTROLS_SHUFFLE);
        }

        updateShuffle();
    }

    @FXML
    protected void handleControlsRepeatOffAction() {
        log.debug("Handling controls repeat off action");

        if (playlistManager.getRepeat() != Repeat.OFF) {
            playlistManager.setRepeat(Repeat.OFF);

            fireEvent(MENU_CONTROLS_REPEAT);
        }

        updateRepeat();
    }

    @FXML
    protected void handleControlsRepeatAllAction() {
        log.debug("Handling controls repeat all action");

        if (playlistManager.getRepeat() != Repeat.ALL) {
            playlistManager.setRepeat(Repeat.ALL);

            fireEvent(MENU_CONTROLS_REPEAT);
        }

        updateRepeat();
    }

    @FXML
    protected void handleControlsRepeatOneAction() {
        log.debug("Handling controls repeat one action");

        if (playlistManager.getRepeat() != Repeat.ONE) {
            playlistManager.setRepeat(Repeat.ONE);

            fireEvent(MENU_CONTROLS_REPEAT);
        }

        updateRepeat();
    }

    @FXML
    protected void handleControlsVolumeUpAction() {
        log.debug("Handling controls volume up action");

        fireEvent(MENU_CONTROLS_VOLUME_UP, 10d);
    }

    @FXML
    protected void handleControlsVolumeDownAction() {
        log.debug("Handling controls volume down action");

        fireEvent(MENU_CONTROLS_VOLUME_DOWN, 10d);
    }

    @FXML
    protected void handleViewEqualizerAction() {
        log.debug("Handling view equalizer action");

        fireEvent(MENU_CONTROLS_VIEW_EQUALIZER);
    }

    private void updateShuffle() {
        if (playlistManager.isShuffle()) {
            checkMenuControlsShuffleOff.setSelected(false);
            checkMenuControlsShuffleOn.setSelected(true);
        } else {
            checkMenuControlsShuffleOff.setSelected(true);
            checkMenuControlsShuffleOn.setSelected(false);
        }
    }

    private void updateRepeat() {
        switch (playlistManager.getRepeat()) {
            case OFF: {
                checkMenuControlsRepeatOff.setSelected(true);
                checkMenuControlsRepeatAll.setSelected(false);
                checkMenuControlsRepeatOne.setSelected(false);
                break;
            }
            case ALL: {
                checkMenuControlsRepeatOff.setSelected(false);
                checkMenuControlsRepeatAll.setSelected(true);
                checkMenuControlsRepeatOne.setSelected(false);
                break;
            }
            case ONE: {
                checkMenuControlsRepeatOff.setSelected(false);
                checkMenuControlsRepeatAll.setSelected(false);
                checkMenuControlsRepeatOne.setSelected(true);
                break;
            }
        }
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch(event) {
            case APPLICATION_INITIALISED: {
                // Enable menu items
                menuFileImportPlaylist.setDisable(false);
                menuFileExportPlaylist.setDisable(false);
                menuFileSettings.setDisable(false);
                menuEditAddPlaylist.setDisable(false);
                menuEditDeletePlaylist.setDisable(false);
                menuEditRandomPlaylist.setDisable(false);
                checkMenuControlsShuffleOff.setDisable(false);
                checkMenuControlsShuffleOn.setDisable(false);
                checkMenuControlsRepeatOff.setDisable(false);
                checkMenuControlsRepeatAll.setDisable(false);
                checkMenuControlsRepeatOne.setDisable(false);
                menuControlsVolumeUp.setDisable(false);
                menuControlsVolumeDown.setDisable(false);
                menuViewEqualizer.setDisable(false);

                // Update shuffle and repeat menu entries
                updateShuffle();
                updateRepeat();

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
