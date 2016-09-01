package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import uk.co.mpcontracting.ioc.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.MessageManager;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class TrackTableCellFactory<S, T> extends EventAwareObject implements Callback<TableColumn<TrackTableModel, T>, TableCell<TrackTableModel, T>>, Constants {

	private MessageManager messageManager;
	private PlaylistManager playlistManager;

	public TrackTableCellFactory() {
		messageManager = ApplicationContext.getBean(MessageManager.class);
		playlistManager = ApplicationContext.getBean(PlaylistManager.class);
	}
	
	@Override
	public TableCell<TrackTableModel, T> call(TableColumn<TrackTableModel, T> tableColumn) {
		final TrackTableCell<TrackTableModel, T> tableCell = new TrackTableCell<TrackTableModel, T>();
		
		//////////////////
		// Mouse Events //
		//////////////////
		
		tableCell.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (event.getClickCount() > 1) {
					// Double click
					if (tableCell != null && tableCell.getItem() != null) {
						playlistManager.playTrack(((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
					}
				} else {
					// Single click
					if (tableCell != null && tableCell.getItem() != null) {
						fireEvent(Event.TRACK_SELECTED, ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
					}
				}
			}
		});
		
		//////////////////
		// Context Menu //
		//////////////////
		
		ContextMenu contextMenu = new ContextMenu();
		
		final MenuItem createPlaylistFromAlbumItem = new MenuItem(messageManager.getMessage(MESSAGE_TRACK_TABLE_CONTEXT_CREATE_PLAYLIST_FROM_ALBUM));
		createPlaylistFromAlbumItem.setOnAction(event -> {
			if (tableCell != null && tableCell.getItem() != null) {
				playlistManager.createPlaylistFromAlbum(((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
			}
		});
		contextMenu.getItems().add(createPlaylistFromAlbumItem);
		
		final MenuItem deleteTrackFromPlaylistItem = new MenuItem(messageManager.getMessage(MESSAGE_TRACK_TABLE_CONTEXT_DELETE_TRACK_FROM_PLAYLIST));
		deleteTrackFromPlaylistItem.setOnAction(event -> {
			if (tableCell != null && tableCell.getItem() != null) {
				Track track = ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack();
				
				playlistManager.removeTrackFromPlaylist(track.getPlaylistId(), track);
			}
		});
		contextMenu.getItems().add(deleteTrackFromPlaylistItem);
		
		tableCell.setContextMenu(contextMenu);
		tableCell.setOnContextMenuRequested(event -> {
			if (tableCell != null && tableCell.getItem() != null) {
				if (((TrackTableModel)tableCell.getTableRow().getItem()).getTrack().getPlaylistId() == PLAYLIST_ID_SEARCH) {
					createPlaylistFromAlbumItem.setDisable(false);
					deleteTrackFromPlaylistItem.setDisable(true);
				} else {
					createPlaylistFromAlbumItem.setDisable(true);
					deleteTrackFromPlaylistItem.setDisable(false);
				}
			} else {
				createPlaylistFromAlbumItem.setDisable(true);
				deleteTrackFromPlaylistItem.setDisable(true);
			}
		});

		///////////////////
		// Drag And Drop //
		///////////////////
		
		tableCell.setOnDragDetected(event -> {
			if (tableCell != null && tableCell.getItem() != null) {
				Dragboard dragboard = tableCell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put(DND_TRACK_DATA_FORMAT, ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
				dragboard.setContent(clipboardContent);

				event.consume();
			}
		});
		
		tableCell.setOnDragOver(event -> {
			if (event.getGestureSource() != tableCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT) &&
				tableCell.getTableRow().getItem() != null && ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack().getPlaylistId() != PLAYLIST_ID_SEARCH) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
	
			event.consume();
		});
		
		tableCell.setOnDragEntered(event -> {
			if (event.getGestureSource() != tableCell && event.getDragboard().hasContent(DND_TRACK_DATA_FORMAT) &&
				tableCell.getTableRow().getItem() != null && ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack().getPlaylistId() != PLAYLIST_ID_SEARCH) {
				tableCell.getTableRow().setStyle("-fx-background-color: -jb-border-color");
			}
			
			event.consume();
		});

		tableCell.setOnDragExited(event -> {
			tableCell.getTableRow().setStyle(null);
			
			event.consume();
		});
		
		tableCell.setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			
			if (dragboard.hasContent(DND_TRACK_DATA_FORMAT)) {
				Track source = (Track)dragboard.getContent(DND_TRACK_DATA_FORMAT);
				Track target = ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack();
				
				playlistManager.moveTracksInPlaylist(source.getPlaylistId(), source, target);
				
				event.setDropCompleted(true);
			}
			
			event.consume();
		});

		tableCell.setOnDragDone(event -> {
			event.consume();
		});

		return tableCell;
	}
}
