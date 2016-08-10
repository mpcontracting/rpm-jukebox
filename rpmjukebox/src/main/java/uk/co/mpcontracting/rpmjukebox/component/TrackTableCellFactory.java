package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class TrackTableCellFactory<S, T> implements Callback<TableColumn<TrackTableModel, T>, TableCell<TrackTableModel, T>>, Constants {

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
							playlistManager.playTrack(((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
						}
					}
				}
			}
		});

		///////////////////
		// Drag And Drop //
		///////////////////
		
		tableCell.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Dragboard dragboard = tableCell.startDragAndDrop(TransferMode.COPY);
				ClipboardContent clipboardContent = new ClipboardContent();
				clipboardContent.put(DND_TRACK_DATA_FORMAT, ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack());
				dragboard.setContent(clipboardContent);

				event.consume();
			}
		});
		
		tableCell.setOnDragDone(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				event.consume();
			}
		});

		return tableCell;
	}
}
