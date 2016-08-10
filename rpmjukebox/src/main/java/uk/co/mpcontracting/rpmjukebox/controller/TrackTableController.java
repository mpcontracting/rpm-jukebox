package uk.co.mpcontracting.rpmjukebox.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.component.LoveButtonTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableCellFactory;
import uk.co.mpcontracting.rpmjukebox.component.TrackTableModel;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@Component
public class TrackTableController extends EventAwareObject {

	@FXML
	private TableView<TrackTableModel> trackTableView;
	
	@FXML
	private TableColumn<TrackTableModel, String> loveColumn;

	@FXML
	private TableColumn<TrackTableModel, String> trackNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> artistNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, Number> albumYearColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> albumNameColumn;
	
	@FXML
	private TableColumn<TrackTableModel, String> genresColumn;
	
	@Autowired
	private PlaylistManager playlistManager;
	
	private ObservableList<TrackTableModel> observableTracks;
	private int visiblePlaylistId;
	
	@FXML
	public void initialize() {
		log.info("Initialising TrackTableController");

		// Track table view
		observableTracks = FXCollections.observableArrayList();
		trackTableView.setPlaceholder(new Label(""));
		trackTableView.setItems(observableTracks);
		
		// Cell factories
		loveColumn.setCellFactory(new LoveButtonTableCellFactory<TrackTableModel, String>(playlistManager));
		trackNameColumn.setCellFactory(new TrackTableCellFactory<TrackTableModel, String>(playlistManager));
		artistNameColumn.setCellFactory(new TrackTableCellFactory<TrackTableModel, String>(playlistManager));
		albumYearColumn.setCellFactory(new TrackTableCellFactory<TrackTableModel, Number>(playlistManager));
		albumNameColumn.setCellFactory(new TrackTableCellFactory<TrackTableModel, String>(playlistManager));
		genresColumn.setCellFactory(new TrackTableCellFactory<TrackTableModel, String>(playlistManager));
		
		// Cell value factories
		loveColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackId());
		trackNameColumn.setCellValueFactory(cellData -> cellData.getValue().getTrackName());
		artistNameColumn.setCellValueFactory(cellData -> cellData.getValue().getArtistName());
		albumYearColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumYear());
		albumNameColumn.setCellValueFactory(cellData -> cellData.getValue().getAlbumName());
		genresColumn.setCellValueFactory(cellData -> cellData.getValue().getGenres());
		
		// State variables
		visiblePlaylistId = playlistManager.getCurrentPlaylistId();
	}
	
	private void updateObservableTracks(int playlistId) {
		log.info("Updating observable tracks - " + playlistId);

		observableTracks.clear();
		
		for (Track track : playlistManager.getPlaylist(playlistId).getTracks()) {
			observableTracks.add(new TrackTableModel(track));
		}
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case PLAYLIST_CONTENT_UPDATED: {
				Integer playlistId = (Integer)payload[0];
	
				if (playlistId != null && playlistId.equals(visiblePlaylistId)) {
					updateObservableTracks(playlistId);
				}
	
				break;
			}
			case PLAYLIST_SELECTED: {
				Integer playlistId = (Integer)payload[0];
	
				if (playlistId != null && !playlistId.equals(visiblePlaylistId)) {
					visiblePlaylistId = playlistId;
					updateObservableTracks(visiblePlaylistId);
	
					break;
				}
			}
			default: {
				// Nothing
			}
		}
	}
}
