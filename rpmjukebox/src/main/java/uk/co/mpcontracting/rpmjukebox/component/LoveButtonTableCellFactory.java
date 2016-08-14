package uk.co.mpcontracting.rpmjukebox.component;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class LoveButtonTableCellFactory<S, T> extends EventAwareObject implements Callback<TableColumn<TrackTableModel, String>, TableCell<TrackTableModel, String>>, Constants {

	private PlaylistManager playlistManager;

	public LoveButtonTableCellFactory(PlaylistManager playlistManager) {
		this.playlistManager = playlistManager;
	}
	
	@Override
	public TableCell<TrackTableModel, String> call(TableColumn<TrackTableModel, String> tableColumn) {
		final LoveButtonTableCell<TrackTableModel, String> tableCell = new LoveButtonTableCell<TrackTableModel, String>(playlistManager);
		
		//////////////////
		// Mouse Events //
		//////////////////
		
		tableCell.setOnMouseClicked(new EventHandler<MouseEvent> () {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (event.getClickCount() == 1) {
						// Single click
						if (tableCell != null && tableCell.getItem() != null) {
							Track track = ((TrackTableModel)tableCell.getTableRow().getItem()).getTrack();
							
							if (playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, track.getTrackId())) {
								playlistManager.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, track);
							} else {
								playlistManager.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, track);
							}
							
							fireEvent(Event.PLAYLIST_CONTENT_UPDATED, playlistManager.getCurrentPlaylistId());
						}
					}
				}
			}
		});
		
		return tableCell;
	}
}
