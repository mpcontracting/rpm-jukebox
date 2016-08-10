package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class PlaylistListCellFactory implements Callback<ListView<Playlist>, ListCell<Playlist>>, Constants {

	private PlaylistManager playlistManager;

	public PlaylistListCellFactory(PlaylistManager playlistManager) {
		this.playlistManager = playlistManager;
	}
	
	@Override
	public ListCell<Playlist> call(ListView<Playlist> listView) {
		final PlaylistListCell listCell = new PlaylistListCell();

		//////////////////
		// Mouse Events //
		//////////////////

		listCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (event.getClickCount() > 1) {
						// Double click
						if (listCell != null && listCell.getItem() != null) {
							playlistManager.playPlaylist(listCell.getItem().getPlaylistId());
						}
					} else {
						// Single click
						if (listCell != null && listCell.getItem() != null) {
							EventManager.getInstance().fireEvent(Event.PLAYLIST_SELECTED, listCell.getItem().getPlaylistId());
						}
					}
				}
			}
		});
		
		//////////////////
		// Context Menu //
		//////////////////
		
		ContextMenu contextMenu = new ContextMenu();
		
		final MenuItem newPlaylistItem = new MenuItem("New Playlist");
		newPlaylistItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				playlistManager.createPlaylist();
			}
		});
		contextMenu.getItems().add(newPlaylistItem);
		
		final MenuItem deletePlaylistItem = new MenuItem("Delete Playlist");
		deletePlaylistItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				playlistManager.deletePlaylist(listView.getSelectionModel().getSelectedItem().getPlaylistId());
			}
		});
		contextMenu.getItems().add(deletePlaylistItem);
		
		listCell.setContextMenu(contextMenu);
		listCell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				@SuppressWarnings("unchecked")
				ListCell<Playlist> sourceCell = (ListCell<Playlist>) event.getSource();
		
				if (listView.getItems().size() > sourceCell.getIndex()) {
					Playlist playlist = listView.getItems().get(sourceCell.getIndex());
		
					if (playlist.getPlaylistId() < 0) {
						deletePlaylistItem.setDisable(true);
					} else {
						deletePlaylistItem.setDisable(false);
					}
		
					newPlaylistItem.setDisable(true);
				} else {
					newPlaylistItem.setDisable(false);
					deletePlaylistItem.setDisable(true);
				}
			}
		});
		
		///////////////////
		// Drag And Drop //
		///////////////////
		
		listCell.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (event.getGestureSource() != listCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
					event.acceptTransferModes(TransferMode.COPY);
				}
		
				event.consume();
			}
		});
		
		listCell.setOnDragEntered(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (event.getGestureSource() != listCell && listCell.getItem() != null && listCell.getItem().getPlaylistId() > -1 && 
					event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
					listCell.setStyle("-fx-background-color: brown");
				}
		
				event.consume();
			}
		});
		
		listCell.setOnDragExited(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				listCell.setStyle(null);
		
				event.consume();
			}
		});
		
		listCell.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Dragboard dragboard = event.getDragboard();
		
				if (dragboard.hasContent(DND_TRACK_DATA_FORMAT)) {
					playlistManager.addTrackToPlaylist(listCell.getItem().getPlaylistId(), (Track)dragboard.getContent(DND_TRACK_DATA_FORMAT));
		
					event.setDropCompleted(true);
				}
		
				event.consume();
			}
		});
		
		return listCell;
	}
}
