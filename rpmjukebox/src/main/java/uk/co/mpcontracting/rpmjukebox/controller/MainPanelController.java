package uk.co.mpcontracting.rpmjukebox.controller;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.component.EqualizerDialogue;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.component.TrackListCellFactory;
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
public class MainPanelController extends EventAwareObject implements Initializable, Constants {

	@FXML
	private TextField searchTextField;
	
	@FXML
	private ListView<Playlist> playlistPanelListView;
	
	@FXML
	private ListView<Track> mainPanelListView;
	
	@FXML
	private Button backButton;
	
	@FXML
	private Button playPauseButton;
	
	@FXML
	private Button forwardButton;
	
	@FXML
	private SliderProgressBar timeSlider;
	
	@FXML
	private Label playTimeLabel;
	
	@FXML
	private Slider volumeSlider;
	
	@FXML
	private Button repeatButton;
	
	@FXML
	private Button eqButton;
	
	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MediaManager mediaManager;

	@Getter private EqualizerDialogue equalizerDialogue;
	private ObservableList<Playlist> observablePlaylists;
	private ObservableList<Track> observableTracks;
	
	private int visiblePlaylistId;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
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
			public void invalidated(Observable ov) {
				if (volumeSlider.isValueChanging()) {
					mediaManager.setVolumePercent(volumeSlider.getValue());
				}
			}
		});

		playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));
		volumeSlider.setValue(mediaManager.getVolume() * 100);
		
		// Equalizer dialogue
		equalizerDialogue = new EqualizerDialogue(FxmlContext.getBean(RpmJukebox.class).getStage());
		
		// Playlist list view
		observablePlaylists = FXCollections.observableArrayList();
		playlistPanelListView.setCellFactory(new PlaylistListCellFactory(playlistManager));
		playlistPanelListView.setItems(observablePlaylists);
		
		// Main panel list view
		observableTracks = FXCollections.observableArrayList();
		mainPanelListView.setCellFactory(new TrackListCellFactory(playlistManager));
		mainPanelListView.setItems(observableTracks);
		
		// State variables
		visiblePlaylistId = playlistManager.getCurrentPlaylistId();
	}
	
	private void searchTextUpdated(String searchText) {
		log.info("Search text updated - '" + searchText + "'");

		if (searchText != null && searchText.trim().length() > 0) {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, searchManager.search(new TrackSearch(searchText.trim())));
		} else {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, Collections.emptyList());
		}
	}
	
	private void updateObservablePlaylists() {
		log.info("Updating observable playlists");

		observablePlaylists.setAll(playlistManager.getPlaylists());
	}
	
	private void updateVisiblePlaylist(Integer playlistId) {
		log.info("Updating visible playlist - " + playlistId);

		observableTracks.setAll(playlistManager.getPlaylist(playlistId).getTracks());
	}
	
	@FXML
	protected void handleEqButtonAction(ActionEvent event) {
		Stage stage = FxmlContext.getBean(RpmJukebox.class).getStage();
		stage.getScene().getRoot().setEffect(new BoxBlur());

		equalizerDialogue.show();
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
				
				// Enable GUI components
				searchTextField.setDisable(false);
				//backButton.setDisable(false);
				//playPauseButton.setDisable(false);
				//forwardButton.setDisable(false);
				//timeSlider.setDisable(false);
				volumeSlider.setDisable(false);
				repeatButton.setDisable(false);
				eqButton.setDisable(false);
				
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
			case MEDIA_STOPPED:
			case END_OF_MEDIA: {
				playTimeLabel.setText(StringHelper.formatElapsedTime(Duration.ZERO, Duration.ZERO));

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
			case PLAYLIST_CONTENT_UPDATED: {
				Integer playlistId = (Integer)payload[0];
	
				if (playlistId != null && playlistId.equals(visiblePlaylistId)) {
					updateVisiblePlaylist(playlistId);
				}
	
				break;
			}
			case UPDATE_VISIBLE_PLAYLIST: {
				Integer playlistId = (Integer)payload[0];
	
				if (playlistId != null && !playlistId.equals(visiblePlaylistId)) {
					visiblePlaylistId = playlistId;
					updateVisiblePlaylist(visiblePlaylistId);
	
					break;
				}
			}
			default: {
				// Nothing
			}
		}
	}
}
