package uk.co.mpcontracting.rpmjukebox.settings;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;

@Data
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
