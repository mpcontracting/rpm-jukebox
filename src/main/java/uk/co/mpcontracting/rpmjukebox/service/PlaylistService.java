package uk.co.mpcontracting.rpmjukebox.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@Service
public class PlaylistService {

  @Getter
  private int currentPlaylistId;

  @Getter
  private Playlist playingPlaylist;

  @Getter
  private boolean shuffle;

  @Getter
  private Repeat repeat;

  @Getter
  private Track selectedTrack;

  public List<Playlist> getPlaylists() {
    return Collections.emptyList();
  }

  public Optional<Playlist> getPlaylist(int playlistId) {
    return Optional.empty();
  }

  public void addPlaylist(Playlist playlist) {

  }

  public void createPlaylist() {

  }

  public void createPlaylistFromAlbum(Track track) {

  }

  public void deletePlaylist(int playlistId) {

  }

  public void setPlaylistTracks(int playlistId, List<Track> tracks) {

  }

  public void addTrackToPlaylist(int playlistId, Track track) {

  }

  public void removeTrackFromPlaylist(int playlistId, Track track) {

  }

  public void moveTracksInPlaylist(int playlistId, Track source, Track target) {

  }

  public boolean isTrackInPlaylist(int playlistId, String trackId) {
    return false;
  }

  public void playPlaylist(int playlistId) {

  }

  public void playTrack(Track track) {

  }

  public void playCurrentTrack(boolean overrideShuffle) {

  }

  public void pauseCurrentTrack() {

  }

  public void resumeCurrentTrack() {

  }

  protected void restartTrack() {

  }

  public boolean playPreviousTrack(boolean overrideRepeatOne) {
    return false;
  }

  public boolean playNextTrack(boolean overrideRepeatOne) {
    return false;
  }

  public Track getTrackAtPlayingPlaylistIndex() {
    return null;
  }

  public void clearSelectedTrack() {

  }

  public void setShuffle(boolean shuffle, boolean ignorePlaylist) {

  }

  public void setRepeat(Repeat repeat) {

  }

  public void updateRepeat() {

  }
}
