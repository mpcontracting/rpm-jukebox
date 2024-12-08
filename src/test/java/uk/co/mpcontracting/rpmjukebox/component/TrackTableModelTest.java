package uk.co.mpcontracting.rpmjukebox.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;

import org.junit.jupiter.api.Test;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class TrackTableModelTest extends AbstractGuiTest {

  @Test
  void shouldInitialise() {
    Track track = createTrack(1, createGenre(), createGenre());
    TrackTableModel underTest = new TrackTableModel(track);

    assertThat(underTest.getTrack()).isEqualTo(track);
    assertThat(underTest.getTrackId().get()).isEqualTo(track.getTrackId());
    assertThat(underTest.getTrackName().get()).isEqualTo(track.getTrackName());
    assertThat(underTest.getArtistName().get()).isEqualTo(track.getArtistName());
    assertThat(underTest.getAlbumYear().get()).isEqualTo(track.getYear());
    assertThat(underTest.getAlbumName().get()).isEqualTo(track.getAlbumName());
    assertThat(underTest.getGenres().get()).isEqualTo(track.getGenres().get(0) + ", " + track.getGenres().get(1));
  }

  @Test
  void shouldInitialiseWithNoGenres() {
    Track track = createTrack(1);
    TrackTableModel underTest = new TrackTableModel(track);

    assertThat(underTest.getTrack()).isEqualTo(track);
    assertThat(underTest.getTrackId().get()).isEqualTo(track.getTrackId());
    assertThat(underTest.getTrackName().get()).isEqualTo(track.getTrackName());
    assertThat(underTest.getArtistName().get()).isEqualTo(track.getArtistName());
    assertThat(underTest.getAlbumYear().get()).isEqualTo(track.getYear());
    assertThat(underTest.getAlbumName().get()).isEqualTo(track.getAlbumName());
    assertThat(underTest.getGenres().get()).isEqualTo("");
  }
}