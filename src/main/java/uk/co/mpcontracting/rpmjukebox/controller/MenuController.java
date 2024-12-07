package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;

@Slf4j
@FXMLController
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
  private MenuItem menuControlsVolumeMute;

  @FXML
  private MenuItem menuViewEqualizer;

  @FXML
  protected void handleFileImportPlaylistAction() {

  }

  @FXML
  protected void handleFileExportPlaylistAction() {

  }

  @FXML
  protected void handleFileSettingsAction() {

  }

  @FXML
  protected void handleEditAddPlaylistAction() {

  }

  @FXML
  protected void handleEditDeletePlaylistAction() {

  }

  @FXML
  protected void handleEditCreatePlaylistFromAlbumAction() {

  }

  @FXML
  protected void handleEditRandomPlaylistAction() {

  }

  @FXML
  protected void handleControlsPlayPauseAction() {

  }

  @FXML
  protected void handleControlsPreviousAction() {

  }

  @FXML
  protected void handleControlsNextAction() {

  }

  @FXML
  protected void handleControlsShuffleOffAction() {

  }

  @FXML
  protected void handleControlsShuffleOnAction() {

  }

  @FXML
  protected void handleControlsRepeatOffAction() {

  }

  @FXML
  protected void handleControlsRepeatAllAction() {

  }

  @FXML
  protected void handleControlsRepeatOneAction() {

  }

  @FXML
  protected void handleControlsVolumeUpAction() {

  }

  @FXML
  protected void handleControlsVolumeDownAction() {

  }

  @FXML
  protected void handleControlsVolumeMuteAction() {

  }

  @FXML
  protected void handleViewEqualizerAction() {

  }
}
