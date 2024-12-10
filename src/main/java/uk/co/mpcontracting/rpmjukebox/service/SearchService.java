package uk.co.mpcontracting.rpmjukebox.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;

@Slf4j
@Service
public class SearchService {

  @Getter
  private List<String> yearList;

  public void initialise() throws Exception {

  }

  void shutdown() {

  }

  @Synchronized
  public void indexData() throws Exception {

  }

  void addTrack(Track track) {

  }

  @Synchronized
  public List<Track> search(TrackSearch trackSearch) {
    return Collections.emptyList();
  }

  @Synchronized
  public List<Track> getShuffledPlaylist(int playlistSize, String yearFilter) {
    return Collections.emptyList();
  }

  @Synchronized
  public Optional<Track> getTrackById(String trackId) {
    return Optional.empty();
  }

  @Synchronized
  Optional<List<Track>> getAlbumById(String albumId) {
    return Optional.empty();
  }
}
