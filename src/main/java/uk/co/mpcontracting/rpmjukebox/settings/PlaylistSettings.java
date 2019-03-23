package uk.co.mpcontracting.rpmjukebox.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor
public class PlaylistSettings {
    private int id;
    private String name;
    private List<String> tracks;

    public PlaylistSettings(Playlist playlist) {
        this.id = playlist.getPlaylistId();
        this.name = playlist.getName();

        tracks = new ArrayList<>();

        playlist.forEach(track -> tracks.add(track.getTrackId()));
    }
}
