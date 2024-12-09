package uk.co.mpcontracting.rpmjukebox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackTest {

  @Test
  void shouldTestHashCode() {
    Track track1 = createTrack(1, createGenre(), createGenre());
    track1.setPlaylistId(4);
    track1.setPlaylistIndex(5);

    Track track2 = Track.builder()
        .artistId(track1.getArtistId())
        .albumId(track1.getAlbumId())
        .trackId(track1.getTrackId())
        .build();

    assertThat(track1.hashCode()).isEqualTo(track2.hashCode());
  }

  @Test
  void shouldTestEquals() {
    Track track1 = createTrack(1, createGenre(), createGenre());
    track1.setPlaylistId(4);
    track1.setPlaylistIndex(5);

    Track track2 = Track.builder()
        .artistId(track1.getArtistId())
        .albumId(track1.getAlbumId())
        .trackId(track1.getTrackId())
        .build();

    assertThat(track1).isEqualTo(track2);
  }

  @Test
  void shouldTestClone() {
    Track track1 = createTrack(1, createGenre(), createGenre());
    track1.setPlaylistId(4);
    track1.setPlaylistIndex(5);

    Track track2 = track1.createClone();

    assertThat(track1).isNotSameAs(track2);
    assertThat(track1.getArtistId()).isEqualTo(track2.getArtistId());
    assertThat(track1.getArtistName()).isEqualTo(track2.getArtistName());
    assertThat(track1.getAlbumId()).isEqualTo(track2.getAlbumId());
    assertThat(track1.getAlbumName()).isEqualTo(track2.getAlbumName());
    assertThat(track1.getAlbumImage()).isEqualTo(track2.getAlbumImage());
    assertThat(track1.getYear()).isEqualTo(track2.getYear());
    assertThat(track1.getTrackId()).isEqualTo(track2.getTrackId());
    assertThat(track1.getTrackName()).isEqualTo(track2.getTrackName());
    assertThat(track1.getIndex()).isEqualTo(track2.getIndex());
    assertThat(track1.getLocation()).isEqualTo(track2.getLocation());
    assertThat(track1.isPreferred()).isEqualTo(track2.isPreferred());
    assertThat(track1.getGenres()).isEqualTo(track2.getGenres());
  }
}