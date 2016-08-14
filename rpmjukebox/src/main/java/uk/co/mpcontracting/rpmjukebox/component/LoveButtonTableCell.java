package uk.co.mpcontracting.rpmjukebox.component;

import javafx.scene.control.TableCell;
import uk.co.mpcontracting.rpmjukebox.manager.PlaylistManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

public class LoveButtonTableCell<S, T> extends TableCell<TrackTableModel, String> implements Constants {

	private PlaylistManager playlistManager;

	public LoveButtonTableCell(PlaylistManager playlistManager) {
		this.playlistManager = playlistManager;
	}
	
	@Override
	protected void updateItem(String value, boolean empty) {
		super.updateItem(value, empty);

		setText(null);
		setGraphic(null);
		
		if (!empty && value != null) {
			if (playlistManager.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, value)) {
				setId(STYLE_LOVE_BUTTON_ON);
			} else {
				setId(STYLE_LOVE_BUTTON_OFF);
			}
		} else {
			setId(null);
		}
	}
}
