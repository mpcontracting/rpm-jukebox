package uk.co.mpcontracting.rpmjukebox.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.generateTrack;

@RunWith(MockitoJUnitRunner.class)
public class TrackTest {

    @Test
    public void shouldTestHashCode() {
        Track track1 = generateTrack(1, "Genre 1", "Genre 2");
        track1.setPlaylistId(4);
        track1.setPlaylistIndex(5);

        Track track2 = Track.builder()
                .artistId("1231")
                .albumId("4561")
                .trackId("7891")
                .build();

        assertThat(track1.hashCode()).isEqualTo(track2.hashCode());
    }

    @Test
    public void shouldTestEquals() {
        Track track1 = generateTrack(1, "Genre 1", "Genre 2");
        track1.setPlaylistId(4);
        track1.setPlaylistIndex(5);

        Track track2 = Track.builder()
                .artistId("1231")
                .albumId("4561")
                .trackId("7891")
                .build();

        assertThat(track1).isEqualTo(track2);
    }

    @Test
    public void shouldTestClone() {
        Track track1 = generateTrack(1, "Genre 1", "Genre 2");
        track1.setPlaylistId(4);
        track1.setPlaylistIndex(5);

        Track track2 = track1.clone();

        assertThat(track1).isNotSameAs(track2);
        assertThat(track1.getArtistId()).isEqualTo(track2.getArtistId());
        assertThat(track1.getArtistName()).isEqualTo(track2.getArtistName());
        assertThat(track1.getAlbumId()).isEqualTo(track2.getAlbumId());
        assertThat(track1.getAlbumName()).isEqualTo(track2.getAlbumName());
        assertThat(track1.getAlbumImage()).isEqualTo(track2.getAlbumImage());
        assertThat(track1.getYear()).isEqualTo(track2.getYear());
        assertThat(track1.getTrackId()).isEqualTo(track2.getTrackId());
        assertThat(track1.getTrackName()).isEqualTo(track2.getTrackName());
        assertThat(track1.getNumber()).isEqualTo(track2.getNumber());
        assertThat(track1.getLocation()).isEqualTo(track2.getLocation());
        assertThat(track1.isPreferred()).isEqualTo(track2.isPreferred());
        assertThat(track1.getGenres()).isEqualTo(track2.getGenres());
    }
}
