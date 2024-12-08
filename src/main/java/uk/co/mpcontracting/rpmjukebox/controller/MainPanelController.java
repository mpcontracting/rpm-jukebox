package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.co.mpcontracting.rpmjukebox.event.Event.MEDIA_STOPPED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CREATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_DELETED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ONE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_PAUSE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_PLAY;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_ALL;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_REPEAT_ONE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_SHUFFLE_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_SHUFFLE_ON;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_VOLUME_OFF;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_VOLUME_ON;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_IMPORT_PLAYLIST_TITLE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_NEW_VERSION_AVAILABLE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_YEAR_FILTER_NONE;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import com.google.gson.reflect.TypeToken;
import com.igormaznitsa.commons.version.Version;
import de.felixroske.jfxsupport.FXMLController;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.ImageFactory;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.service.MediaService;
import uk.co.mpcontracting.rpmjukebox.service.NativeService;
import uk.co.mpcontracting.rpmjukebox.service.PlaylistService;
import uk.co.mpcontracting.rpmjukebox.service.SearchService;
import uk.co.mpcontracting.rpmjukebox.service.SettingsService;
import uk.co.mpcontracting.rpmjukebox.service.StringResourceService;
import uk.co.mpcontracting.rpmjukebox.service.UpdateService;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.StringHelper;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;
import uk.co.mpcontracting.rpmjukebox.view.MessageView;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

@Slf4j
@FXMLController
@RequiredArgsConstructor
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

  private final ApplicationProperties applicationProperties;

  private final ImageFactory imageFactory;

  private final ConfirmView confirmView;
  private final EqualizerView equalizerView;
  private final ExportView exportView;
  private final MessageView messageView;
  private final SettingsView settingsView;
  private final TrackTableView trackTableView;

  private final EqualizerController equalizerController;
  private final ExportController exportController;
  private final SettingsController settingsController;
  private final TrackTableController trackTableController;

  private final CacheService cacheService;
  private final MediaService mediaService;
  private final NativeService nativeService;
  private final PlaylistService playlistService;
  private final SearchService searchService;
  private final SettingsService settingsService;
  private final StringResourceService stringResourceService;
  private final UpdateService updateService;

  private ObservableList<Playlist> observablePlaylists;
  private String playlistExtensionFilter;
  private int currentSelectedPlaylistId;

  @FXML
  public void initialize() {
    log.info("Initialising MainPanelController");

    yearFilterComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
        searchParametersUpdated(searchTextField.getText(), yearFilterComboBox.getSelectionModel().getSelectedItem(), false));

    searchTextField.textProperty().addListener((observable, oldValue, newValue) ->
        searchParametersUpdated(newValue, yearFilterComboBox.getSelectionModel().getSelectedItem(), true));

    timeSlider.sliderValueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
      if (!isChanging) {
        mediaService.setSeekPositionPercent(timeSlider.getSliderValue());
      }
    });

    timeSlider.sliderValueProperty().addListener((observable, oldValue, newValue) -> {
      if (!timeSlider.isSliderValueChanging()) {
        double sliderPercent = newValue.doubleValue();
        double trackPercent = mediaService.getPlayingTimePercent();

        if (Math.abs(trackPercent - sliderPercent) > 0.5) {
          mediaService.setSeekPositionPercent(timeSlider.getSliderValue());
        }
      }
    });

    volumeSlider.valueProperty().addListener(observable -> {
      if (volumeSlider.isValueChanging()) {
        mediaService.setVolumePercent(volumeSlider.getValue());
      }
    });

    playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
    volumeSlider.setValue(mediaService.getVolume() * 100);

    // Playlist list view
    observablePlaylists = FXCollections.observableArrayList();
    playlistPanelListView.setCellFactory(new PlaylistListCellFactory(stringResourceService, playlistService, this));
    playlistPanelListView.setEditable(true);
    playlistPanelListView.setItems(observablePlaylists);
    playlistPanelListView.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
        Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();

        showConfirmView(stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()),
            true, () -> playlistService.deletePlaylist(playlist.getPlaylistId()), null);
      }
    });
    playlistPanelListView.getSelectionModel().selectedItemProperty()
        .addListener(((observable, oldValue, newValue) -> ofNullable(newValue)
            .ifPresent(playlist -> fireEvent(PLAYLIST_SELECTED, playlist.getPlaylistId()))));

    // Track table view
    mainPanel.setCenter(trackTableView.getView());

    playlistExtensionFilter = "*." + applicationProperties.getPlaylistFileExtension();
    currentSelectedPlaylistId = -999;
  }

  private void searchParametersUpdated(String searchText, YearFilter yearFilter, boolean searchTextUpdated) {
    log.debug("Search parameters updated - '{}' - {}", searchText, yearFilter);

    if (nonNull(searchText) && !searchText.trim().isEmpty()) {
      TrackFilter trackFilter = null;
      TrackSearch trackSearch;

      if (nonNull(yearFilter) && nonNull(yearFilter.year()) && !yearFilter.year().trim().isEmpty()) {
        trackFilter = new TrackFilter(null, yearFilter.year());
      }

      if (nonNull(trackFilter)) {
        trackSearch = new TrackSearch(searchText.trim(), trackFilter);
      } else {
        trackSearch = new TrackSearch(searchText.trim());
      }

      playlistService.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchService.search(trackSearch));
    } else if (nonNull(playlistService.getPlayingPlaylist()) && playlistService.getPlayingPlaylist().getPlaylistId() == PLAYLIST_ID_SEARCH) {
      playlistService.setPlaylistTracks(PLAYLIST_ID_SEARCH, playlistService.getPlayingPlaylist().getTracks());
    } else {
      playlistService.setPlaylistTracks(PLAYLIST_ID_SEARCH, emptyList());
    }

    if (searchTextUpdated) {
      fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
    }
  }

  protected void showMessageView(String message, boolean blurBackground) {
    messageView.setMessage(message);

    if (!messageView.isShowing()) {
      messageView.show(blurBackground);
    }
  }

  protected void closeMessageView() {
    messageView.close();
  }

  public void showConfirmView(String message, boolean blurBackground, Runnable okRunnable, Runnable cancelRunnable) {
    confirmView.setMessage(message);
    confirmView.setRunnables(okRunnable, cancelRunnable);

    if (!confirmView.isShowing()) {
      confirmView.show(blurBackground);
    }
  }

  protected void updateYearFilter() {
    log.debug("Updating year filter - {}", searchService.getYearList());

    List<YearFilter> yearFilters = new ArrayList<>();
    yearFilters.add(new YearFilter(stringResourceService.getString(MESSAGE_YEAR_FILTER_NONE), null));

    ofNullable(searchService.getYearList())
        .ifPresent(years -> years.forEach(year -> yearFilters.add(new YearFilter(year, year))));

    ofNullable(yearFilterComboBox).ifPresent(comboBox -> {
      comboBox.getItems().clear();
      comboBox.getItems().addAll(yearFilters);
      comboBox.getSelectionModel().selectFirst();
    });
  }

  protected void updateObservablePlaylists() {
    log.debug("Updating observable playlists");

    observablePlaylists.setAll(playlistService.getPlaylists());
  }

  protected void setVolumeButtonImage() {
    if (mediaService.isMuted()) {
      volumeButton.setStyle("-fx-background-image: url('" + IMAGE_VOLUME_OFF + "')");
    } else {
      volumeButton.setStyle("-fx-background-image: url('" + IMAGE_VOLUME_ON + "')");
    }
  }

  protected void setShuffleButtonImage() {
    if (playlistService.isShuffle()) {
      shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
    } else {
      shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
    }
  }

  protected void setRepeatButtonImage() {
    switch (playlistService.getRepeat()) {
      case OFF -> repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");
      case ALL -> repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_ALL + "')");
      case ONE -> repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_ONE + "')");
    }
  }

  private boolean isPlaylistPlayable() {
    return playlistService.getPlaylist(currentSelectedPlaylistId).filter(playlist -> !playlist.isEmpty()).isPresent();
  }

  @FXML
  protected void handleNewVersionButtonAction() {
    log.debug("New version button pressed");

    updateService.downloadNewVersion();
  }

  @FXML
  protected void handleAddPlaylistButtonAction() {
    log.debug("Add playlist button pressed");

    playlistService.createPlaylist();
  }

  @FXML
  protected void handleDeletePlaylistButtonAction() {
    log.debug("Delete playlist button pressed");

    Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();

    if (nonNull(playlist) && playlist.getPlaylistId() > 0) {
      showConfirmView(
          stringResourceService.getString(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()),
          true,
          () -> playlistService.deletePlaylist(playlist.getPlaylistId()),
          null
      );
    }
  }

  @FXML
  protected void handleSettingsButtonAction() {
    log.debug("Settings button pressed");

    settingsController.bindSystemSettings();
    settingsView.show(true);
  }

  @FXML
  protected void handleImportPlaylistButtonAction() {
    log.debug("Import playlist button pressed");

    FileChooser fileChooser = constructFileChooser();
    fileChooser.setTitle(stringResourceService.getString(MESSAGE_IMPORT_PLAYLIST_TITLE));
    fileChooser.getExtensionFilters()
        .add(new ExtensionFilter(
            stringResourceService.getString(MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER, playlistExtensionFilter),
            playlistExtensionFilter));

    RpmJukebox.getStage().getScene().getRoot().setEffect(new BoxBlur());

    int currentPlaylistSelection = playlistPanelListView.getSelectionModel().getSelectedIndex();

    File file = fileChooser.showOpenDialog(RpmJukebox.getStage());

    if (nonNull(file)) {
      try (FileReader fileReader = constructFileReader(file)) {
        List<PlaylistSettings> playlists = settingsService.getGson().fromJson(fileReader,
            new TypeToken<ArrayList<PlaylistSettings>>() {
            }.getType());

        if (nonNull(playlists)) {
          playlists.forEach(playlistSettings -> {
            Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(),
                applicationProperties.getMaxPlaylistSize());

            playlistSettings.getTracks().forEach(trackId ->
                searchService.getTrackById(trackId).ifPresent(playlist::addTrack));

            playlistService.addPlaylist(playlist);
          });

          // Update the observable lists
          updateObservablePlaylists();

          // Select the last selected playlist
          playlistPanelListView.getSelectionModel().select(currentPlaylistSelection);
          playlistPanelListView.getFocusModel().focus(currentPlaylistSelection);
        }
      } catch (Exception e) {
        log.error("Unable to import playlists file - {}", file.getAbsolutePath(), e);

        RpmJukebox.getStage().getScene().getRoot().setEffect(null);

        return;
      }
    }

    RpmJukebox.getStage().getScene().getRoot().setEffect(null);
  }

  protected FileChooser constructFileChooser() {
    return new FileChooser();
  }

  protected FileReader constructFileReader(File file) throws Exception {
    return new FileReader(file);
  }

  @FXML
  protected void handleExportPlaylistButtonAction() {
    log.debug("Export playlist button pressed");

    exportController.bindPlaylists();
    exportView.show(true);
  }

  @FXML
  protected void handlePreviousButtonAction() {
    log.debug("Previous button pressed");

    if (mediaService.getPlayingTimeSeconds() > applicationProperties.getPreviousSecondsCutoff()) {
      mediaService.setSeekPositionPercent(0);
    } else {
      playlistService.playPreviousTrack(true);
    }
  }

  @FXML
  protected void handlePlayPauseButtonAction() {
    log.debug("Play/pause button pressed");

    if (mediaService.isPlaying()) {
      playlistService.pauseCurrentTrack();
    } else if (mediaService.isPaused()) {
      playlistService.resumeCurrentTrack();
    } else if (playlistService.getPlaylist(currentSelectedPlaylistId).filter(playlist -> !playlist.isEmpty()).isPresent() &&
        playlistService.getSelectedTrack() == null) {
      playlistService.playPlaylist(currentSelectedPlaylistId);
    } else {
      playlistService.playCurrentTrack(true);
    }
  }

  @FXML
  protected void handleNextButtonAction() {
    log.debug("Next button pressed");

    playlistService.playNextTrack(true);
  }

  @FXML
  protected void handleVolumeButtonAction() {
    log.debug("Volume button pressed");

    mediaService.setMuted();

    setVolumeButtonImage();
  }

  @FXML
  protected void handleShuffleButtonAction() {
    log.debug("Shuffle button pressed");

    playlistService.setShuffle(!playlistService.isShuffle(), false);

    setShuffleButtonImage();
  }

  @FXML
  protected void handleRepeatButtonAction() {
    log.debug("Repeat button pressed");

    playlistService.updateRepeat();

    setRepeatButtonImage();
  }

  @FXML
  protected void handleEqButtonAction() {
    log.debug("EQ button pressed");

    equalizerController.updateSliderValues();
    equalizerView.show(true);
  }

  @FXML
  protected void handleRandomButtonAction() {
    log.debug("Random button pressed");

    YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();

    playlistService.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchService.getShuffledPlaylist(
        applicationProperties.getShuffledPlaylistSize(), (nonNull(yearFilter) ? yearFilter.year() : null)));
    playlistService.playPlaylist(PLAYLIST_ID_SEARCH);
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    switch (event) {
      // Application Events
      case APPLICATION_INITIALISED -> {
        // Update year filter
        updateYearFilter();

        // Update the observable lists
        updateObservablePlaylists();

        // Select the first playlist
        if (!observablePlaylists.isEmpty()) {
          playlistPanelListView.getSelectionModel().select(0);
          playlistPanelListView.getFocusModel().focus(0);
        }

        // Set the button images
        setVolumeButtonImage();
        setShuffleButtonImage();
        setRepeatButtonImage();

        // Enable GUI components
        yearFilterComboBox.setDisable(false);
        searchTextField.setDisable(false);
        addPlaylistButton.setDisable(false);
        deletePlaylistButton.setDisable(false);
        importPlaylistButton.setDisable(false);
        exportPlaylistButton.setDisable(false);
        settingsButton.setDisable(false);
        timeSlider.setDisable(false);
        volumeButton.setDisable(false);
        volumeSlider.setDisable(false);
        shuffleButton.setDisable(false);
        repeatButton.setDisable(false);
        eqButton.setDisable(false);
        randomButton.setDisable(false);
      }
      case DATA_INDEXED -> updateYearFilter();
      case NEW_VERSION_AVAILABLE -> {
        Version newVersion = (Version) payload[0];

        newVersionButton.setText(stringResourceService.getString(MESSAGE_NEW_VERSION_AVAILABLE, newVersion));
        newVersionButton.setDisable(false);
        newVersionButton.setVisible(true);
      }
      case MUTE_UPDATED -> setVolumeButtonImage();
      case TIME_UPDATED -> {
        Duration mediaDuration = (Duration) payload[0];
        Duration currentTime = (Duration) payload[1];

        timeSlider.setDisable(mediaDuration.isUnknown());

        if (!timeSlider.isDisabled() && mediaDuration.greaterThan(Duration.ZERO) && !timeSlider.isSliderValueChanging()) {
          timeSlider.setSliderValue(currentTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
        } else if (!timeSlider.isSliderValueChanging()) {
          timeSlider.setSliderValue(0.0d);
        }

        playTimeLabel.setText(StringHelper.formatElapsedTime(mediaDuration, currentTime));
      }
      case BUFFER_UPDATED -> {
        Duration mediaDuration = (Duration) payload[0];
        Duration bufferProgressTime = (Duration) payload[1];

        if (mediaDuration != null && bufferProgressTime != null) {
          timeSlider.setProgressValue(bufferProgressTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
        }
      }
      case MEDIA_PLAYING -> {
        playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PAUSE + "')");
        playPauseButton.setDisable(false);
        previousButton.setDisable(false);
        nextButton.setDisable(false);
      }
      case MEDIA_PAUSED -> {
        playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PLAY + "')");
        playPauseButton.setDisable(false);
        previousButton.setDisable(true);
        nextButton.setDisable(true);
      }
      case MEDIA_STOPPED, END_OF_MEDIA -> {
        playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PLAY + "')");
        playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
        timeSlider.setSliderValue(0);

        // If the event is MEDIA_STOPPED or the playlist manager has
        // a repeat that isn't ONE, reset the progress value
        if (event == MEDIA_STOPPED || playlistService.getRepeat() != ONE) {
          timeSlider.setProgressValue(0);
        }

        previousButton.setDisable(true);
        nextButton.setDisable(true);
      }
      case PLAYLIST_SELECTED, PLAYLIST_CREATED, PLAYLIST_DELETED -> {
        // If we have created or deleted a playlist, update the
        // observable list
        if (event == PLAYLIST_CREATED || event == PLAYLIST_DELETED) {
          updateObservablePlaylists();
        }

        if (nonNull(payload) && payload.length > 0) {
          Integer selectedPlaylistId = (Integer) payload[0];

          // Select the correct playlist
          if (nonNull(selectedPlaylistId) && observablePlaylists.size() > selectedPlaylistId) {
            for (int i = 0; i < observablePlaylists.size(); i++) {
              Playlist playlist = observablePlaylists.get(i);

              if (playlist.getPlaylistId() == selectedPlaylistId) {
                playlistPanelListView.getSelectionModel().select(i);
                playlistPanelListView.getFocusModel().focus(i);

                // If this is a playlist creation event, go
                // straight into edit mode
                if (event == PLAYLIST_CREATED) {
                  if (payload.length > 1 && (Boolean) payload[1]) {
                    playlistPanelListView.edit(i);
                  }
                }

                break;
              }
            }

            // If we're selecting a new playlist, then clear the selected track
            if (currentSelectedPlaylistId != selectedPlaylistId) {
              playlistService.clearSelectedTrack();
            }

            currentSelectedPlaylistId = selectedPlaylistId;

            // If we're not playing or paused and the playlist is not empty
            // then enable the play button so we can play the playlist
            if (!mediaService.isPlaying() && !mediaService.isPaused()) {
              playPauseButton.setDisable(!isPlaylistPlayable());
            }
          }
        }
      }
      case TRACK_SELECTED -> playPauseButton.setDisable(false);
      case TRACK_QUEUED_FOR_PLAYING -> {
        if (nonNull(payload) && payload.length > 0) {
          Track track = (Track) payload[0];

          playingTrackLabel.setText(track.getTrackName());
          playingAlbumLabel.setText(track.getAlbumName());
          playingArtistLabel.setText(track.getArtistName());

          imageFactory.loadImage(playingImageView, cacheService.constructInternalUrl(CacheType.IMAGE,
              track.getAlbumId(), track.getAlbumImage()));

          playPauseButton.setDisable(true);

          nativeService.displayNotification(track);
        }
      }

      // Menu Events
      case MENU_FILE_IMPORT_PLAYLIST -> handleImportPlaylistButtonAction();
      case MENU_FILE_EXPORT_PLAYLIST -> handleExportPlaylistButtonAction();
      case MENU_FILE_SETTINGS -> handleSettingsButtonAction();
      case MENU_EDIT_ADD_PLAYLIST -> handleAddPlaylistButtonAction();
      case MENU_EDIT_DELETE_PLAYLIST -> handleDeletePlaylistButtonAction();
      case MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM -> ofNullable(trackTableController.getSelectedTrack()).ifPresent(playlistService::createPlaylistFromAlbum);
      case MENU_EDIT_RANDOM_PLAYLIST -> handleRandomButtonAction();
      case MENU_CONTROLS_PLAY_PAUSE -> handlePlayPauseButtonAction();
      case MENU_CONTROLS_PREVIOUS -> handlePreviousButtonAction();
      case MENU_CONTROLS_NEXT -> handleNextButtonAction();
      case MENU_CONTROLS_SHUFFLE -> handleShuffleButtonAction();
      case MENU_CONTROLS_REPEAT -> handleRepeatButtonAction();
      case MENU_CONTROLS_VOLUME_UP -> {
        if (nonNull(payload) && payload.length > 0) {
          double volume = volumeSlider.getValue() + (Double)payload[0];

          if (volume > 100) {
            volume = 100;
          }

          volumeSlider.setValue(volume);
          mediaService.setVolumePercent(volume);
        }
      }
      case MENU_CONTROLS_VOLUME_DOWN -> {
        if (nonNull(payload) && payload.length > 0) {
          double volume = volumeSlider.getValue() - (Double)payload[0];

          if (volume < 0) {
            volume = 0;
          }

          volumeSlider.setValue(volume);
          mediaService.setVolumePercent(volume);
        }
      }
      case MENU_CONTROLS_VOLUME_MUTE -> handleVolumeButtonAction();
      case MENU_VIEW_EQUALIZER -> handleEqButtonAction();
    }
  }
}
