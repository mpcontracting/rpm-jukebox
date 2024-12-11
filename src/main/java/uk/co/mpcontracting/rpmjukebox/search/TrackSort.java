package uk.co.mpcontracting.rpmjukebox.search;

import lombok.Getter;

@Getter
public enum TrackSort {
  DEFAULT_SORT("Default"),
  ARTIST_SORT("Artist"),
  ALBUM_SORT("Album"),
  TRACK_SORT("Track");

  private final String friendlyName;

  TrackSort(String friendlyName) {
    this.friendlyName = friendlyName;
  }
}
