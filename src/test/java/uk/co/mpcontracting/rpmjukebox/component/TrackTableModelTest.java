package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TrackTableModelTest extends AbstractGUITest {

    @Test
    public void shouldInitialise() {
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789",
            "Track Name", 1, "Location", true, Arrays.asList("Genre 1", "Genre 2"));
        TrackTableModel trackTableModel = new TrackTableModel(track);

        assertThat(trackTableModel.getTrack()).isEqualTo(track);
        assertThat(trackTableModel.getTrackId().get()).isEqualTo("789");
        assertThat(trackTableModel.getTrackName().get()).isEqualTo("Track Name");
        assertThat(trackTableModel.getArtistName().get()).isEqualTo("Artist Name");
        assertThat(trackTableModel.getAlbumYear().get()).isEqualTo(2000);
        assertThat(trackTableModel.getAlbumName().get()).isEqualTo("Album Name");
        assertThat(trackTableModel.getGenres().get()).isEqualTo("Genre 1, Genre 2");
    }

    @Test
    public void shouldInitialiseWithNoGenres() {
        Track track = new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image", 2000, "789",
            "Track Name", 1, "Location", true, null);
        TrackTableModel trackTableModel = new TrackTableModel(track);

        assertThat(trackTableModel.getTrack()).isEqualTo(track);
        assertThat(trackTableModel.getTrackId().get()).isEqualTo("789");
        assertThat(trackTableModel.getTrackName().get()).isEqualTo("Track Name");
        assertThat(trackTableModel.getArtistName().get()).isEqualTo("Artist Name");
        assertThat(trackTableModel.getAlbumYear().get()).isEqualTo(2000);
        assertThat(trackTableModel.getAlbumName().get()).isEqualTo("Album Name");
        assertThat(trackTableModel.getGenres().get()).isEqualTo("");
    }
}
