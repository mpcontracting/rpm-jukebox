package uk.co.mpcontracting.rpmjukebox.search;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TrackSearch {

    private String keywords;
    private TrackFilter trackFilter;
    private TrackSort trackSort;

    public TrackSearch(String keywords) {
        this(keywords, TrackSort.DEFAULTSORT);
    }

    public TrackSearch(String keywords, TrackFilter trackFilter) {
        this(keywords, trackFilter, TrackSort.DEFAULTSORT);
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
