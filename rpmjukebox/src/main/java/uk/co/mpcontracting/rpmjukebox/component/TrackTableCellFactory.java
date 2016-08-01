package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;

public class TrackTableCellFactory<S, T> implements Callback<TableColumn<TrackTableModel, T>, TableCell<TrackTableModel, T>> {

	private PlaylistManager playlistManager;

	public TrackTableCellFactory(PlaylistManager playlistManager) {
		this.playlistManager = playlistManager;
	}
	
	@Override
	public TableCell<TrackTableModel, T> call(TableColumn<TrackTableModel, T> tableColumn) {
		final TrackTableCell<TrackTableModel, T> tableCell = new TrackTableCell<TrackTableModel, T>();
		
		//////////////////
		// Mouse Events //
		//////////////////
		
		tableCell.setOnMouseClicked(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (event.getClickCount() > 1) {
						// Double click
						if (tableCell != null && tableCell.getItem() != null) {
							playlistManager.playTrackAtIndex(tableCell.getIndex());
						}
					}
				}
			}
		});

		///////////////////
		// Drag And Drop //
		///////////////////

		/*listCell.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard dragboard = listCell.startDragAndDrop(TransferMode.COPY);
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put(DND_TRACK_DATA_FORMAT, tableCell.getTableRow().getItem());
				dragboard.setContent(clipboardContent);

				event.consume();
			}
		});

		listCell.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				event.consume();
			}
		});*/
		
		return tableCell;
	}
}
