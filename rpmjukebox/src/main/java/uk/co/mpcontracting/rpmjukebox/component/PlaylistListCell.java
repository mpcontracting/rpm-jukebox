package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ListCell;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class PlaylistListCell extends ListCell<Playlist> {
	
	@Override
	protected void updateItem(Playlist playlist, boolean empty) {
		super.updateItem(playlist, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			setText(playlist.getName());
		}
	}
}
