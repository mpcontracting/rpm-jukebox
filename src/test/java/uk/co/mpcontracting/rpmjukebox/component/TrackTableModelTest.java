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
        TrackTableModel underTest = new TrackTableModel(track);

        assertThat(underTest.getTrack()).isEqualTo(track);
        assertThat(underTest.getTrackId().get()).isEqualTo("7891");
        assertThat(underTest.getTrackName().get()).isEqualTo("Track Name 1");
        assertThat(underTest.getArtistName().get()).isEqualTo("Artist Name 1");
        assertThat(underTest.getAlbumYear().get()).isEqualTo(2001);
        assertThat(underTest.getAlbumName().get()).isEqualTo("Album Name 1");
        assertThat(underTest.getGenres().get()).isEqualTo("Genre 1, Genre 2");
    }

    @Test
    public void shouldInitialiseWithNoGenres() {
        Track track = generateTrack(1);
        TrackTableModel underTest = new TrackTableModel(track);

        assertThat(underTest.getTrack()).isEqualTo(track);
        assertThat(underTest.getTrackId().get()).isEqualTo("7891");
        assertThat(underTest.getTrackName().get()).isEqualTo("Track Name 1");
        assertThat(underTest.getArtistName().get()).isEqualTo("Artist Name 1");
        assertThat(underTest.getAlbumYear().get()).isEqualTo(2001);
        assertThat(underTest.getAlbumName().get()).isEqualTo("Album Name 1");
        assertThat(underTest.getGenres().get()).isEqualTo("");
    }
}
