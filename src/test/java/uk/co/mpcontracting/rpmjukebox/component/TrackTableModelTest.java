package uk.co.mpcontracting.rpmjukebox.component;

import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;

public class TrackTableModelTest extends AbstractGUITest {

    @Test
    public void shouldInitialise() {
        Track track = generateTrack(1, "Genre 1", "Genre 2");
        TrackTableModel trackTableModel = new TrackTableModel(track);

        assertThat(trackTableModel.getTrack()).isEqualTo(track);
        assertThat(trackTableModel.getTrackId().get()).isEqualTo("7891");
        assertThat(trackTableModel.getTrackName().get()).isEqualTo("Track Name 1");
        assertThat(trackTableModel.getArtistName().get()).isEqualTo("Artist Name 1");
        assertThat(trackTableModel.getAlbumYear().get()).isEqualTo(2001);
        assertThat(trackTableModel.getAlbumName().get()).isEqualTo("Album Name 1");
        assertThat(trackTableModel.getGenres().get()).isEqualTo("Genre 1, Genre 2");
    }

    @Test
    public void shouldInitialiseWithNoGenres() {
        Track track = generateTrack(1);
        TrackTableModel trackTableModel = new TrackTableModel(track);

        assertThat(trackTableModel.getTrack()).isEqualTo(track);
        assertThat(trackTableModel.getTrackId().get()).isEqualTo("7891");
        assertThat(trackTableModel.getTrackName().get()).isEqualTo("Track Name 1");
        assertThat(trackTableModel.getArtistName().get()).isEqualTo("Artist Name 1");
        assertThat(trackTableModel.getAlbumYear().get()).isEqualTo(2001);
        assertThat(trackTableModel.getAlbumName().get()).isEqualTo("Album Name 1");
        assertThat(trackTableModel.getGenres().get()).isEqualTo("");
    }
}
