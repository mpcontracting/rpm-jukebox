package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;

public class TrackTableCell<S, T> extends TableCell<TrackTableModel, T>  {

	@Override
	protected void updateItem(T value, boolean empty) {
		super.updateItem(value, empty);

		if (empty || value == null) {
			setText(null);
		} else {
			setText(value.toString());
		}
	}
}
