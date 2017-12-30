package uk.co.mpcontracting.rpmjukebox.controller;

import static java.util.Optional.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.reflect.TypeToken;
import com.igormaznitsa.commons.version.Version;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.NativeManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.manager.UpdateManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.model.YearFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackFilter;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.StringHelper;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.view.ConfirmView;
import uk.co.mpcontracting.rpmjukebox.view.EqualizerView;
import uk.co.mpcontracting.rpmjukebox.view.ExportView;
import uk.co.mpcontracting.rpmjukebox.view.MessageView;
import uk.co.mpcontracting.rpmjukebox.view.SettingsView;
import uk.co.mpcontracting.rpmjukebox.view.TrackTableView;

@Slf4j
@FXMLController
public class MainPanelController extends EventAwareObject implements Constants {

    @FXML
    private Button newVersionButton;

    @FXML
    private ComboBox<YearFilter> yearFilterComboBox;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<Playlist> playlistPanelListView;

    @FXML
    private BorderPane mainPanel;

    @FXML
    private Button addPlaylistButton;

    @FXML
    private Button deletePlaylistButton;

    @FXML
    private Button importPlaylistButton;

    @FXML
    private Button exportPlaylistButton;

    @FXML
    private Button settingsButton;

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

    @Autowired
    private EqualizerView equalizerView;

    @Autowired
    private SettingsView settingsView;

    @Autowired
    private ExportView exportView;

    @Autowired
    private MessageView messageView;

    @Autowired
    private ConfirmView confirmView;

    @Autowired
    private TrackTableView trackTableView;

    @Autowired
    private EqualizerController equalizerController;

    @Autowired
    private SettingsController settingsController;

    @Autowired
    private ExportController exportController;

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private PlaylistManager playlistManager;

    @Autowired
    private MediaManager mediaManager;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private NativeManager nativeManager;

    @Autowired
    private UpdateManager updateManager;

    @Value("${previous.seconds.cutoff}")
    private int previousSecondsCutoff;

    @Value("${max.playlist.size}")
    private int maxPlaylistSize;

    @Value("${shuffled.playlist.size}")
    private int shuffledPlaylistSize;

    @Value("${playlist.file.extension}")
    private String playlistFileExtension;

    private ObservableList<Playlist> observablePlaylists;

    private String playlistExtensionFilter;
    private int currentSelectedPlaylistId;

    @FXML
    public void initialize() {
        log.info("Initialising MainPanelController");

        yearFilterComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            searchParametersUpdated(searchTextField.getText(), yearFilterComboBox.getSelectionModel().getSelectedItem(),
                false);
        });

        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchParametersUpdated(newValue, yearFilterComboBox.getSelectionModel().getSelectedItem(), true);
        });

        timeSlider.sliderValueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            if (!isChanging) {
                mediaManager.setSeekPositionPercent(timeSlider.getSliderValue());
            }
        });

        timeSlider.sliderValueProperty().addListener((observable, oldValue, newValue) -> {
            if (!timeSlider.isSliderValueChanging()) {
                double sliderPercent = newValue.doubleValue();
                double trackPercent = mediaManager.getPlayingTimePercent();

                if (Math.abs(trackPercent - sliderPercent) > 0.5) {
                    mediaManager.setSeekPositionPercent(timeSlider.getSliderValue());
                }
            }
        });

        volumeSlider.valueProperty().addListener(observable -> {
            if (volumeSlider.isValueChanging()) {
                mediaManager.setVolumePercent(volumeSlider.getValue());
            }
        });

        playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
        volumeSlider.setValue(mediaManager.getVolume() * 100);

        // Playlist list view
        observablePlaylists = FXCollections.observableArrayList();
        playlistPanelListView.setCellFactory(new PlaylistListCellFactory());
        playlistPanelListView.setEditable(true);
        playlistPanelListView.setItems(observablePlaylists);
        playlistPanelListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();

                showConfirmView(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()),
                    true, () -> {
                        playlistManager.deletePlaylist(playlist.getPlaylistId());
                    }, null);
            }
        });

        // Track table view
        mainPanel.setCenter(trackTableView.getView());

        playlistExtensionFilter = "*." + playlistFileExtension;
        currentSelectedPlaylistId = -999;
    }

    public void showMessageView(String message, boolean blurBackground) {
        ThreadRunner.runOnGui(() -> {
            messageView.setMessage(message);

            if (!messageView.isShowing()) {
                messageView.show(blurBackground);
            }
        });
    }

    public void closeMessageView() {
        ThreadRunner.runOnGui(() -> {
            messageView.close();
        });
    }

    public void showConfirmView(String message, boolean blurBackground, Runnable okRunnable, Runnable cancelRunnable) {
        ThreadRunner.runOnGui(() -> {
            confirmView.setMessage(message);
            confirmView.setRunnables(okRunnable, cancelRunnable);

            if (!confirmView.isShowing()) {
                confirmView.show(blurBackground);
            }
        });
    }

    private void searchParametersUpdated(String searchText, YearFilter yearFilter, boolean searchTextUpdated) {
        log.debug("Search parameters updated - '{}' - {}", searchText, yearFilter);

        if (searchText != null && searchText.trim().length() > 0) {
            TrackFilter trackFilter = null;
            TrackSearch trackSearch = null;

            if (yearFilter != null && yearFilter.getYear() != null && yearFilter.getYear().trim().length() > 0) {
                trackFilter = new TrackFilter(null, yearFilter.getYear());
            }

            if (trackFilter != null) {
                trackSearch = new TrackSearch(searchText.trim(), trackFilter);
            } else {
                trackSearch = new TrackSearch(searchText.trim());
            }

            playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchManager.search(trackSearch));
        } else if (playlistManager.getPlayingPlaylist() != null
            && playlistManager.getPlayingPlaylist().getPlaylistId() == PLAYLIST_ID_SEARCH) {
            playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, playlistManager.getPlayingPlaylist().getTracks());
        } else {
            playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, Collections.emptyList());
        }

        if (searchTextUpdated) {
            fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
        }
    }

    // Package level for testing purposes
    void updateYearFilter() {
        log.debug("Updating year filter - {}", searchManager.getYearList());

        List<YearFilter> yearFilters = new ArrayList<>();
        yearFilters.add(new YearFilter("None", null));

        ofNullable(searchManager.getYearList())
            .ifPresent(years -> years.forEach(year -> yearFilters.add(new YearFilter(year, year))));

        ofNullable(yearFilterComboBox).ifPresent(comboBox -> {
            comboBox.getItems().clear();
            comboBox.getItems().addAll(yearFilters);
            comboBox.getSelectionModel().selectFirst();
        });
    }

    // Package level for testing purposes
    void updateObservablePlaylists() {
        log.debug("Updating observable playlists");

        observablePlaylists.setAll(playlistManager.getPlaylists());
    }

    // Package level for testing purposes
    void setVolumeButtonImage() {
        if (mediaManager.isMuted()) {
            volumeButton.setStyle("-fx-background-image: url('" + IMAGE_VOLUME_OFF + "')");
        } else {
            volumeButton.setStyle("-fx-background-image: url('" + IMAGE_VOLUME_ON + "')");
        }
    }

    private void setShuffleButtonImage() {
        if (playlistManager.isShuffle()) {
            shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
        } else {
            shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
        }
    }

    private void setRepeatButtonImage() {
        switch (playlistManager.getRepeat()) {
            case OFF: {
                repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");
                break;
            }
            case ALL: {
                repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_ALL + "')");
                break;
            }
            case ONE: {
                repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_ONE + "')");
                break;
            }
        }
    }

    @FXML
    protected void handleNewVersionButtonAction(ActionEvent event) {
        log.debug("New version button pressed");

        updateManager.downloadNewVersion();
    }

    @FXML
    protected void handleAddPlaylistButtonAction(ActionEvent event) {
        log.debug("Add playlist button pressed");

        playlistManager.createPlaylist();
    }

    @FXML
    protected void handleDeletePlaylistButtonAction(ActionEvent event) {
        log.debug("Delete playlist button pressed");

        Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();

        if (playlist != null && playlist.getPlaylistId() > 0) {
            showConfirmView(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()), true,
                () -> {
                    playlistManager.deletePlaylist(playlist.getPlaylistId());
                }, null);
        }
    }

    @FXML
    protected void handleImportPlaylistButtonAction(ActionEvent event) {
        log.debug("Import playlist button pressed");

        FileChooser fileChooser = constructFileChooser();
        fileChooser.setTitle(messageManager.getMessage(MESSAGE_EXPORT_PLAYLIST_TITLE));
        fileChooser.getExtensionFilters()
            .add(new ExtensionFilter(
                messageManager.getMessage(MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER, playlistExtensionFilter),
                playlistExtensionFilter));

        RpmJukebox.getStage().getScene().getRoot().setEffect(new BoxBlur());

        int currentPlaylistSelection = playlistPanelListView.getSelectionModel().getSelectedIndex();

        File file = fileChooser.showOpenDialog(RpmJukebox.getStage());

        if (file != null) {
            List<PlaylistSettings> playlists = null;

            try (FileReader fileReader = constructFileReader(file)) {
                playlists = settingsManager.getGson().fromJson(fileReader,
                    new TypeToken<ArrayList<PlaylistSettings>>() {
                    }.getType());

                if (playlists != null) {
                    playlists.forEach(playlistSettings -> {
                        Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(),
                            maxPlaylistSize);

                        playlistSettings.getTracks().forEach(trackId -> {
                            Track track = searchManager.getTrackById(trackId);

                            if (track != null) {
                                playlist.addTrack(track);
                            }
                        });

                        playlistManager.addPlaylist(playlist);
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

    // Package level for testing purposes
    FileChooser constructFileChooser() {
        return new FileChooser();
    }

    // Package level for testing purposes
    FileReader constructFileReader(File file) throws Exception {
        return new FileReader(file);
    }

    @FXML
    protected void handleExportPlaylistButtonAction(ActionEvent event) {
        log.debug("Export playlist button pressed");

        exportController.bindPlaylists();
        exportView.show(true);
    }

    @FXML
    protected void handleSettingsButtonAction(ActionEvent event) {
        log.debug("Settings button pressed");

        settingsController.bindSystemSettings();
        settingsView.show(true);
    }

    @FXML
    protected void handlePreviousButtonAction(ActionEvent event) {
        log.debug("Previous button pressed");

        if (mediaManager.getPlayingTimeSeconds() > previousSecondsCutoff) {
            mediaManager.setSeekPositionPercent(0);
        } else {
            playlistManager.playPreviousTrack(true);
        }
    }

    @FXML
    protected void handlePlayPauseButtonAction(ActionEvent event) {
        log.debug("Play/pause button pressed");

        if (mediaManager.isPlaying()) {
            playlistManager.pauseCurrentTrack();
        } else if (mediaManager.isPaused()) {
            playlistManager.resumeCurrentTrack();
        } else if (!playlistManager.getPlaylist(currentSelectedPlaylistId).isEmpty()
            && playlistManager.getSelectedTrack() == null) {
            playlistManager.playPlaylist(currentSelectedPlaylistId);
        } else {
            playlistManager.playCurrentTrack(true);
        }
    }

    @FXML
    protected void handleNextButtonAction(ActionEvent event) {
        log.debug("Next button pressed");

        playlistManager.playNextTrack(true);
    }

    @FXML
    protected void handleVolumeButtonAction(ActionEvent event) {
        log.debug("Volume button pressed");

        mediaManager.setMuted();

        setVolumeButtonImage();
    }

    @FXML
    protected void handleShuffleButtonAction(ActionEvent event) {
        log.debug("Shuffle button pressed");

        playlistManager.setShuffle(!playlistManager.isShuffle(), false);

        setShuffleButtonImage();
    }

    @FXML
    protected void handleRepeatButtonAction(ActionEvent event) {
        log.debug("Repeat button pressed");

        playlistManager.updateRepeat();

        setRepeatButtonImage();
    }

    @FXML
    protected void handleEqButtonAction(ActionEvent event) {
        log.debug("EQ button pressed");

        equalizerController.updateSliderValues();
        equalizerView.show(true);
    }

    @FXML
    protected void handleRandomButtonAction(ActionEvent event) {
        log.debug("Random button pressed");

        YearFilter yearFilter = yearFilterComboBox.getSelectionModel().getSelectedItem();

        playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchManager.getShuffledPlaylist(shuffledPlaylistSize,
            (yearFilter != null ? yearFilter.getYear() : null)));
        playlistManager.playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            case APPLICATION_INITIALISED: {
                // Update year filter
                updateYearFilter();

                // Update the observable lists
                updateObservablePlaylists();

                // Select the first playlist
                if (observablePlaylists.size() > 0) {
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

                break;
            }
            case DATA_INDEXED: {
                updateYearFilter();

                break;
            }
            case NEW_VERSION_AVAILABLE: {
                Version newVersion = (Version)payload[0];

                newVersionButton.setText(messageManager.getMessage(MESSAGE_NEW_VERSION_AVAILABLE, newVersion));
                newVersionButton.setDisable(false);
                newVersionButton.setVisible(true);

                break;
            }
            case MUTE_UPDATED: {
                setVolumeButtonImage();

                break;
            }
            case TIME_UPDATED: {
                Duration mediaDuration = (Duration)payload[0];
                Duration currentTime = (Duration)payload[1];

                timeSlider.setDisable(mediaDuration.isUnknown());

                if (!timeSlider.isDisabled() && mediaDuration.greaterThan(Duration.ZERO)
                    && !timeSlider.isSliderValueChanging()) {
                    timeSlider.setSliderValue(currentTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
                } else if (!timeSlider.isSliderValueChanging()) {
                    timeSlider.setSliderValue(0.0d);
                }

                playTimeLabel.setText(StringHelper.formatElapsedTime(mediaDuration, currentTime));

                break;
            }
            case BUFFER_UPDATED: {
                Duration mediaDuration = (Duration)payload[0];
                Duration bufferProgressTime = (Duration)payload[1];

                if (mediaDuration != null && bufferProgressTime != null) {
                    timeSlider.setProgressValue(bufferProgressTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
                }

                break;
            }
            case MEDIA_PLAYING: {
                playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PAUSE + "')");
                playPauseButton.setDisable(false);
                previousButton.setDisable(false);
                nextButton.setDisable(false);

                break;
            }
            case MEDIA_PAUSED: {
                playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PLAY + "')");
                playPauseButton.setDisable(false);
                previousButton.setDisable(true);
                nextButton.setDisable(true);

                break;
            }
            case MEDIA_STOPPED:
            case END_OF_MEDIA: {
                playPauseButton.setStyle("-fx-background-image: url('" + IMAGE_PLAY + "')");
                playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
                timeSlider.setSliderValue(0);

                // If the event is MEDIA_STOPPED or the playlist manager has
                // a repeat that isn't ONE, reset the progress value
                if (event == Event.MEDIA_STOPPED || playlistManager.getRepeat() != Repeat.ONE) {
                    timeSlider.setProgressValue(0);
                }

                previousButton.setDisable(true);
                nextButton.setDisable(true);

                break;
            }
            case PLAYLIST_SELECTED:
            case PLAYLIST_CREATED:
            case PLAYLIST_DELETED: {
                // If we have created or deleted a playlist, update the
                // observable list
                if (event == Event.PLAYLIST_CREATED || event == Event.PLAYLIST_DELETED) {
                    updateObservablePlaylists();
                }

                if (payload != null && payload.length > 0) {
                    Integer selectedPlaylistId = (Integer)payload[0];

                    // Select the correct playlist
                    if (selectedPlaylistId != null && observablePlaylists.size() > selectedPlaylistId) {
                        for (int i = 0; i < observablePlaylists.size(); i++) {
                            Playlist playlist = observablePlaylists.get(i);

                            if (playlist.getPlaylistId() == selectedPlaylistId) {
                                playlistPanelListView.getSelectionModel().select(i);
                                playlistPanelListView.getFocusModel().focus(i);

                                // If this is a playlist creation event, go
                                // straight into edit mode
                                if (event == Event.PLAYLIST_CREATED) {
                                    if (payload.length > 1 && (Boolean)payload[1]) {
                                        playlistPanelListView.edit(i);
                                    }
                                }

                                break;
                            }
                        }

                        // If we're selecting a new playlist, then clear the
                        // selected track
                        if (currentSelectedPlaylistId != selectedPlaylistId) {
                            playlistManager.clearSelectedTrack();
                        }

                        currentSelectedPlaylistId = selectedPlaylistId;

                        // If we're not playing or paused and the playlist is
                        // not empty
                        // then enable the play button so we can play the
                        // playlist
                        if (!mediaManager.isPlaying() && !mediaManager.isPaused()) {
                            if (!playlistManager.getPlaylist(currentSelectedPlaylistId).isEmpty()) {
                                playPauseButton.setDisable(false);
                            } else {
                                playPauseButton.setDisable(true);
                            }
                        }
                    }
                }

                break;
            }
            case TRACK_SELECTED: {
                playPauseButton.setDisable(false);

                break;
            }
            case TRACK_QUEUED_FOR_PLAYING: {
                if (payload != null && payload.length > 0) {
                    Track track = (Track)payload[0];

                    playingTrackLabel.setText(track.getTrackName());
                    playingAlbumLabel.setText(track.getAlbumName());
                    playingArtistLabel.setText(track.getArtistName());

                    if (track.getAlbumImage() != null && track.getAlbumImage().trim().length() > 0) {
                        playingImageView.setImage(new Image(cacheManager.constructInternalUrl(CacheType.IMAGE,
                            track.getAlbumId(), track.getAlbumImage()), true));
                    } else if (track.getArtistImage() != null && track.getArtistImage().trim().length() > 0) {
                        playingImageView.setImage(new Image(cacheManager.constructInternalUrl(CacheType.IMAGE,
                            track.getAlbumId(), track.getArtistImage()), true));
                    } else {
                        playingImageView.setImage(new Image(IMAGE_NO_ARTWORK));
                    }

                    playPauseButton.setDisable(true);

                    nativeManager.displayNotification(track);
                }

                break;
            }
            default: {
                // Nothing
            }
        }
    }
}
