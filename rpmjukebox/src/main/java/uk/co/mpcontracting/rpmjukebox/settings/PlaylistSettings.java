package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@NoArgsConstructor
@ToString(includeFieldNames = true)
public class PlaylistSettings {
	@Getter private int id;
	@Getter private String name;
	@Getter private List<String> tracks;

	public PlaylistSettings(Playlist playlist) {
		this.id = playlist.getPlaylistId();
		this.name = playlist.getName();
		
		tracks = new ArrayList<String>();
		
		for (Track track : playlist) {
			tracks.add(track.getTrackId());
		}
	}
}
