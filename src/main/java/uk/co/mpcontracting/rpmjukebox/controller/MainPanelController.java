package uk.co.mpcontracting.rpmjukebox.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;

@Slf4j
@FXMLController
public class MainPanelController extends EventAwareObject {

  @FXML
  private Button newVersionButton;

  @FXML
  private ComboBox<YearFilter> yearFilterComboBox;

  @FXML
  private TextField searchTextField;

  @FXML
  private ListView<Playlist> playlistPanelListView;

  @FXML
  private Button addPlaylistButton;

  @FXML
  private Button deletePlaylistButton;

  @FXML
  private Button settingsButton;

  @FXML
  private Button importPlaylistButton;

  @FXML
  private Button exportPlaylistButton;

  @FXML
  private BorderPane mainPanel;

  @FXML
  private ImageView playingImageView;

  @FXML
  private Label playingTrackLabel;

  @FXML
  private Label playingArtistLabel;

  @FXML
  private Label playingAlbumLabel;

  @FXML
  private Button previousButton;

  @FXML
  private Button playPauseButton;

  @FXML
  private Button nextButton;

  @FXML
  private SliderProgressBar timeSlider;

  @FXML
  private Label playTimeLabel;

  @FXML
  private Button volumeButton;

  @FXML
  private Slider volumeSlider;

  @FXML
  private Button shuffleButton;

  @FXML
  private Button repeatButton;

  @FXML
  private Button eqButton;

  @FXML
  private Button randomButton;

  public void showConfirmView(String message, boolean blurBackground, Runnable okRunnable, Runnable cancelRunnable) {

  }

  @FXML
  protected void handleNewVersionButtonAction() {

  }

  @FXML
  protected void handleAddPlaylistButtonAction() {

  }

  @FXML
  protected void handleDeletePlaylistButtonAction() {

  }

  @FXML
  protected void handleSettingsButtonAction() {

  }

  @FXML
  protected void handleImportPlaylistButtonAction() {

  }

  @FXML
  protected void handleExportPlaylistButtonAction() {

  }

  @FXML
  protected void handlePreviousButtonAction() {

  }

  @FXML
  protected void handlePlayPauseButtonAction() {

  }

  @FXML
  protected void handleNextButtonAction() {

  }

  @FXML
  protected void handleVolumeButtonAction() {

  }

  @FXML
  protected void handleShuffleButtonAction() {

  }

  @FXML
  protected void handleRepeatButtonAction() {

  }

  @FXML
  protected void handleEqButtonAction() {

  }

  @FXML
  protected void handleRandomButtonAction() {

  }
}
