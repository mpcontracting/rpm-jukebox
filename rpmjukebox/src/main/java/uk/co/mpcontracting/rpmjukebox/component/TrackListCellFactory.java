package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class TrackListCellFactory implements Callback<ListView<Track>, ListCell<Track>>, Constants {

	private PlaylistManager playlistManager;

	public TrackListCellFactory(PlaylistManager playlistManager) {
		this.playlistManager = playlistManager;
	}
	
	@Override
	public ListCell<Track> call(ListView<Track> listView) {
		final TrackListCell listCell = new TrackListCell();

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
							playlistManager.playTrackAtIndex(listCell.getIndex());
						}
					} else {
						// Single click
						if (listCell != null && listCell.getItem() != null) {
							//EventManager.getInstance().fireEvent(Event.UPDATE_VISIBLE_PLAYLIST, listCell.getItem().getPlaylistId());
						}
					}
				}
			}
		});
		
		///////////////////
		// Drag And Drop //
		///////////////////

		listCell.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard dragboard = listCell.startDragAndDrop(TransferMode.COPY);
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put(DND_TRACK_DATA_FORMAT, listCell.getItem());
				dragboard.setContent(clipboardContent);

				event.consume();
			}
		});

		listCell.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				event.consume();
			}
		});

		return listCell;
	}
}
