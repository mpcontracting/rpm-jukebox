package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

public class PlaylistListCellFactory extends EventAwareObject implements Callback<ListView<Playlist>, ListCell<Playlist>>, Constants {

	private MessageManager messageManager;
	private PlaylistManager playlistManager;

	public PlaylistListCellFactory() {
		messageManager = FxmlContext.getBean(MessageManager.class);
		playlistManager = FxmlContext.getBean(PlaylistManager.class);
	}
	
	@Override
	public ListCell<Playlist> call(ListView<Playlist> listView) {
		final PlaylistListCell listCell = new PlaylistListCell(new PlaylistStringConverter<Playlist>());

		//////////////////
		// Mouse Events //
		//////////////////

		listCell.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (event.getClickCount() == 1) {
					// Single click
					if (listCell != null && listCell.getItem() != null) {
						fireEvent(Event.PLAYLIST_SELECTED, listCell.getItem().getPlaylistId());
					}
				}
			} else if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() < 2) {
				// Right click
				if (listCell != null && listCell.getItem() != null) {
					fireEvent(Event.PLAYLIST_SELECTED, listCell.getItem().getPlaylistId());
				}
			}
		});

		//////////////////
		// Context Menu //
		//////////////////

		ContextMenu contextMenu = new ContextMenu();
		
		final MenuItem newPlaylistItem = new MenuItem(messageManager.getMessage(MESSAGE_PLAYLIST_CONTEXT_NEW_PLAYLIST));
		newPlaylistItem.setOnAction(event -> {
			playlistManager.createPlaylist();
		});
		contextMenu.getItems().add(newPlaylistItem);
		
		final MenuItem deletePlaylistItem = new MenuItem(messageManager.getMessage(MESSAGE_PLAYLIST_CONTEXT_DELETE_PLAYLIST));
		deletePlaylistItem.setOnAction(event -> {
			Playlist playlist = listView.getSelectionModel().getSelectedItem();

			FxmlContext.getBean(MainPanelController.class).showConfirmWindow(messageManager.getMessage(MESSAGE_PLAYLIST_DELETE_ARE_YOU_SURE, playlist.getName()), 
				true,
				() -> {
					playlistManager.deletePlaylist(playlist.getPlaylistId());
				},
				null
			);
		});
		contextMenu.getItems().add(deletePlaylistItem);
		
		listCell.setContextMenu(contextMenu);
		listCell.setOnContextMenuRequested(event -> {
			@SuppressWarnings("unchecked")
			ListCell<Playlist> sourceCell = (ListCell<Playlist>)event.getSource();
	
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
		});
		
		///////////////////
		// Drag And Drop //
		///////////////////
		
		listCell.setOnDragOver(event -> {
			if (event.getGestureSource() != listCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
				event.acceptTransferModes(TransferMode.COPY);
			}
	
			event.consume();
		});

		listCell.setOnDragEntered(event -> {
			if (event.getGestureSource() != listCell && listCell.getItem() != null && listCell.getItem().getPlaylistId() > -1 && 
				event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT)) {
				listCell.setStyle("-fx-background-color: -jb-foreground-color; -fx-text-fill: -jb-background-color");
			}
	
			event.consume();
		});

		listCell.setOnDragExited(event -> {
			listCell.setStyle(null);
			
			event.consume();
		});

		listCell.setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			
			if (dragboard.hasContent(DND_TRACK_DATA_FORMAT)) {
				playlistManager.addTrackToPlaylist(listCell.getItem().getPlaylistId(), ((Track)dragboard.getContent(DND_TRACK_DATA_FORMAT)).clone());
	
				event.setDropCompleted(true);
			}
	
			event.consume();
		});

		return listCell;
	}
}
