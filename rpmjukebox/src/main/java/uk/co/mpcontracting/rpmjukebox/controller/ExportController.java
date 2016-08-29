package uk.co.mpcontracting.rpmjukebox.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableCell;
import uk.co.mpcontracting.rpmjukebox.component.PlaylistTableModel;
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
	private TableView<PlaylistTableModel> playlistTableView;
	
	@FXML
	private TableColumn<PlaylistTableModel, Boolean> selectColumn;

	@FXML
	private TableColumn<PlaylistTableModel, String> playlistColumn;
	
	@FXML
	private Button cancelButton;

	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MainPanelController mainPanelController;
	
	private ObservableList<PlaylistTableModel> observablePlaylists;
	private Set<Integer> playlistsToExport;
	
	@FXML
	public void initialize() {
		log.info("Initialising ExportController");
		
		observablePlaylists = FXCollections.observableArrayList();
		playlistTableView.setPlaceholder(new Label(""));
		playlistTableView.setItems(observablePlaylists);
		playlistTableView.setEditable(true);
		playlistTableView.setSelectionModel(null);
		
		// Hide the table header
		playlistTableView.widthProperty().addListener((observable, oldValue, newValue) -> {
			Pane header = (Pane)playlistTableView.lookup("TableHeaderRow");
			
			if (header != null && header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
				header.setManaged(false);
			}
		});

		// Cell factories
		selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
		playlistColumn.setCellFactory(tableColumn -> { return new PlaylistTableCell<PlaylistTableModel, String>(); });
		
		// Cell value factories
		selectColumn.setCellValueFactory(cellData -> cellData.getValue().getSelected());
		playlistColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		
		// Set the select column to be editable
		selectColumn.setEditable(true);

		playlistsToExport = new HashSet<Integer>();
	}
	
	public void bindPlaylists() {
		observablePlaylists.clear();
		playlistsToExport.clear();
		
		List<Playlist> playlists = playlistManager.getPlaylists();
		
		for (Playlist playlist : playlists) {
			if (playlist.getPlaylistId() > 0 || playlist.getPlaylistId() == PLAYLIST_ID_FAVOURITES) {
				PlaylistTableModel tableModel = new PlaylistTableModel(playlist);
				
				tableModel.getSelected().addListener((observable, oldValue, newValue) -> {
					setPlaylistToExport(tableModel.getPlaylist().getPlaylistId(), newValue);
				});
				
				observablePlaylists.add(tableModel);
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