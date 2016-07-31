package uk.co.mpcontracting.rpmjukebox.component;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.FxmlHelper;

public class TrackListCell extends ListCell<Track> {

	@FXML
	private ImageView imageView;
	
	@FXML
	private Label label;
	
	private Parent parent;
	
	public TrackListCell() {
		parent = (Parent)FxmlHelper.loadFxml("tracklistcell.fxml", this);
	}
	
	@Override
	public void updateItem(Track track, boolean empty) {
		super.updateItem(track, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			label.setText(track.getArtistName() + " - " + track.getTrackName() + " - " + track.getAlbumName());

			setText(null);
			setGraphic(parent);
		}
	}
}
