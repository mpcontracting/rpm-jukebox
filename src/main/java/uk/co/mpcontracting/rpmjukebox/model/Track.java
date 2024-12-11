package uk.co.mpcontracting.rpmjukebox.model;

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Track implements Serializable {
  @EqualsAndHashCode.Include private String artistId;
  private String artistName;
  @EqualsAndHashCode.Include private String albumId;
  private String albumName;
  private String albumImage;
  private int year;
  @EqualsAndHashCode.Include private String trackId;
  private String trackName;
  private int index;
  private String location;
  private boolean isPreferred;
  private List<String> genres;

  private int playlistId;
  private int playlistIndex;

  public Track createClone() {
    Track clone = Track.builder()
        .artistId(artistId)
        .artistName(artistName)
        .albumId(albumId)
        .albumName(albumName)
        .albumImage(albumImage)
        .year(year)
        .trackId(trackId)
        .trackName(trackName)
        .index(index)
        .location(location)
        .isPreferred(isPreferred)
        .genres(genres)
        .build();

    clone.setPlaylistId(playlistId);
    clone.setPlaylistIndex(playlistIndex);

    return clone;
  }
}
