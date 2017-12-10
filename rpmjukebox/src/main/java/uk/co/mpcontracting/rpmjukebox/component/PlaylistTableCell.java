package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;

public class PlaylistTableCell<S, T> extends TableCell<PlaylistTableModel, T> {

	@Override
	protected void updateItem(T value, boolean empty) {
		super.updateItem(value, empty);
		
		setGraphic(null);
		
		if (empty || value == null) {
			setText(null);
		} else {
			setText(value.toString());
		}
	}
}
