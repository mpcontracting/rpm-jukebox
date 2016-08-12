package uk.co.mpcontracting.rpmjukebox.controller;

import java.util.Collections;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.ModalWindow;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;
import uk.co.mpcontracting.rpmjukebox.support.StringHelper;

@Slf4j
@Component
public class MainPanelController extends EventAwareObject implements Constants {

	@FXML
	private TextField searchTextField;
	
	@FXML
	private ListView<Playlist> playlistPanelListView;
	
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
	private SearchManager searchManager;
	
	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MediaManager mediaManager;

	@Getter private ModalWindow equalizerDialogue;
	private ObservableList<Playlist> observablePlaylists;

	@FXML
	public void initialize() {
		searchTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				searchTextUpdated(newValue);
			}
		});
		
		timeSlider.sliderValueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable observable) {
				if (timeSlider.isSliderValueChanging()) {
					mediaManager.setSeekPositionPercent(timeSlider.getSliderValue());
				}
			}
		});

		volumeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable observable) {
				if (volumeSlider.isValueChanging()) {
					mediaManager.setVolumePercent(volumeSlider.getValue());
				}
			}
		});

		playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
		volumeSlider.setValue(mediaManager.getVolume() * 100);
		
		// Equalizer dialogue
		equalizerDialogue = new ModalWindow(FxmlContext.getBean(RpmJukebox.class).getStage(), "equalizer.fxml");
		
		// Playlist list view
		observablePlaylists = FXCollections.observableArrayList();
		playlistPanelListView.setCellFactory(new PlaylistListCellFactory(playlistManager));
		playlistPanelListView.setItems(observablePlaylists);
		
		// Track table view
		mainPanel.setCenter((Node)FxmlContext.loadFxml("tracktable.fxml"));
	}
	
	private void searchTextUpdated(String searchText) {
		log.info("Search text updated - '" + searchText + "'");

		if (searchText != null && searchText.trim().length() > 0) {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, searchManager.search(new TrackSearch(searchText.trim())));
		} else {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, Collections.emptyList());
		}
		
		fireEvent(Event.PLAYLIST_SELECTED, SEARCH_PLAYLIST_ID);
	}
	
	private void updateObservablePlaylists() {
		log.info("Updating observable playlists");

		observablePlaylists.setAll(playlistManager.getPlaylists());
	}
	
	private void setShuffleButtonImage() {
		if (playlistManager.isShuffle()) {
			shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_ON + "')");
		} else {
			shuffleButton.setStyle("-fx-background-image: url('" + IMAGE_SHUFFLE_OFF + "')");
		}
	}
	
	private void setRepeatButtonImage() {
		if (playlistManager.isRepeat()) {
			repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_ON + "')");
		} else {
			repeatButton.setStyle("-fx-background-image: url('" + IMAGE_REPEAT_OFF + "')");
		}
	}
	
	@FXML
	protected void handlePreviousButtonAction(ActionEvent event) {
		log.info("Previous button pressed");
		
		if (mediaManager.getPlayingTimeSeconds() > PREVIOUS_SECONDS_CUTOFF) {
			mediaManager.setSeekPositionPercent(0);
		} else {
			playlistManager.playPreviousTrack();
		}
	}
	
	@FXML
	protected void handlePlayPauseButtonAction(ActionEvent event) {
		log.info("Play/pause button pressed");

		if (mediaManager.isPlaying()) {
			playlistManager.pauseCurrentTrack();
		} else if (mediaManager.isPaused()) {
			playlistManager.resumeCurrentTrack();
		} else {
			playlistManager.playCurrentTrack();
		}
	}
	
	@FXML
	protected void handleNextButtonAction(ActionEvent event) {
		log.info("Next button pressed");
		
		playlistManager.playNextTrack();
	}
	
	@FXML
	protected void handleShuffleButtonAction(ActionEvent event) {
		log.info("Shuffle button pressed");
		
		playlistManager.setSuffle(!playlistManager.isShuffle());
		
		setShuffleButtonImage();
	}
	
	@FXML
	protected void handleRepeatButtonAction(ActionEvent event) {
		log.info("Repeat button pressed");

		playlistManager.setRepeat(!playlistManager.isRepeat());
		
		setRepeatButtonImage();
	}

	@FXML
	protected void handleEqButtonAction(ActionEvent event) {
		log.info("EQ button pressed");

		equalizerDialogue.display();
	}
	
	@FXML
	protected void handleRandomButtonAction(ActionEvent event) {
		log.info("Random button pressed");

		playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, searchManager.getRandomPlaylist(RANDOM_PLAYLIST_SIZE));
		playlistManager.playPlaylist(SEARCH_PLAYLIST_ID);
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
				setShuffleButtonImage();
				setRepeatButtonImage();
				
				// Enable GUI components
				searchTextField.setDisable(false);
				timeSlider.setDisable(false);
				volumeSlider.setDisable(false);
				//shuffleButton.setDisable(false);
				repeatButton.setDisable(false);
				//eqButton.setDisable(false);
				randomButton.setDisable(false);

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
				timeSlider.setProgressValue(0);
				previousButton.setDisable(true);
				nextButton.setDisable(true);

				break;
			}
			case PLAYLIST_CREATED:
			case PLAYLIST_DELETED: {
				updateObservablePlaylists();

				if (payload != null && payload.length > 0) {
					Integer selectedPlaylist = (Integer)payload[0];
					
					// Select the correct playlist
					if (observablePlaylists.size() > selectedPlaylist) {
						playlistPanelListView.getSelectionModel().select(selectedPlaylist);
						playlistPanelListView.getFocusModel().focus(selectedPlaylist);
					}
				}
				
				break;
			}
			case PLAYLIST_SELECTED: {
				if (payload != null && payload.length > 0) {
					Integer playlistId = (Integer)payload[0];
		
					if (playlistId != null) {
						for (int i = 0; i < observablePlaylists.size(); i++) {
							Playlist playlist = observablePlaylists.get(i);
							
							if (playlist.getPlaylistId() == playlistId) {
								playlistPanelListView.getSelectionModel().select(i);
								playlistPanelListView.getFocusModel().focus(i);
								
								break;
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
						playingImageView.setImage(new Image(track.getAlbumImage(), true));
					} else if (track.getArtistImage() != null && track.getArtistImage().trim().length() > 0) {
						playingImageView.setImage(new Image(track.getArtistImage(), true));
					} else {
						playingImageView.setImage(new Image(IMAGE_NO_ARTWORK));
					}
					
					playPauseButton.setDisable(true);
				}
				
				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
