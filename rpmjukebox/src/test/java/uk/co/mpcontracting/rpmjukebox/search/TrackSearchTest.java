package uk.co.mpcontracting.rpmjukebox.search;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackSearchTest extends AbstractTest {

    @Test
    public void shouldInitialiseWithKeywords() {
        TrackSearch trackSearch = new TrackSearch("Keywords");
        TrackFilter trackFilter = trackSearch.getTrackFilter();
        
        assertThat("Keywords should be 'Keywords'", trackSearch.getKeywords(), equalTo("Keywords"));
        assertThat("Sort should be 'DEFAULTSORT'", trackSearch.getTrackSort(), equalTo(TrackSort.DEFAULTSORT));
        assertThat("Filter query should be null", trackFilter.getFilter(), nullValue());
    }
    
    @Test
    public void shouldInitialiseWithKeywordsAndFilter() {
        TrackSearch trackSearch = new TrackSearch("Keywords", new TrackFilter("Genre", "2000"));
        TrackFilter trackFilter = trackSearch.getTrackFilter();
        
        assertThat("Keywords should be 'Keywords'", trackSearch.getKeywords(), equalTo("Keywords"));
        assertThat("Sort should be 'DEFAULTSORT'", trackSearch.getTrackSort(), equalTo(TrackSort.DEFAULTSORT));
        assertThat("Filter query should not be null", trackFilter.getFilter(), notNullValue());
    }
    
    @Test
    public void shouldInitialiseWithKeywordsAndSort() {
        TrackSearch trackSearch = new TrackSearch("Keywords", TrackSort.ALBUMSORT);
        TrackFilter trackFilter = trackSearch.getTrackFilter();
        
        assertThat("Keywords should be 'Keywords'", trackSearch.getKeywords(), equalTo("Keywords"));
        assertThat("Sort should be 'ALBUMSORT'", trackSearch.getTrackSort(), equalTo(TrackSort.ALBUMSORT));
        assertThat("Filter query should be null", trackFilter.getFilter(), nullValue());
    }
    
    @Test
    public void shouldInitialiseWithKeywordsFilterAndSort() {
        TrackSearch trackSearch = new TrackSearch("Keywords", new TrackFilter("Genre", "2000"), TrackSort.ALBUMSORT);
        TrackFilter trackFilter = trackSearch.getTrackFilter();
        
        assertThat("Keywords should be 'Keywords'", trackSearch.getKeywords(), equalTo("Keywords"));
        assertThat("Sort should be 'ALBUMSORT'", trackSearch.getTrackSort(), equalTo(TrackSort.ALBUMSORT));
        assertThat("Filter query should not be null", trackFilter.getFilter(), notNullValue());
    }
}
