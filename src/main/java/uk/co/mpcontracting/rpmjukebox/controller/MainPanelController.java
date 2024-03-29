package uk.co.mpcontracting.rpmjukebox.controller;

import com.google.gson.reflect.TypeToken;
import com.igormaznitsa.commons.version.Version;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.ImageFactory;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.javafx.FXMLController;
import uk.co.mpcontracting.rpmjukebox.manager.*;
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
import uk.co.mpcontracting.rpmjukebox.view.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;

@Slf4j
@FXMLController
@RequiredArgsConstructor
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

    private final AppProperties appProperties;
    private final ThreadRunner threadRunner;
    private final MessageManager messageManager;
    private final ImageFactory imageFactory;

    private EqualizerView equalizerView;
    private SettingsView settingsView;
    private ExportView exportView;
    private MessageView messageView;
    private ConfirmView confirmView;
    private TrackTableView trackTableView;
    private TrackTableController trackTableController;
    private EqualizerController equalizerController;
    private SettingsController settingsController;
    private ExportController exportController;
    private SettingsManager settingsManager;
    private SearchManager searchManager;
    private PlaylistManager playlistManager;
    private MediaManager mediaManager;
    private CacheManager cacheManager;
    private NativeManager nativeManager;
    private UpdateManager updateManager;

    private ObservableList<Playlist> observablePlaylists;

    private String playlistExtensionFilter;
    private int currentSelectedPlaylistId;

    @Autowired
    private void wireEqualizerView(EqualizerView equalizerView) {
        this.equalizerView = equalizerView;
    }

    @Autowired
    private void wireSettingsView(SettingsView settingsView) {
        this.settingsView = settingsView;
    }

    @Autowired
    private void wireExportView(ExportView exportView) {
        this.exportView = exportView;
    }

    @Autowired
    private void wireMessageView(MessageView messageView) {
        this.messageView = messageView;
    }

    @Autowired
    private void wireConfirmView(ConfirmView confirmView) {
        this.confirmView = confirmView;
    }

    @Autowired
    private void wireTrackTableView(TrackTableView trackTableView) {
        this.trackTableView = trackTableView;
    }

    @Autowired
    private void wireTrackTableController(TrackTableController trackTableController) {
        this.trackTableController = trackTableController;
    }

    @Autowired
    private void wireEqualizerController(EqualizerController equalizerController) {
        this.equalizerController = equalizerController;
    }

    @Autowired
    private void wireSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }

    @Autowired
    private void wireExportController(ExportController exportController) {
        this.exportController = exportController;
    }

    @Autowired
    private void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    private void wireSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    private void wirePlaylistManager(PlaylistManager playlistManager) {
        this.playlistManager = playlistManager;
    }

    @Autowired
    private void wireMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @Autowired
    private void wireCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    private void wireNativeManager(NativeManager nativeManager) {
        this.nativeManager = nativeManager;
    }

    @Autowired
    private void wireUpdateManager(UpdateManager updateManager) {
        this.updateManager = updateManager;
    }

    @FXML
    public void initialize() {
        log.info("Initialising MainPanelController");

        yearFilterComboBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                searchParametersUpdated(searchTextField.getText(), yearFilterComboBox.getSelectionModel().getSelectedItem(), false));

        searchTextField.textProperty().addListener((observable, oldValue, newValue) ->
                searchParametersUpdated(newValue, yearFilterComboBox.getSelectionModel().getSelectedItem(), true));

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
                        true, () -> playlistManager.deletePlaylist(playlist.getPlaylistId()), null);
            }
        });
        playlistPanelListView.getSelectionModel().selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> ofNullable(newValue)
                        .ifPresent(playlist -> fireEvent(PLAYLIST_SELECTED, playlist.getPlaylistId()))));

        // Track table view
        mainPanel.setCenter(trackTableView.getView());

        playlistExtensionFilter = "*." + appProperties.getPlaylistFileExtension();
        currentSelectedPlaylistId = -999;
    }

    void showMessageView(String message, boolean blurBackground) {
        threadRunner.runOnGui(() -> {
            messageView.setMessage(message);

            if (!messageView.isShowing()) {
                messageView.show(blurBackground);
            }
        });
    }

    void closeMessageView() {
        threadRunner.runOnGui(() -> messageView.close());
    }

    public void showConfirmView(String message, boolean blurBackground, Runnable okRunnable, Runnable cancelRunnable) {
        threadRunner.runOnGui(() -> {
            confirmView.setMessage(message);
            confirmView.setRunnables(okRunnable, cancelRunnable);

            if (!confirmView.isShowing()) {
                confirmView.show(blurBackground);
            }
        });
    }

    public boolean isPlaylistPlayable() {
         return playlistManager.getPlaylist(currentSelectedPlaylistId)
                    .filter(playlist -> !playlist.isEmpty()).isPresent();
    }

    private void searchParametersUpdated(String searchText, YearFilter yearFilter, boolean searchTextUpdated) {
        log.debug("Search parameters updated - '{}' - {}", searchText, yearFilter);

        if (searchText != null && searchText.trim().length() > 0) {
            TrackFilter trackFilter = null;
            TrackSearch trackSearch;

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
            fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
        }
    }

    // Package level for testing purposes
    void updateYearFilter() {
        log.debug("Updating year filter - {}", searchManager.getYearList());

        List<YearFilter> yearFilters = new ArrayList<>();
        yearFilters.add(new YearFilter(messageManager.getMessage(MESSAGE_YEAR_FILTER_NONE), null));

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

    // Package level for testing purposes
    void setShuffleButtonImage() {
        if (playlistManager.isShuffle()) {
            shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
        } else {
            shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
        }
    }

    // Package level for testing purposes
    void setRepeatButtonImage() {
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
    protected void handleNewVersionButtonAction() {
        log.debug("New version button pressed");

        updateManager.downloadNewVersion();
    }

    @FXML
    protected void handleAddPlaylistButtonAction() {
        log.debug("Add playlist button pressed");

        playlistManager.createPlaylist();
    }

    @FXML
    protected void handleDeletePlaylistButtonAction() {
        log.debug("Delete playlist button pressed");

        Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();

        if (playlist != null && playlist.getPlaylistId() > 0) {
            showConfirmView(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()), true,
                    () -> playlistManager.deletePlaylist(playlist.getPlaylistId()), null);
        }
    }

    @FXML
    protected void handleImportPlaylistButtonAction() {
        log.debug("Import playlist button pressed");

        FileChooser fileChooser = constructFileChooser();
        fileChooser.setTitle(messageManager.getMessage(MESSAGE_IMPORT_PLAYLIST_TITLE));
        fileChooser.getExtensionFilters()
                .add(new ExtensionFilter(
                        messageManager.getMessage(MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER, playlistExtensionFilter),
                        playlistExtensionFilter));

        RpmJukebox.getStage().getScene().getRoot().setEffect(new BoxBlur());

        int currentPlaylistSelection = playlistPanelListView.getSelectionModel().getSelectedIndex();

        File file = fileChooser.showOpenDialog(RpmJukebox.getStage());

        if (file != null) {
            try (FileReader fileReader = constructFileReader(file)) {
                List<PlaylistSettings> playlists = settingsManager.getGson().fromJson(fileReader,
                        new TypeToken<ArrayList<PlaylistSettings>>() {
                        }.getType());

                if (playlists != null) {
                    playlists.forEach(playlistSettings -> {
                        Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(),
                                appProperties.getMaxPlaylistSize());

                        playlistSettings.getTracks().forEach(trackId ->
                                searchManager.getTrackById(trackId).ifPresent(playlist::addTrack));

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
    protected void handleExportPlaylistButtonAction() {
        log.debug("Export playlist button pressed");

        exportController.bindPlaylists();
        exportView.show(true);
    }

    @FXML
    protected void handleSettingsButtonAction() {
        log.debug("Settings button pressed");

        settingsController.bindSystemSettings();
        settingsView.show(true);
    }

    @FXML
    protected void handlePreviousButtonAction() {
        log.debug("Previous button pressed");

        if (mediaManager.getPlayingTimeSeconds() > appProperties.getPreviousSecondsCutoff()) {
            mediaManager.setSeekPositionPercent(0);
        } else {
            playlistManager.playPreviousTrack(true);
        }
    }

    @FXML
    protected void handlePlayPauseButtonAction() {
        log.debug("Play/pause button pressed");

        if (mediaManager.isPlaying()) {
            playlistManager.pauseCurrentTrack();
        } else if (mediaManager.isPaused()) {
            playlistManager.resumeCurrentTrack();
        } else if (playlistManager.getPlaylist(currentSelectedPlaylistId).filter(playlist -> !playlist.isEmpty()).isPresent() &&
                playlistManager.getSelectedTrack() == null) {
            playlistManager.playPlaylist(currentSelectedPlaylistId);
        } else {
            playlistManager.playCurrentTrack(true);
        }
    }

    @FXML
    protected void handleNextButtonAction() {
        log.debug("Next button pressed");

        playlistManager.playNextTrack(true);
    }

    @FXML
    protected void handleVolumeButtonAction() {
        log.debug("Volume button pressed");

        mediaManager.setMuted();

        setVolumeButtonImage();
    }

    @FXML
    protected void handleShuffleButtonAction() {
        log.debug("Shuffle button pressed");

        playlistManager.setShuffle(!playlistManager.isShuffle(), false);

        setShuffleButtonImage();
    }

    @FXML
    protected void handleRepeatButtonAction() {
        log.debug("Repeat button pressed");

        playlistManager.updateRepeat();

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

        playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchManager.getShuffledPlaylist(
                appProperties.getShuffledPlaylistSize(), (yearFilter != null ? yearFilter.getYear() : null)));
        playlistManager.playPlaylist(PLAYLIST_ID_SEARCH);
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        switch (event) {
            // Application Events
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
                Version newVersion = (Version) payload[0];

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
                Duration mediaDuration = (Duration) payload[0];
                Duration currentTime = (Duration) payload[1];

                timeSlider.setDisable(mediaDuration.isUnknown());

                if (!timeSlider.isDisabled() && mediaDuration.greaterThan(Duration.ZERO) && !timeSlider.isSliderValueChanging()) {
                    timeSlider.setSliderValue(currentTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
                } else if (!timeSlider.isSliderValueChanging()) {
                    timeSlider.setSliderValue(0.0d);
                }

                playTimeLabel.setText(StringHelper.formatElapsedTime(mediaDuration, currentTime));

                break;
            }
            case BUFFER_UPDATED: {
                Duration mediaDuration = (Duration) payload[0];
                Duration bufferProgressTime = (Duration) payload[1];

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
                    Integer selectedPlaylistId = (Integer) payload[0];

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
                                    if (payload.length > 1 && (Boolean) payload[1]) {
                                        playlistPanelListView.edit(i);
                                    }
                                }

                                break;
                            }
                        }

                        // If we're selecting a new playlist, then clear the selected track
                        if (currentSelectedPlaylistId != selectedPlaylistId) {
                            playlistManager.clearSelectedTrack();
                        }

                        currentSelectedPlaylistId = selectedPlaylistId;

                        // If we're not playing or paused and the playlist is not empty
                        // then enable the play button so we can play the playlist
                        if (!mediaManager.isPlaying() && !mediaManager.isPaused()) {
                            playPauseButton.setDisable(!isPlaylistPlayable());
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
                    Track track = (Track) payload[0];

                    playingTrackLabel.setText(track.getTrackName());
                    playingAlbumLabel.setText(track.getAlbumName());
                    playingArtistLabel.setText(track.getArtistName());

                    imageFactory.loadImage(playingImageView, cacheManager.constructInternalUrl(CacheType.IMAGE,
                            track.getAlbumId(), track.getAlbumImage()));

                    playPauseButton.setDisable(true);

                    nativeManager.displayNotification(track);
                }

                break;
            }

            // Menu Events
            case MENU_FILE_IMPORT_PLAYLIST: {
                handleImportPlaylistButtonAction();
                break;
            }
            case MENU_FILE_EXPORT_PLAYLIST: {
                handleExportPlaylistButtonAction();
                break;
            }
            case MENU_FILE_SETTINGS: {
                handleSettingsButtonAction();
                break;
            }
            case MENU_EDIT_ADD_PLAYLIST: {
                handleAddPlaylistButtonAction();
                break;
            }
            case MENU_EDIT_DELETE_PLAYLIST: {
                handleDeletePlaylistButtonAction();
                break;
            }
            case MENU_EDIT_CREATE_PLAYLIST_FROM_ALBUM: {
                ofNullable(trackTableController.getSelectedTrack()).ifPresent(playlistManager::createPlaylistFromAlbum);
                break;
            }
            case MENU_EDIT_RANDOM_PLAYLIST: {
                handleRandomButtonAction();
                break;
            }
            case MENU_CONTROLS_PLAY_PAUSE: {
                handlePlayPauseButtonAction();
                break;
            }
            case MENU_CONTROLS_PREVIOUS: {
                handlePreviousButtonAction();
                break;
            }
            case MENU_CONTROLS_NEXT: {
                handleNextButtonAction();
                break;
            }
            case MENU_CONTROLS_SHUFFLE: {
                setShuffleButtonImage();
                break;
            }
            case MENU_CONTROLS_REPEAT: {
                setRepeatButtonImage();
                break;
            }
            case MENU_CONTROLS_VOLUME_UP: {
                if (payload != null && payload.length > 0) {
                    double volume = volumeSlider.getValue() + (Double)payload[0];

                    if (volume > 100) {
                        volume = 100;
                    }

                    volumeSlider.setValue(volume);
                    mediaManager.setVolumePercent(volume);
                }

                break;
            }
            case MENU_CONTROLS_VOLUME_DOWN: {
                if (payload != null && payload.length > 0) {
                    double volume = volumeSlider.getValue() - (Double)payload[0];

                    if (volume < 0) {
                        volume = 0;
                    }

                    volumeSlider.setValue(volume);
                    mediaManager.setVolumePercent(volume);
                }

                break;
            }
            case MENU_CONTROLS_VOLUME_MUTE: {
                handleVolumeButtonAction();
                break;
            }
            case MENU_VIEW_EQUALIZER: {
                handleEqButtonAction();
                break;
            }

            default: {
                // Nothing
            }
        }
    }
}
