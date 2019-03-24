package uk.co.mpcontracting.rpmjukebox.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

@Data
@Builder
public class Track implements Serializable, Cloneable {
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

    private int playlistId;
    private int playlistIndex;

    @Override
    public int hashCode() {
        return reflectionHashCode(this,
                "artistName",
                "artistImage",
                "albumName",
                "albumImage",
                "year",
                "trackName",
                "number",
                "location",
                "isPreferred",
                "genres",
                "playlistId",
                "playlistIndex");
    }

    @Override
    public boolean equals(Object object) {
        return reflectionEquals(this, object,
                "artistName",
                "artistImage",
                "albumName",
                "albumImage",
                "year",
                "trackName",
                "number",
                "location",
                "isPreferred",
                "genres",
                "playlistId",
                "playlistIndex");
    }

    @Override
    public Track clone() {
        Track clone = Track.builder()
                .artistId(artistId)
                .artistName(artistName)
                .artistImage(artistImage)
                .albumId(albumId)
                .albumName(albumName)
                .albumImage(albumImage)
                .year(year)
                .trackId(trackId)
                .trackName(trackName)
                .number(number)
                .location(location)
                .isPreferred(isPreferred)
                .genres(genres)
                .build();

        clone.setPlaylistId(playlistId);
        clone.setPlaylistIndex(playlistIndex);

        return clone;
    }
}
