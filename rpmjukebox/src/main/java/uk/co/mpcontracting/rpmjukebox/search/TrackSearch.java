package uk.co.mpcontracting.rpmjukebox.search;

import lombok.Getter;

public class TrackSearch {
    @Getter private String keywords;
    @Getter private TrackFilter trackFilter;
    @Getter private TrackSort trackSort;
    
    public TrackSearch(String keywords) {
        this(keywords, TrackSort.DEFAULTSORT);
    }
    
    public TrackSearch(String keywords, TrackFilter trackFilter) {
        this(keywords, trackFilter, TrackSort.DEFAULTSORT);
    }
    
    public TrackSearch(String keywords, TrackSort trackSort) {
        this(keywords, new TrackFilter(null, null), trackSort);
    }
    
    public TrackSearch(String keywords, TrackFilter trackFilter, TrackSort trackSort) {
        this.keywords = keywords;
        this.trackFilter = trackFilter;
        this.trackSort = trackSort;
    }
}
