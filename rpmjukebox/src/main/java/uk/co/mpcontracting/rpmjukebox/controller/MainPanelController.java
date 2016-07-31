package uk.co.mpcontracting.rpmjukebox.controller;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.component.SliderProgressBar;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class MainPanelController extends EventAwareObject implements Initializable, Constants {

	@FXML
	private TextField searchTextField;
	
	@FXML
	private Button backButton;
	
	@FXML
	private Button playPauseButton;
	
	@FXML
	private Button forwardButton;
	
	@FXML
	private SliderProgressBar timeSlider;
	
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
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		searchTextField.textProperty().addListener(
			new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					searchTextUpdated(newValue);
				}
			}
		);
	}
	
	private void searchTextUpdated(String searchText) {
		log.info("Search text updated - '" + searchText + "'");

		if (searchText != null && searchText.trim().length() > 0) {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, searchManager.search(new TrackSearch(searchText.trim())));
		} else {
			playlistManager.setPlaylistTracks(SEARCH_PLAYLIST_ID, Collections.emptyList());
		}
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case APPLICATION_INITIALISED: {
				log.info("Application initialised event received");
				
				searchTextField.setDisable(false);
				backButton.setDisable(false);
				playPauseButton.setDisable(false);
				forwardButton.setDisable(false);
				timeSlider.setDisable(false);
				volumeSlider.setDisable(false);
				repeatButton.setDisable(false);
				eqButton.setDisable(false);
				
				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
