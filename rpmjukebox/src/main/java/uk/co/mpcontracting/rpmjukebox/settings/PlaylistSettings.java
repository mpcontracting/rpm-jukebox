package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

@NoArgsConstructor
@ToString(includeFieldNames = true)
public class PlaylistSettings {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private List<String> tracks;

    public PlaylistSettings(Playlist playlist) {
        this.id = playlist.getPlaylistId();
        this.name = playlist.getName();

        tracks = new ArrayList<>();

        playlist.forEach(track -> tracks.add(track.getTrackId()));
    }
}
