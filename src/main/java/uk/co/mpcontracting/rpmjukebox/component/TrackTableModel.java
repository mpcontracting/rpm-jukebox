package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Getter
public class TrackTableModel {

    private Track track;
    private StringProperty trackId;
    private StringProperty trackName;
    private StringProperty artistName;
    private IntegerProperty albumYear;
    private StringProperty albumName;
    private StringProperty genres;

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
