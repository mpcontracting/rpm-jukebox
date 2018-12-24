package uk.co.mpcontracting.rpmjukebox.model;

import java.io.Serializable;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(includeFieldNames = true)
@EqualsAndHashCode(of = { "artistId", "albumId", "trackId" })
public class Track implements Serializable, Cloneable {
    private static final long serialVersionUID = 55518786963702600L;

    public Track(String artistId, String artistName, String artistImage, String albumId, String albumName,
        String albumImage, int year, String trackId, String trackName, int number, String location, boolean isPreferred,
        List<String> genres) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.artistImage = artistImage;
        this.albumId = albumId;
        this.albumName = albumName;
        this.albumImage = albumImage;
        this.year = year;
        this.trackId = trackId;
        this.trackName = trackName;
        this.number = number;
        this.location = location;
        this.isPreferred = isPreferred;
        this.genres = genres;
    }

    @Getter
    private String artistId;
    @Getter
    private String artistName;
    @Getter
    private String artistImage;
    @Getter
    private String albumId;
    @Getter
    private String albumName;
    @Getter
    private String albumImage;
    @Getter
    private int year;
    @Getter
    private String trackId;
    @Getter
    private String trackName;
    @Getter
    private int number;
    @Getter
    private String location;
    @Getter
    private boolean isPreferred;
    @Getter
    private List<String> genres;

    @Getter
    @Setter
    private int playlistId;
    @Getter
    @Setter
    private int playlistIndex;

    @Override
    public Track clone() {
        Track clone = new Track(artistId, artistName, artistImage, albumId, albumName, albumImage, year, trackId,
            trackName, number, location, isPreferred, genres);
        clone.setPlaylistId(playlistId);
        clone.setPlaylistIndex(playlistIndex);

        return clone;
    }
}
