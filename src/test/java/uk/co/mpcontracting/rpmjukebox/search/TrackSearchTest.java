package uk.co.mpcontracting.rpmjukebox.search;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.search.TrackSort.ALBUM_SORT;
import static uk.co.mpcontracting.rpmjukebox.search.TrackSort.DEFAULT_SORT;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createKeywords;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createYearString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackSearchTest {

  @Test
  void shouldInitialiseWithKeywords() {
    String keywords = createKeywords();
    TrackSearch trackSearch = new TrackSearch(keywords);
    TrackFilter trackFilter = trackSearch.getTrackFilter();

    assertThat(trackSearch.getKeywords()).isEqualTo(keywords);
    assertThat(trackSearch.getTrackSort()).isEqualTo(DEFAULT_SORT);
    assertThat(trackFilter.getTermQueries()).isEmpty();
  }

  @Test
  void shouldInitialiseWithKeywordsAndFilter() {
    String keywords = createKeywords();
    TrackSearch trackSearch = new TrackSearch(keywords, new TrackFilter(createGenre(), createYearString()));
    TrackFilter trackFilter = trackSearch.getTrackFilter();

    assertThat(trackSearch.getKeywords()).isEqualTo(keywords);
    assertThat(trackSearch.getTrackSort()).isEqualTo(DEFAULT_SORT);
    assertThat(trackFilter.getTermQueries()).isNotEmpty();
  }

  @Test
  void shouldInitialiseWithKeywordsAndSort() {
    String keywords = createKeywords();
    TrackSearch trackSearch = new TrackSearch(keywords, ALBUM_SORT);
    TrackFilter trackFilter = trackSearch.getTrackFilter();

    assertThat(trackSearch.getKeywords()).isEqualTo(keywords);
    assertThat(trackSearch.getTrackSort()).isEqualTo(ALBUM_SORT);
    assertThat(trackFilter.getTermQueries()).isEmpty();
  }

  @Test
  void shouldInitialiseWithKeywordsFilterAndSort() {
    String keywords = createKeywords();
    TrackSearch trackSearch = new TrackSearch(keywords, new TrackFilter(createGenre(), createYearString()), ALBUM_SORT);
    TrackFilter trackFilter = trackSearch.getTrackFilter();

    assertThat(trackSearch.getKeywords()).isEqualTo(keywords);
    assertThat(trackSearch.getTrackSort()).isEqualTo(ALBUM_SORT);
    assertThat(trackFilter.getTermQueries()).isNotEmpty();
  }
}