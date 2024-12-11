package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.END_OF_MEDIA;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CONTENT_UPDATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_CREATED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_DELETED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.PLAYLIST_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TRACK_SELECTED;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ALL;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.OFF;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ONE;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createPlaylistName;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getNonNullField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_DEFAULT;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.controller.TrackTableController;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractEventAwareObjectTest;

class PlaylistServiceTest extends AbstractEventAwareObjectTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private StringResourceService stringResourceService;

  @Mock
  private TrackTableController trackTableController;

  @Mock
  private MediaService mediaService;

  @Mock
  private SearchService searchService;

  private PlaylistService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new PlaylistService(applicationProperties, stringResourceService, mediaService, searchService));
    setField(underTest, "trackTableController", trackTableController);

    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_SEARCH, createPlaylistName(), 10),
        new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10)
    );

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    lenient().when(applicationProperties.getMaxPlaylistSize()).thenReturn(50);
    lenient().when(stringResourceService.getString(MESSAGE_PLAYLIST_DEFAULT)).thenReturn(createPlaylistName());
  }

  @Test
  void shouldSetPlaylists() {
    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_FAVOURITES, createPlaylistName(), 10),
        new Playlist(1, createPlaylistName(), 10),
        new Playlist(3, createPlaylistName(), 10)
    );

    underTest.setPlaylists(playlists);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);

    assertThat(playlistMap).hasSize(4);
  }

  @Test
  void shouldGetPlaylists() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    List<Playlist> result = underTest.getPlaylists();

    assertThat(playlistMap).hasSize(4);
    assertThat(result).isInstanceOf(unmodifiableList(new ArrayList<>()).getClass());
  }

  @Test
  void shouldAddPlaylist() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    Playlist playlist = new Playlist(999, "New Playlist", 10);

    underTest.addPlaylist(playlist);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getField(underTest, "playlistMap", Map.class);

    assertThat(newPlaylistMap).hasSize(5);
    assertThat(playlist.getPlaylistId()).isEqualTo(2);
  }

  @Test
  void shouldCreatePlaylist() {
    List<Playlist> playlists = List.of(
        new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10),
        new Playlist(3, "Playlist 3", 10)
    );

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    underTest.createPlaylist();

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = newPlaylistMap.get(2);

    assertThat(newPlaylistMap).hasSize(5);
    assertThat(playlist).isNotNull();
    assertThat(playlist.getPlaylistId()).isEqualTo(2);
    verify(eventProcessor).fireEvent(PLAYLIST_CREATED, 2, true);
  }

  @Test
  void shouldCreatePlaylistFromAlbum() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    Track track = mock(Track.class);
    when(track.getArtistName()).thenReturn("Artist");
    when(track.getAlbumName()).thenReturn("Album");

    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(mock(Track.class));
    }

    when(searchService.getAlbumById(any())).thenReturn(of(tracks));

    underTest.createPlaylistFromAlbum(track);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = newPlaylistMap.get(2);

    assertThat(newPlaylistMap).hasSize(5);
    assertThat(playlist).isNotNull();
    assertThat(playlist.getPlaylistId()).isEqualTo(2);
    assertThat(playlist.getName()).isEqualTo("Artist - Album");
    assertThat(playlist.getTracks()).hasSize(10);
    verify(eventProcessor).fireEvent(PLAYLIST_CREATED, 2, false);
  }

  @Test
  void shouldNotCreatePlaylistFromAlbumWithNullTracks() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    when(searchService.getAlbumById(any())).thenReturn(empty());

    underTest.createPlaylistFromAlbum(mock(Track.class));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = newPlaylistMap.get(2);

    assertThat(newPlaylistMap).hasSize(4);
    assertThat(playlist).isNull();
  }

  @Test
  void shouldNotCreatePlaylistFromAlbumWithEmptyTracks() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    when(searchService.getAlbumById(any())).thenReturn(of(emptyList()));

    underTest.createPlaylistFromAlbum(mock(Track.class));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = newPlaylistMap.get(2);

    assertThat(newPlaylistMap).hasSize(4);
    assertThat(playlist).isNull();
  }

  @Test
  void shouldGetPlaylist() {
    Playlist playlist = underTest.getPlaylist(PLAYLIST_ID_SEARCH).orElse(null);

    assertThat(playlist).isNotNull();
    assertThat(playlist.getPlaylistId()).isEqualTo(PLAYLIST_ID_SEARCH);
  }

  @Test
  void shouldDeletePlaylist() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10), new Playlist(4, "Playlist 4", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    underTest.deletePlaylist(3);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);

    assertThat(newPlaylistMap).hasSize(4);
    verify(eventProcessor).fireEvent(PLAYLIST_DELETED, 1);
  }

  @Test
  void shouldNotDeleteAReservedPlaylist() {
    List<Playlist> playlists = List.of(new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10),
        new Playlist(1, "Playlist 1", 10), new Playlist(3, "Playlist 3", 10));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    playlists.forEach(playlist -> playlistMap.put(playlist.getPlaylistId(), playlist));

    setField(underTest, "playlistMap", playlistMap);

    underTest.deletePlaylist(PLAYLIST_ID_FAVOURITES);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);

    assertThat(newPlaylistMap).hasSize(4);
    verify(eventProcessor, never()).fireEvent(PLAYLIST_DELETED, 1);
  }

  @Test
  void shouldSetPlaylistTracks() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(mock(Track.class));
    }

    underTest.setPlaylistTracks(PLAYLIST_ID_FAVOURITES, tracks);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

    assertThat(playlist.getTracks()).hasSize(10);
    verify(eventProcessor).fireEvent(PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldAddTrackToPlaylist() {
    underTest.addTrackToPlaylist(PLAYLIST_ID_FAVOURITES, mock(Track.class));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);

    assertThat(playlist.getTracks()).hasSize(1);
    verify(eventProcessor).fireEvent(PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldRemoveTrackFromPlaylist() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(tracks);

    setField(underTest, "playlistMap", playlistMap);

    underTest.removeTrackFromPlaylist(PLAYLIST_ID_FAVOURITES, tracks.get(5));

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist newPlaylist = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES);

    assertThat(newPlaylist.getTracks()).hasSize(9);
    verify(eventProcessor).fireEvent(PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldMoveTracksInPlaylist() {
    Track track1 = createTrack(1);
    Track track2 = createTrack(2);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(List.of(track1, track2));

    setField(underTest, "playlistMap", playlistMap);

    underTest.moveTracksInPlaylist(PLAYLIST_ID_FAVOURITES, track1, track2);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> newPlaylistMap = getNonNullField(underTest, "playlistMap", Map.class);
    List<Track> tracks = newPlaylistMap.get(PLAYLIST_ID_FAVOURITES).getTracks();

    assertThat(tracks).hasSize(2);
    assertThat(tracks.get(0).getTrackId()).isEqualTo(track2.getTrackId());
    assertThat(tracks.get(1).getTrackId()).isEqualTo(track1.getTrackId());
    verify(eventProcessor).fireEvent(PLAYLIST_CONTENT_UPDATED, PLAYLIST_ID_FAVOURITES, track1);
  }

  @Test
  void shouldReturnIfTrackIsInPlaylist() {
    Track track = createTrack(1);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(singletonList(track));

    setField(underTest, "playlistMap", playlistMap);

    boolean result = underTest.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, track.getTrackId());

    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalseIfTrackIsInPlaylistWithNullTrackId() {
    Track track = createTrack(1);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(singletonList(track));

    setField(underTest, "playlistMap", playlistMap);

    boolean result = underTest.isTrackInPlaylist(PLAYLIST_ID_FAVOURITES, null);

    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalseIfTrackIsInPlaylistWithUnknownPlaylistId() {
    Track track = createTrack(1);

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(singletonList(track));

    setField(underTest, "playlistMap", playlistMap);

    boolean result = underTest.isTrackInPlaylist(999, "1");

    assertThat(result).isFalse();
  }

  @Test
  void shouldPlayPlaylist() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(mock(Track.class));
    }

    @SuppressWarnings("unchecked")
    Map<Integer, Playlist> playlistMap = getNonNullField(underTest, "playlistMap", Map.class);
    Playlist playlist = playlistMap.get(PLAYLIST_ID_FAVOURITES);
    playlist.setTracks(tracks);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistIndex", 10);
    setField(underTest, "playingPlaylist", null);

    underTest.playPlaylist(PLAYLIST_ID_FAVOURITES);

    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);
    Playlist playingPlaylist = underTest.getPlayingPlaylist();

    assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentPlaylistIndex).isEqualTo(0);
    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    verify(underTest).playCurrentTrack(anyBoolean());
    verify(eventProcessor).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldPlayTrack() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Track track = mock(Track.class);
    when(track.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);
    when(track.getPlaylistIndex()).thenReturn(10);

    underTest.playTrack(track);

    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);
    Playlist playingPlaylist = underTest.getPlayingPlaylist();

    assertThat(currentPlaylistId).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentPlaylistIndex).isEqualTo(10);
    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    verify(underTest).playCurrentTrack(anyBoolean());
    verify(eventProcessor, never()).fireEvent(PLAYLIST_SELECTED, PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldPlayCurrentTrackNoShuffleNoOverride() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    originalPlaylist.setTracks(tracks);

    Playlist playlist = spy(originalPlaylist);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", false);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(false);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNotNull();
    assertThat(currentTrack.getTrackId()).isEqualTo(tracks.get(currentPlaylistIndex).getTrackId());
    verify(playingPlaylist).createClone();
    verify(playingPlaylist).getTrackAtIndex(currentPlaylistIndex);
    verify(mediaService).playTrack(currentTrack);
  }

  @Test
  void shouldPlayCurrentTrackShuffleNoOverride() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    originalPlaylist.setTracks(tracks);

    Playlist playlist = spy(originalPlaylist);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", true);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(false);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNotNull();
    verify(playingPlaylist).createClone();
    verify(playingPlaylist).getShuffledTrackAtIndex(currentPlaylistIndex);
    verify(mediaService).playTrack(currentTrack);
  }

  @Test
  void shouldPlayCurrentTrackNoShuffleOverride() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    originalPlaylist.setTracks(tracks);

    Playlist playlist = spy(originalPlaylist);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", false);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(true);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNotNull();
    assertThat(currentTrack.getTrackId()).isEqualTo(tracks.get(currentPlaylistIndex).getTrackId());
    verify(playingPlaylist).createClone();
    verify(playingPlaylist).getTrackAtIndex(currentPlaylistIndex);
    verify(mediaService).playTrack(currentTrack);
  }

  @Test
  void shouldPlayCurrentTrackShuffleOverride() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    originalPlaylist.setTracks(tracks);

    Playlist playlist = spy(originalPlaylist);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", true);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(true);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNotNull();
    assertThat(currentTrack.getTrackId()).isEqualTo(tracks.get(currentPlaylistIndex).getTrackId());
    verify(playingPlaylist).createClone();
    verify(playingPlaylist).getTrackAtIndex(currentPlaylistIndex);
    verify(playingPlaylist).setTrackAtShuffledIndex(currentTrack, currentPlaylistIndex);
    verify(mediaService).playTrack(currentTrack);
  }

  @Test
  void shouldPlayCurrentTrackNoShuffleNoOverrideExistingPlaylist() {
    List<Track> tracks = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      tracks.add(createTrack(i));
    }

    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    originalPlaylist.setTracks(tracks);

    Playlist playlist = spy(originalPlaylist);
    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", false);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", playlist);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(false);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNotNull();
    assertThat(currentTrack.getTrackId()).isEqualTo(tracks.get(currentPlaylistIndex).getTrackId());
    verify(playingPlaylist, never()).createClone();
    verify(playingPlaylist).getTrackAtIndex(currentPlaylistIndex);
    verify(mediaService).playTrack(currentTrack);
  }

  @Test
  void shouldNotPlayCurrentTrackNoShuffleNoOverrideEmptyPlaylist() {
    Playlist originalPlaylist = new Playlist(PLAYLIST_ID_FAVOURITES, "Favourites", 10);
    Playlist playlist = spy(originalPlaylist);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);

    int currentPlaylistIndex = 5;

    setField(underTest, "shuffle", false);
    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", currentPlaylistIndex);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentTrack", null);

    underTest.playCurrentTrack(false);

    Playlist playingPlaylist = underTest.getPlayingPlaylist();
    Track currentTrack = getField(underTest, "currentTrack", Track.class);

    assertThat(playingPlaylist).isNotNull();
    assertThat(playingPlaylist.getPlaylistId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    assertThat(currentTrack).isNull();
    verify(playingPlaylist).createClone();
    verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
    verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
    verify(playingPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
    verify(mediaService, never()).playTrack(any());
  }

  @Test
  void shouldPauseCurrentTrack() {
    underTest.pauseCurrentTrack();

    verify(mediaService).pausePlayback();
  }

  @Test
  void shouldResumeCurrentTrack() {
    Track track = createTrack(1);
    track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
    when(trackTableController.getSelectedTrack()).thenReturn(track);

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

    setField(underTest, "playingPlaylist", playingPlaylist);

    underTest.resumeCurrentTrack();

    verify(underTest, never()).playTrack(any());
    verify(mediaService).resumePlayback();
  }

  @Test
  void shouldResumeCurrentTrackWithoutSelectedTrack() {
    Track track = createTrack(1);
    track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
    when(trackTableController.getSelectedTrack()).thenReturn(null);

    setField(underTest, "playingPlaylist", mock(Playlist.class));

    underTest.resumeCurrentTrack();

    verify(underTest, never()).playTrack(any());
    verify(mediaService).resumePlayback();
  }

  @Test
  void shouldResumeCurrentTrackWithDifferentCurrentTrack() {
    doNothing().when(underTest).playTrack(any());

    Track track = createTrack(1);
    track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
    when(trackTableController.getSelectedTrack()).thenReturn(track);

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentTrack", createTrack(2));

    underTest.resumeCurrentTrack();

    verify(underTest).playTrack(track);
    verify(mediaService, never()).resumePlayback();
  }

  @Test
  void shouldResumeCurrentTrackWithDifferentPlaylist() {
    doNothing().when(underTest).playTrack(any());

    Track track = createTrack(1);
    track.setPlaylistId(PLAYLIST_ID_FAVOURITES);
    when(trackTableController.getSelectedTrack()).thenReturn(track);

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentTrack", createTrack(1));

    underTest.resumeCurrentTrack();

    verify(underTest).playTrack(track);
    verify(mediaService, never()).resumePlayback();
  }

  @Test
  void shouldRestartCurrentTrack() {
    underTest.restartTrack();

    verify(mediaService).setSeekPositionPercent(0d);
  }

  @Test
  void shouldPlayPreviousTrackWithNullPlaylist() {
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playPreviousTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isFalse();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
    setField(underTest, "playingPlaylist", mock(Playlist.class));
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", ONE);

    boolean result = underTest.playPreviousTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(2);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "repeat", ONE);

    boolean result = underTest.playPreviousTrack(true);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(4);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "repeat", ALL);

    boolean result = underTest.playPreviousTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(4);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayPreviousTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    setField(underTest, "playingPlaylist", mock(Playlist.class));
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playPreviousTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(1);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayPreviousTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
    setField(underTest, "playingPlaylist", mock(Playlist.class));
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playPreviousTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isFalse();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithNullPlaylist() {
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playNextTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isFalse();
    assertThat(currentPlaylistIndex).isEqualTo(2);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOneNoOverrideRepeatOne() {
    setField(underTest, "playingPlaylist", mock(Playlist.class));
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", ONE);

    boolean result = underTest.playNextTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(2);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOneOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 4);
    setField(underTest, "repeat", ONE);

    boolean result = underTest.playNextTrack(true);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatAllNoOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 4);
    setField(underTest, "repeat", ALL);

    boolean result = underTest.playNextTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
    doNothing().when(underTest).playCurrentTrack(anyBoolean());

    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 2);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playNextTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isTrue();
    assertThat(currentPlaylistIndex).isEqualTo(3);
    verify(underTest).playCurrentTrack(false);
    verify(mediaService, never()).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldPlayNextTrackWithNoTracksLeftInPlaylistRepeatOffNoOverrideRepeatOne() {
    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.size()).thenReturn(5);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "currentPlaylistIndex", 4);
    setField(underTest, "repeat", OFF);

    boolean result = underTest.playNextTrack(false);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(result).isFalse();
    assertThat(currentPlaylistIndex).isEqualTo(4);
    verify(underTest, never()).playCurrentTrack(false);
    verify(mediaService).stopPlayback();
    verify(mediaService, never()).setSeekPositionPercent(anyDouble());
  }

  @Test
  void shouldGetTrackAtCurrentPlayingPlaylistIndexNoShuffle() {
    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.isEmpty()).thenReturn(false);
    when(playingPlaylist.getTrackAtIndex(anyInt())).thenReturn(mock(Track.class));

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "shuffle", false);

    Track track = underTest.getTrackAtPlayingPlaylistIndex();

    assertThat(track).isNotNull();
    verify(playingPlaylist).getTrackAtIndex(anyInt());
    verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
  }

  @Test
  void shouldGetTrackAtCurrentPlayingPlaylistIndexWithShuffle() {
    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.isEmpty()).thenReturn(false);
    when(playingPlaylist.getShuffledTrackAtIndex(anyInt())).thenReturn(mock(Track.class));

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "shuffle", true);

    Track track = underTest.getTrackAtPlayingPlaylistIndex();

    assertThat(track).isNotNull();
    verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
    verify(playingPlaylist).getShuffledTrackAtIndex(anyInt());
  }

  @Test
  void shouldGetTrackAtCurrentPlayingPlaylistWithEmptyPlaylist() {
    Playlist playingPlaylist = mock(Playlist.class);
    when(playingPlaylist.isEmpty()).thenReturn(true);

    setField(underTest, "playingPlaylist", playingPlaylist);
    setField(underTest, "shuffle", false);

    Track track = underTest.getTrackAtPlayingPlaylistIndex();

    assertThat(track).isNull();
    verify(playingPlaylist, never()).getTrackAtIndex(anyInt());
    verify(playingPlaylist, never()).getShuffledTrackAtIndex(anyInt());
  }

  @Test
  void shouldGetTrackAtCurrentPlayingPlaylistWithNullPlaylist() {
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", false);

    Track track = underTest.getTrackAtPlayingPlaylistIndex();

    assertThat(track).isNull();
  }

  @Test
  void shouldClearSelectedTrack() {
    setField(underTest, "selectedTrack", mock(Track.class));

    underTest.clearSelectedTrack();

    Track track = getField(underTest, "selectedTrack", Track.class);

    assertThat(track).isNull();
  }

  @Test
  void shouldSetShuffleNoIgnorePlaylistNoCurrentTrack() {
    Playlist mockPlaylist = mock(Playlist.class);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, mockPlaylist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentTrack", null);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", false);

    underTest.setShuffle(true, false);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);

    assertThat(shuffle).isTrue();
    assertThat(playingPlaylist).isNull();
    verify(mockPlaylist).shuffle();
    verify(mockPlaylist, never()).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
    when(mediaService.isPaused()).thenReturn(true);
    when(mediaService.isPlaying()).thenReturn(false);

    Track track = mock(Track.class);

    Playlist playlist = mock(Playlist.class);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentTrack", track);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", false);

    underTest.setShuffle(true, false);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);

    assertThat(shuffle).isTrue();
    assertThat(playingPlaylist).isNotNull();
    verify(playlist).shuffle();
    verify(playlist).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
    when(mediaService.isPlaying()).thenReturn(true);

    Track track = mock(Track.class);

    Playlist playlist = mock(Playlist.class);
    when(playlist.createClone()).thenReturn(playlist);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentTrack", track);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", false);

    underTest.setShuffle(true, false);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);

    assertThat(shuffle).isTrue();
    assertThat(playingPlaylist).isNotNull();
    verify(playlist).shuffle();
    verify(playlist).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPaused() {
    when(mediaService.isPaused()).thenReturn(true);
    when(mediaService.isPlaying()).thenReturn(false);

    Track track = mock(Track.class);
    when(track.getPlaylistIndex()).thenReturn(5);

    Playlist playlist = mock(Playlist.class);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "currentTrack", track);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", true);

    underTest.setShuffle(false, false);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(shuffle).isFalse();
    assertThat(playingPlaylist).isNull();
    assertThat(currentPlaylistIndex).isEqualTo(5);
    verify(playlist, never()).shuffle();
    verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetNoShuffleNoIgnorePlaylistWithCurrentTrackAndIsPlaying() {
    when(mediaService.isPlaying()).thenReturn(true);

    Track track = mock(Track.class);
    when(track.getPlaylistIndex()).thenReturn(5);

    Playlist playlist = mock(Playlist.class);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "currentTrack", track);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", true);

    underTest.setShuffle(false, false);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(shuffle).isFalse();
    assertThat(playingPlaylist).isNull();
    assertThat(currentPlaylistIndex).isEqualTo(5);
    verify(playlist, never()).shuffle();
    verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetShuffleIgnorePlaylist() {
    Playlist playlist = mock(Playlist.class);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", false);

    underTest.setShuffle(true, true);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(shuffle).isTrue();
    assertThat(playingPlaylist).isNull();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(playlist, never()).shuffle();
    verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetNoShuffleIgnorePlaylist() {
    Playlist playlist = mock(Playlist.class);

    Map<Integer, Playlist> playlistMap = new LinkedHashMap<>();
    playlistMap.put(PLAYLIST_ID_FAVOURITES, playlist);

    setField(underTest, "playlistMap", playlistMap);
    setField(underTest, "currentPlaylistId", PLAYLIST_ID_FAVOURITES);
    setField(underTest, "currentPlaylistIndex", 0);
    setField(underTest, "playingPlaylist", null);
    setField(underTest, "shuffle", true);

    underTest.setShuffle(false, true);

    boolean shuffle = getNonNullField(underTest, "shuffle", Boolean.class);
    Playlist playingPlaylist = getField(underTest, "playingPlaylist", Playlist.class);
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(shuffle).isFalse();
    assertThat(playingPlaylist).isNull();
    assertThat(currentPlaylistIndex).isEqualTo(0);
    verify(playlist, never()).shuffle();
    verify(playlist, never()).setTrackAtShuffledIndex(any(), anyInt());
  }

  @Test
  void shouldSetRepeat() {
    setField(underTest, "repeat", OFF);

    underTest.setRepeat(ALL);

    Repeat repeat = getField(underTest, "repeat", Repeat.class);

    assertThat(repeat).isEqualTo(ALL);
  }

  @Test
  void shouldUpdateRepeatFromOff() {
    setField(underTest, "repeat", OFF);

    underTest.updateRepeat();

    Repeat repeat = getField(underTest, "repeat", Repeat.class);

    assertThat(repeat).isEqualTo(ALL);
  }

  @Test
  void shouldUpdateRepeatFromAll() {
    setField(underTest, "repeat", ALL);

    underTest.updateRepeat();

    Repeat repeat = getField(underTest, "repeat", Repeat.class);

    assertThat(repeat).isEqualTo(ONE);
  }

  @Test
  void shouldUpdateRepeatFromOne() {
    setField(underTest, "repeat", ONE);

    underTest.updateRepeat();

    Repeat repeat = getField(underTest, "repeat", Repeat.class);

    assertThat(repeat).isEqualTo(OFF);
  }

  @Test
  void shouldSelectTrackOnTrackSelectedEventWhenMediaPlaying() {
    when(mediaService.isPlaying()).thenReturn(true);

    setField(underTest, "selectedTrack", null);
    setField(underTest, "currentPlaylistId", 0);
    setField(underTest, "currentPlaylistIndex", 0);

    Track track = mock(Track.class);
    when(track.getTrackId()).thenReturn("123");

    underTest.eventReceived(TRACK_SELECTED, track);

    Track selectedTrack = underTest.getSelectedTrack();
    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(selectedTrack).isNotNull();
    assertThat(selectedTrack.getTrackId()).isEqualTo("123");
    assertThat(currentPlaylistId).isEqualTo(0);
    assertThat(currentPlaylistIndex).isEqualTo(0);
  }

  @Test
  void shouldSelectTrackOnTrackSelectedEventWhenNoMediaPlaying() {
    when(mediaService.isPlaying()).thenReturn(false);

    setField(underTest, "selectedTrack", null);
    setField(underTest, "currentPlaylistId", 0);
    setField(underTest, "currentPlaylistIndex", 0);

    Track track = mock(Track.class);
    when(track.getTrackId()).thenReturn("123");
    when(track.getPlaylistId()).thenReturn(456);
    when(track.getPlaylistIndex()).thenReturn(5);

    underTest.eventReceived(TRACK_SELECTED, track);

    Track selectedTrack = underTest.getSelectedTrack();
    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(selectedTrack).isNotNull();
    assertThat(selectedTrack.getTrackId()).isEqualTo("123");
    assertThat(currentPlaylistId).isEqualTo(456);
    assertThat(currentPlaylistIndex).isEqualTo(5);
  }

  @Test
  void shouldNotSelectTrackOnTrackSelectedEventIfPayloadTrackIsNull() {
    setField(underTest, "selectedTrack", null);
    setField(underTest, "currentPlaylistId", 0);
    setField(underTest, "currentPlaylistIndex", 0);

    underTest.eventReceived(TRACK_SELECTED, (Object[]) null);

    Track selectedTrack = underTest.getSelectedTrack();
    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(selectedTrack).isNull();
    assertThat(currentPlaylistId).isEqualTo(0);
    assertThat(currentPlaylistIndex).isEqualTo(0);
  }

  @Test
  void shouldNotSelectTrackOnTrackSelectedEventIfPayloadArrayIsEmpty() {
    setField(underTest, "selectedTrack", null);
    setField(underTest, "currentPlaylistId", 0);
    setField(underTest, "currentPlaylistIndex", 0);

    underTest.eventReceived(TRACK_SELECTED);

    Track selectedTrack = underTest.getSelectedTrack();
    int currentPlaylistId = underTest.getCurrentPlaylistId();
    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(selectedTrack).isNull();
    assertThat(currentPlaylistId).isEqualTo(0);
    assertThat(currentPlaylistIndex).isEqualTo(0);
  }

  @Test
  void shouldPlayNextTrackOnEndOfMediaEvent() {
    doReturn(true).when(underTest).playNextTrack(false);
    setField(underTest, "currentPlaylistIndex", 5);

    underTest.eventReceived(END_OF_MEDIA, (Object[]) null);

    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(currentPlaylistIndex).isEqualTo(5);
  }

  @Test
  void shouldNotPlayNextTrackOnEndOfMediaEventIfNoTracksLeftInPlaylist() {
    doReturn(false).when(underTest).playNextTrack(false);
    setField(underTest, "currentPlaylistIndex", 5);

    underTest.eventReceived(END_OF_MEDIA, (Object[]) null);

    int currentPlaylistIndex = getNonNullField(underTest, "currentPlaylistIndex", Integer.class);

    assertThat(currentPlaylistIndex).isEqualTo(0);
  }
}