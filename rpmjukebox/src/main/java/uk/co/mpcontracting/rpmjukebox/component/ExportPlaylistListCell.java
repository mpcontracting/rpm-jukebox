package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import uk.co.mpcontracting.ioc.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.controller.ExportController;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class ExportPlaylistListCell extends ListCell<Playlist> {

	private static ExportController exportController;
	
	public ExportPlaylistListCell() {
		exportController = ApplicationContext.getBean(ExportController.class);
	}
	
	@Override
	public void updateItem(Playlist playlist, boolean empty) {
		super.updateItem(playlist, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			setText(playlist.getName());
			
			CheckBox checkBox = new CheckBox();
			checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
				exportController.setPlaylistToExport(playlist.getPlaylistId(), newValue);
			});
			
			setGraphic(checkBox);
		}
	}
}
