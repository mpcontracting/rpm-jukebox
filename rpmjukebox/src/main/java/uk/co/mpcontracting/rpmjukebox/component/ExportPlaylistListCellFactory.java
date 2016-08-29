package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class ExportPlaylistListCellFactory implements Callback<ListView<Playlist>, ListCell<Playlist>> {

	@Override
	public ListCell<Playlist> call(ListView<Playlist> listView) {
		return new ExportPlaylistListCell();
	}
}
