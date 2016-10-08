package uk.co.mpcontracting.rpmjukebox.controller;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.igormaznitsa.commons.version.Version;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.ConfirmWindow;
import uk.co.mpcontracting.rpmjukebox.component.MessageWindow;
import uk.co.mpcontracting.rpmjukebox.component.ModalWindow;
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
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;
import uk.co.mpcontracting.rpmjukebox.support.StringHelper;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class MainPanelController extends EventAwareObject implements Constants {

	@FXML
	private Button newVersionButton;
	
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
	private RpmJukebox rpmJukebox;
	
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
	
	@Autowired
	private EqualizerController equalizerController;
	
	@Autowired
	private SettingsController settingsController;
	
	@Autowired
	private ExportController exportController;

	@Getter private ModalWindow equalizerWindow;
	@Getter private ModalWindow settingsWindow;
	@Getter private ModalWindow exportWindow;
	private MessageWindow messageWindow;
	private ConfirmWindow confirmWindow;
	private ObservableList<Playlist> observablePlaylists;
	
	private int previousSecondsCutoff;
	private int randomPlaylistSize;
	private String playlistExtensionFilter;
	private int currentSelectedPlaylistId;

	@FXML
	public void initialize() {
		log.info("Initialising MainPanelController");
		
		searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			searchTextUpdated(newValue);
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
		
		// Equalizer window
		equalizerWindow = new ModalWindow(rpmJukebox.getStage(), "equalizer.fxml");
		
		// Settings window
		settingsWindow = new ModalWindow(rpmJukebox.getStage(), "settings.fxml");
		
		// Export window
		exportWindow = new ModalWindow(rpmJukebox.getStage(), "export.fxml");
		
		// Message window
		messageWindow = new MessageWindow(rpmJukebox.getStage(), "message.fxml");
		
		// Confirm window
		confirmWindow = new ConfirmWindow(rpmJukebox.getStage(), "confirm.fxml");

		// Playlist list view
		observablePlaylists = FXCollections.observableArrayList();
		playlistPanelListView.setCellFactory(new PlaylistListCellFactory());
		playlistPanelListView.setEditable(true);
		playlistPanelListView.setItems(observablePlaylists);
		playlistPanelListView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
				Playlist playlist = playlistPanelListView.getSelectionModel().getSelectedItem();
				
				showConfirmWindow(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()), 
					true,
					() -> {
						playlistManager.deletePlaylist(playlist.getPlaylistId());
					},
					null
				);
			}
		});
		
		// Track table view
		mainPanel.setCenter((Node)FxmlContext.loadFxml("tracktable.fxml"));
		
		previousSecondsCutoff = settingsManager.getPropertyInteger(PROP_PREVIOUS_SECONDS_CUTOFF);
		randomPlaylistSize = settingsManager.getPropertyInteger(PROP_RANDOM_PLAYLIST_SIZE);
		playlistExtensionFilter = "*." + settingsManager.getPropertyString(PROP_PLAYLIST_FILE_EXTENSION);
		currentSelectedPlaylistId = -999;
	}

	public void showMessageWindow(String message, boolean blurBackground) {
		ThreadRunner.runOnGui(() -> {
			messageWindow.setMessage(message);
			
			if (!messageWindow.isShowing()) {
				messageWindow.display(blurBackground);
			}
		});
	}
	
	public void closeMessageWindow() {
		ThreadRunner.runOnGui(() -> {
			messageWindow.close();
		});
	}
	
	public void showConfirmWindow(String message, boolean blurBackground, Runnable okRunnable, Runnable cancelRunnable) {
		ThreadRunner.runOnGui(() -> {
			confirmWindow.setMessage(message);
			confirmWindow.setRunnables(okRunnable, cancelRunnable);
			
			if (!confirmWindow.isShowing()) {
				confirmWindow.display(blurBackground);
			}
		});
	}
	
	public void closeConfirmWindow() {
		confirmWindow.close();
	}

	private void searchTextUpdated(String searchText) {
		log.debug("Search text updated - '" + searchText + "'");

		if (searchText != null && searchText.trim().length() > 0) {
			playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchManager.search(new TrackSearch(searchText.trim())));
		} else if (playlistManager.getPlayingPlaylist() != null && playlistManager.getPlayingPlaylist().getPlaylistId() == PLAYLIST_ID_SEARCH) {
			playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, playlistManager.getPlayingPlaylist().getTracks());
		} else {
			playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, Collections.emptyList());
		}
		
		fireEvent(Event.PLAYLIST_SELECTED, PLAYLIST_ID_SEARCH);
	}
	
	private void updateObservablePlaylists() {
		log.debug("Updating observable playlists");

		observablePlaylists.setAll(playlistManager.getPlaylists());
	}
	
	private void setVolumeButtonImage() {
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
			showConfirmWindow(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()), 
				true,
				() -> {
					playlistManager.deletePlaylist(playlist.getPlaylistId());
				},
				null
			);
		}
	}
	
	@FXML
	protected void handleImportPlaylistButtonAction(ActionEvent event) {
		log.debug("Import playlist button pressed");
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(messageManager.getMessage(MESSAGE_EXPORT_PLAYLIST_TITLE));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(messageManager.getMessage(MESSAGE_FILE_CHOOSER_PLAYLIST_FILTER, playlistExtensionFilter), 
			playlistExtensionFilter));
		
		rpmJukebox.getStage().getScene().getRoot().setEffect(new BoxBlur());
		
		int currentPlaylistSelection = playlistPanelListView.getSelectionModel().getSelectedIndex();
		
		File file = fileChooser.showOpenDialog(rpmJukebox.getStage());

		if (file != null) {
			List<PlaylistSettings> playlists = null;
			
			try (FileReader fileReader = new FileReader(file)) {
				playlists = settingsManager.getGson().fromJson(fileReader, new TypeToken<ArrayList<PlaylistSettings>>(){}.getType());
				
				if (playlists != null) {
					for (PlaylistSettings playlistSettings : playlists) {
						Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(), 
							settingsManager.getPropertyInteger(PROP_MAX_PLAYLIST_SIZE));

						for (String trackId : playlistSettings.getTracks()) {
							Track track = searchManager.getTrackById(trackId);
							
							if (track != null) {
								playlist.addTrack(track);
							}
						}
						
						playlistManager.addPlaylist(playlist);
					}
					
					
					// Update the observable lists
					updateObservablePlaylists();
					
					// Select the last selected playlist
					playlistPanelListView.getSelectionModel().select(currentPlaylistSelection);
					playlistPanelListView.getFocusModel().focus(currentPlaylistSelection);
				}
			} catch (Exception e) {
				log.error("Unable to import playlists file - " + file.getAbsolutePath(), e);
				
				return;
			}
		}
		
		rpmJukebox.getStage().getScene().getRoot().setEffect(null);
	}
	
	@FXML
	protected void handleExportPlaylistButtonAction(ActionEvent event) {
		log.debug("Export playlist button pressed");
		
		exportController.bindPlaylists();
		exportWindow.display(true);
	}
	
	@FXML
	protected void handleSettingsButtonAction(ActionEvent event) {
		log.debug("Settings button pressed");
		
		settingsController.bindSystemSettings();
		settingsWindow.display(true);
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
		} else if (!playlistManager.getPlaylist(currentSelectedPlaylistId).isEmpty() && 
			playlistManager.getSelectedTrack() == null) {
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
		equalizerWindow.display(true);
	}
	
	@FXML
	protected void handleRandomButtonAction(ActionEvent event) {
		log.debug("Random button pressed");

		playlistManager.setPlaylistTracks(PLAYLIST_ID_SEARCH, searchManager.getRandomPlaylist(randomPlaylistSize));
		playlistManager.playPlaylist(PLAYLIST_ID_SEARCH);
	}

	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case APPLICATION_INITIALISED: {
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

				if (!timeSlider.isDisabled() && mediaDuration.greaterThan(Duration.ZERO) && !timeSlider.isSliderValueChanging()) {
					timeSlider.setSliderValue(currentTime.divide(mediaDuration.toMillis()).toMillis() * 100.0);
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
				// If we have created or deleted a playlist, update the observable list
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
								
								// If this is a playlist creation event, go straight into edit mode
								if (event == Event.PLAYLIST_CREATED) {
									if (payload.length > 1 && (Boolean)payload[1]) {
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
						playingImageView.setImage(new Image(cacheManager.constructInternalUrl(CacheType.IMAGE, track.getAlbumId(), track.getAlbumImage()), true));
					} else if (track.getArtistImage() != null && track.getArtistImage().trim().length() > 0) {
						playingImageView.setImage(new Image(cacheManager.constructInternalUrl(CacheType.IMAGE, track.getAlbumId(), track.getArtistImage()), true));
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
