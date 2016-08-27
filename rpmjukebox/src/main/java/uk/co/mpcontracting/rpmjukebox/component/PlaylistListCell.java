package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class PlaylistListCell extends TextFieldListCell<Playlist> {

	public PlaylistListCell(PlaylistStringConverter<Playlist> stringConverter) {
		super(stringConverter);
	}
	
	@Override
	public void updateItem(Playlist playlist, boolean empty) {
		super.updateItem(playlist, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			((PlaylistStringConverter<Playlist>)getConverter()).setPlaylist(playlist);

			if (playlist.getPlaylistId() < 0) {
				setEditable(false);
			}

			setText(playlist.getName());
		}
	}
	
	@Override
	public void startEdit() {
		super.startEdit();

		if (getGraphic() instanceof TextField) {
			TextField textField = (TextField)getGraphic();
			
			textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (newValue != null && !newValue) {
						commitEdit(getItem());
					}
				}
			});
		}
	}
}
