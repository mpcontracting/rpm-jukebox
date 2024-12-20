package uk.co.mpcontracting.rpmjukebox.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TrackSearch {

  private final String keywords;
  private final TrackFilter trackFilter;
  private final TrackSort trackSort;

  public TrackSearch(String keywords) {
    this(keywords, TrackSort.DEFAULT_SORT);
  }

  public TrackSearch(String keywords, TrackFilter trackFilter) {
    this(keywords, trackFilter, TrackSort.DEFAULT_SORT);
  }

  TrackSearch(String keywords, TrackSort trackSort) {
    this(keywords, new TrackFilter(null, null), trackSort);
  }

  TrackSearch(String keywords, TrackFilter trackFilter, TrackSort trackSort) {
    this.keywords = keywords;
    this.trackFilter = trackFilter;
    this.trackSort = trackSort;
  }
}
