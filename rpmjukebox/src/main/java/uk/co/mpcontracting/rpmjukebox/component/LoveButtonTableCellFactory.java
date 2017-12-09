package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

public class LoveButtonTableCellFactory<S, T> extends EventAwareObject implements Callback<TableColumn<TrackTableModel, String>, TableCell<TrackTableModel, String>>, Constants {

	private PlaylistManager playlistManager;

	public LoveButtonTableCellFactory() {
		playlistManager = ContextHelper.getBean(PlaylistManager.class);
	}
	
	@Override
	public TableCell<TrackTableModel, String> call(TableColumn<TrackTableModel, String> tableColumn) {
		final LoveButtonTableCell<TrackTableModel, String> tableCell = new LoveButtonTableCell<TrackTableModel, String>(playlistManager);
		
		//////////////////
		// Mouse Events //
		//////////////////
		
		tableCell.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (event.getClickCount() == 1) {
					// Single click
					if (tableCell != null && tableCell.getItem() != null) {
						Track track = ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack();

						if (playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, track.getTrackId())) {
							playlistManager.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
						} else {
							playlistManager.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track.clone());
						}
						
						fireEvent(Event.PLAYLIST_CONTENT_UPDATED, track.getPlaylistId());
					}
				}
			}
		});
		
		return tableCell;
	}
}
