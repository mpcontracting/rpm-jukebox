package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Getter
public class TrackTableModel {

    private final Track track;
    private final StringProperty trackId;
    private final StringProperty trackName;
    private final StringProperty artistName;
    private final IntegerProperty albumYear;
    private final StringProperty albumName;
    private final StringProperty genres;

    public TrackTableModel(Track track) {
        this.track = track;

        trackId = new SimpleStringProperty(track.getTrackId());
        trackName = new SimpleStringProperty(track.getTrackName());
        artistName = new SimpleStringProperty(track.getArtistName());
        albumYear = new SimpleIntegerProperty(track.getYear());
        albumName = new SimpleStringProperty(track.getAlbumName());

        StringBuilder builder = new StringBuilder();

        if (track.getGenres() != null) {
            track.getGenres().forEach(genre -> builder.append(genre).append(", "));

            builder.setLength(builder.length() - 2);
        }

        genres = new SimpleStringProperty(builder.toString());
    }
}
