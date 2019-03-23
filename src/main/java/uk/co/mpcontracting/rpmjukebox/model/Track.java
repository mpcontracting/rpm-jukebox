package uk.co.mpcontracting.rpmjukebox.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode(of = { "artistId", "albumId", "trackId" })
public class Track implements Serializable, Cloneable {
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

    private String artistId;
    private String artistName;
    private String artistImage;
    private String albumId;
    private String albumName;
    private String albumImage;
    private int year;
    private String trackId;
    private String trackName;
    private int number;
    private String location;
    private boolean isPreferred;
    private List<String> genres;

    @Setter
    private int playlistId;

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
