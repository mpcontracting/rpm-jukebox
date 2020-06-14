package uk.co.mpcontracting.rpmjukebox.search;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TrackSearchTest {

    @Test
    public void shouldInitialiseWithKeywords() {
        TrackSearch trackSearch = new TrackSearch("Keywords");
        TrackFilter trackFilter = trackSearch.getTrackFilter();

        assertThat(trackSearch.getKeywords()).isEqualTo("Keywords");
        assertThat(trackSearch.getTrackSort()).isEqualTo(TrackSort.DEFAULT_SORT);
        assertThat(trackFilter.getFilter()).isNull();
    }

    @Test
    public void shouldInitialiseWithKeywordsAndFilter() {
        TrackSearch trackSearch = new TrackSearch("Keywords", new TrackFilter("Genre", "2000"));
        TrackFilter trackFilter = trackSearch.getTrackFilter();

        assertThat(trackSearch.getKeywords()).isEqualTo("Keywords");
        assertThat(trackSearch.getTrackSort()).isEqualTo(TrackSort.DEFAULT_SORT);
        assertThat(trackFilter.getFilter()).isNotNull();
    }

    @Test
    public void shouldInitialiseWithKeywordsAndSort() {
        TrackSearch trackSearch = new TrackSearch("Keywords", TrackSort.ALBUM_SORT);
        TrackFilter trackFilter = trackSearch.getTrackFilter();

        assertThat(trackSearch.getKeywords()).isEqualTo("Keywords");
        assertThat(trackSearch.getTrackSort()).isEqualTo(TrackSort.ALBUM_SORT);
        assertThat(trackFilter.getFilter()).isNull();
    }

    @Test
    public void shouldInitialiseWithKeywordsFilterAndSort() {
        TrackSearch trackSearch = new TrackSearch("Keywords", new TrackFilter("Genre", "2000"), TrackSort.ALBUM_SORT);
        TrackFilter trackFilter = trackSearch.getTrackFilter();

        assertThat(trackSearch.getKeywords()).isEqualTo("Keywords");
        assertThat(trackSearch.getTrackSort()).isEqualTo(TrackSort.ALBUM_SORT);
        assertThat(trackFilter.getFilter()).isNotNull();
    }
}
