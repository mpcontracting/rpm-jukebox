package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
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
		
		return tableCell;
	}
}
