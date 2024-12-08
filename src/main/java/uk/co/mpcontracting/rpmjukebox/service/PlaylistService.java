package uk.co.mpcontracting.rpmjukebox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.model.Track;

@Slf4j
@Service
public class PlaylistService {

  public void createPlaylist() {

  }

  public void createPlaylistFromAlbum(Track track) {

  }

  public void deletePlaylist(int playlistId) {

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
}
