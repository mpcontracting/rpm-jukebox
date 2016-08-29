package uk.co.mpcontracting.rpmjukebox.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.component.ExportPlaylistListCellFactory;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class ExportController extends EventAwareObject implements Constants {

	@FXML
	private ListView<Playlist> playlistListView;
	
	@FXML
	private Button cancelButton;

	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MainPanelController mainPanelController;
	
	private ObservableList<Playlist> observablePlaylists;
	private Set<Integer> playlistsToExport;
	
	@FXML
	public void initialize() {
		log.info("Initialising ExportController");
		
		observablePlaylists = FXCollections.observableArrayList();
		playlistListView.setCellFactory(new ExportPlaylistListCellFactory());
		playlistListView.setItems(observablePlaylists);
		
		playlistsToExport = new HashSet<Integer>();
	}
	
	public void bindPlaylists() {
		observablePlaylists.clear();
		playlistsToExport.clear();
		
		List<Playlist> playlists = playlistManager.getPlaylists();
		
		for (Playlist playlist : playlists) {
			if (playlist.getPlaylistId() > 0 || playlist.getPlaylistId() == PLAYLIST_ID_FAVOURITES) {
				observablePlaylists.add(playlist);
			}
		}

		cancelButton.requestFocus();
	}
	
	public void setPlaylistToExport(int playlistId, boolean export) {
		log.info("Setting playlist to export : ID - " + playlistId + ", Export - " + export);
		
		if (export) {
			playlistsToExport.add(playlistId);
		} else {
			playlistsToExport.remove(playlistId);
		}
	}
	
	private void exportPlaylists() {
		log.info("Exporting playlists - " + playlistsToExport);
		fireEvent(Event.PLAYLISTS_EXPORTED);
	}
	
	@FXML
	protected void handleOkButtonAction(ActionEvent event) {
		// Don't run this on the GUI thread
		ThreadRunner.run(() -> {
			exportPlaylists();
		});
	}
	
	@FXML
	protected void handleCancelButtonAction(ActionEvent event) {
		mainPanelController.getExportWindow().close();
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case PLAYLISTS_EXPORTED: {
				mainPanelController.getExportWindow().close();
				
				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
