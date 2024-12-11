package uk.co.mpcontracting.rpmjukebox.model;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaylistTest {

  private Playlist underTest;

  @BeforeEach
  void beforeEach() {
    underTest = createPlaylist(1, "Playlist", 10);
  }

  @Test
  void shouldSetPlaylistId() {
    @SuppressWarnings("unchecked")
    List<Track> tracks = getNonNullField(underTest, "tracks", List.class);

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = getNonNullField(underTest, "shuffledTracks", List.class);

    Track track = spy(tracks.getFirst());
    tracks.set(0, track);

    for (int i = 0; i < tracks.size(); i++) {
      if (shuffledTracks.get(i).equals(track)) {
        shuffledTracks.set(i, track);
        break;
      }
    }

    underTest.setPlaylistId(2);

    verify(track, times(2)).setPlaylistId(2);
  }

  @Test
  void shouldGetTrackAtIndex() {
    Track track = underTest.getTrackAtIndex(5);

    assertThat(track.getTrackId()).isEqualTo("7896");
  }

  @Test
  void shouldFailToGetTrackAtIndex() {
    Track track = underTest.getTrackAtIndex(10);

    assertThat(track).isNull();
  }

  @Test
  void shouldGetPlaylistTrack() {
    Track track = createTrack(1);

    Track result = underTest.getPlaylistTrack(track);

    assertThat(result).isEqualTo(track);
  }

  @Test
  void shouldFailToGetPlaylistTrack() {
    Track track = createTrack(0);

    Track result = underTest.getPlaylistTrack(track);

    assertThat(result).isNull();
  }

  @Test
  void shouldGetShuffledTrackAtIndex() {
    Track track = underTest.getShuffledTrackAtIndex(5);

    assertThat(track).isNotNull();
  }

  @Test
  void shouldFailToGetShuffledTrackAtIndex() {
    Track track = underTest.getShuffledTrackAtIndex(10);

    assertThat(track).isNull();
  }

  @Test
  void shouldShufflePlaylist() {
    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = spy(getNonNullField(underTest, "shuffledTracks", List.class));
    setField(underTest, "shuffledTracks", shuffledTracks);

    underTest.shuffle();

    verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
  }

  @Test
  void shouldSetTrackAtShuffledIndex() {
    Track track = underTest.getTrackAtIndex(1);

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = spy(getNonNullField(underTest, "shuffledTracks", List.class));
    setField(underTest, "shuffledTracks", shuffledTracks);

    underTest.setTrackAtShuffledIndex(track, 5);

    verify(shuffledTracks, times(1)).remove(track);
    verify(shuffledTracks, times(1)).add(5, track);
  }

  @Test
  void shouldGetIsTrackInPlaylist() {
    boolean result = underTest.isTrackInPlaylist("7895");

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotGetIsTrackInPlaylistWhenTrackIdIsNull() {
    boolean result = underTest.isTrackInPlaylist(null);

    assertThat(result).isFalse();
  }

  @Test
  void shouldNotGetIsTrackInPlaylistWhenTrackIdIsUnknown() {
    boolean result = underTest.isTrackInPlaylist("20");

    assertThat(result).isFalse();
  }

  @Test
  void shouldSetTracks() {
    underTest.setTracks(asList(mock(Track.class), mock(Track.class)));

    assertThat(underTest.getTracks()).hasSize(2);
  }

  @Test
  void shouldAddTrack() {
    Track track1 = createTrack(1);
    Track track2 = createTrack(2);
    underTest.setTracks(asList(track1, track2));

    Track addedTrack = createTrack(3);

    underTest.addTrack(addedTrack);

    assertThat(underTest.getTracks()).hasSize(3);
    assertThat(addedTrack.getPlaylistId()).isEqualTo(1);
    assertThat(addedTrack.getPlaylistIndex()).isEqualTo(2);
  }

  @Test
  void shouldNotAddTrackWhenItAlreadyExists() {
    Track track1 = createTrack(1);
    Track track2 = createTrack(2);
    underTest.setTracks(asList(track1, track2));

    underTest.addTrack(createTrack(2));

    assertThat(underTest.getTracks()).hasSize(2);
  }

  @Test
  void shouldNotAddTrackWhenPlaylistSizeAtMaximum() {
    Track track1 = createTrack(1);
    Track track2 = createTrack(2);
    Playlist playlist = new Playlist(1, "Playlist", 2);
    playlist.setTracks(asList(track1, track2));

    playlist.addTrack(createTrack(3));

    assertThat(playlist.getTracks()).hasSize(2);
  }

  @Test
  void shouldRemoveTrack() {
    underTest.removeTrack(createTrack(1));

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = getField(underTest, "shuffledTracks", List.class);

    assertThat(underTest.getTracks()).hasSize(9);
    assertThat(shuffledTracks).hasSize(9);
  }

  @Test
  void shouldNotRemoveTrackWhenItDoesntExist() {
    underTest.removeTrack(createTrack(0));

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = getField(underTest, "shuffledTracks", List.class);

    assertThat(underTest.getTracks()).hasSize(10);
    assertThat(shuffledTracks).hasSize(10);
  }

  @Test
  void shouldSwapTracksSourceLessThanTarget() {
    Track source = underTest.getPlaylistTrack(createTrack(2));
    Track target = underTest.getPlaylistTrack(createTrack(8));

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = spy(getNonNullField(underTest, "shuffledTracks", List.class));
    setField(underTest, "shuffledTracks", shuffledTracks);

    underTest.swapTracks(source, target);

    assertThat(source.getPlaylistIndex()).isEqualTo(7);
    assertThat(target.getPlaylistIndex()).isEqualTo(6);
    verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
  }

  @Test
  void shouldSwapTracksTargetLessThanSource() {
    Track source = underTest.getPlaylistTrack(createTrack(8));
    Track target = underTest.getPlaylistTrack(createTrack(2));

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = spy(getNonNullField(underTest, "shuffledTracks", List.class));
    setField(underTest, "shuffledTracks", shuffledTracks);

    underTest.swapTracks(source, target);

    assertThat(source.getPlaylistIndex()).isEqualTo(1);
    assertThat(target.getPlaylistIndex()).isEqualTo(2);
    verify(shuffledTracks, atLeastOnce()).set(anyInt(), any());
  }

  @Test
  void shouldGetIterator() {
    Iterator<Track> iterator = underTest.iterator();

    assertThat(iterator.hasNext()).isTrue();
  }

  @Test
  void shouldGetSize() {
    assertThat(underTest.size()).isEqualTo(10);
  }

  @Test
  void shouldGetIsEmpty() {
    assertThat(underTest.isEmpty()).isFalse();
  }

  @Test
  void shouldClearPlaylist() {
    underTest.clear();

    @SuppressWarnings("unchecked")
    List<Track> shuffledTracks = (List<Track>) getField(underTest, "shuffledTracks", List.class);

    assertThat(underTest.getTracks()).isEmpty();
    assertThat(shuffledTracks).isEmpty();
  }

  @Test
  void shouldClonePlaylist() {
    Playlist clone = underTest.createClone();

    int playlistMaxSize = getNonNullField(underTest, "maxPlaylistSize", Integer.class);
    int cloneMaxSize = getNonNullField(clone, "maxPlaylistSize", Integer.class);
    SecureRandom playlistRandom = getField(underTest, "random", SecureRandom.class);
    SecureRandom cloneRandom = getField(clone, "random", SecureRandom.class);

    @SuppressWarnings("unchecked")
    List<Track> playlistShuffledTracks = getNonNullField(underTest, "shuffledTracks", List.class);

    @SuppressWarnings("unchecked")
    List<Track> cloneShuffledTracks = getNonNullField(clone, "shuffledTracks", List.class);

    assertThat(clone).isNotSameAs(underTest);
    assertThat(clone.getPlaylistId()).isEqualTo(underTest.getPlaylistId());
    assertThat(clone.getName()).isEqualTo(underTest.getName());
    assertThat(cloneMaxSize).isEqualTo(playlistMaxSize);
    assertThat(getAreTrackListsEqual(clone.getTracks(), underTest.getTracks())).isTrue();
    assertThat(getAreTrackListsEqual(cloneShuffledTracks, playlistShuffledTracks)).isTrue();
    assertThat(cloneRandom).isNotSameAs(playlistRandom);
  }

  private Playlist createPlaylist(int playlistId, String playlistName, int maxPlaylistSize) {
    Playlist playlist = new Playlist(playlistId, playlistName, maxPlaylistSize);
    List<Track> tracks = new ArrayList<>();

    for (int i = 1; i <= maxPlaylistSize; i++) {
      tracks.add(createTrack(i));
    }

    playlist.setTracks(tracks);

    return playlist;
  }

  private Track createTrack(int index, String... genres) {
    return Track.builder()
        .artistId("123" + index)
        .artistName("Artist Name " + index)
        .albumId("456" + index)
        .albumName("Album Name " + index)
        .albumImage("Album Image " + index)
        .year(2000 + index)
        .trackId("789" + index)
        .trackName("Track Name " + index)
        .index(index)
        .location("Location " + index)
        .isPreferred(true)
        .genres(genres.length < 1 ? null : asList(genres))
        .build();
  }

  private boolean getAreTrackListsEqual(List<Track> tracks1, List<Track> tracks2) {
    if (tracks1.size() != tracks2.size()) {
      return false;
    }

    for (int i = 0; i < tracks1.size(); i++) {
      if (!tracks1.get(i).equals(tracks2.get(i))) {
        return false;
      }
    }

    return true;
  }
}