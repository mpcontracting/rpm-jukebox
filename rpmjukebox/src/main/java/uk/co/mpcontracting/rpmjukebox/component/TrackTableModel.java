package uk.co.mpcontracting.rpmjukebox.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import uk.co.mpcontracting.rpmjukebox.model.Track;

public class TrackTableModel {

    @Getter
    private Track track;
    @Getter
    private StringProperty trackId;
    @Getter
    private StringProperty trackName;
    @Getter
    private StringProperty artistName;
    @Getter
    private IntegerProperty albumYear;
    @Getter
    private StringProperty albumName;
    @Getter
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
            for (String genre : track.getGenres()) {
                builder.append(genre).append(", ");
            }

            builder.setLength(builder.length() - 2);
        }

        genres = new SimpleStringProperty(builder.toString());
    }
}
