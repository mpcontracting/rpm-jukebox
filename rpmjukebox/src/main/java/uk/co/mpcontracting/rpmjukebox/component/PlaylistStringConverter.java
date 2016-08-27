package uk.co.mpcontracting.rpmjukebox.component;

import javafx.util.StringConverter;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

public class PlaylistStringConverter<T extends Playlist> extends StringConverter<T> {

	private T playlist;
	private String originalName;

	public void setPlaylist(T playlist) {
		this.playlist = playlist;
		this.originalName = playlist.getName();
	}
	
	@Override
	public String toString(T playlist) {
		return playlist.getName();
	}

	@Override
	public T fromString(String string) {
		if (string != null && string.trim().length() > 0) {
			playlist.setName(string);
		} else {
			playlist.setName(originalName);
		}
		
		return playlist;
	}
}
