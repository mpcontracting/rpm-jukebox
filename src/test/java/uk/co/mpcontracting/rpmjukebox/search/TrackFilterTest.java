package uk.co.mpcontracting.rpmjukebox.search;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createYearString;

import org.apache.lucene.queries.TermsQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackFilterTest {

  @Test
  void shouldInitialiseWithNoGenreOrYear() {
    TrackFilter trackFilter = new TrackFilter(null, null);

    assertThat(trackFilter.getFilter()).isNull();
  }

  @Test
  void shouldInitialiseWithBlankGenreOrYear() {
    TrackFilter trackFilter = new TrackFilter("", "");

    assertThat(trackFilter.getFilter()).isNull();
  }

  @Test
  void shouldInitialiseWithGenre() {
    TrackFilter trackFilter = new TrackFilter(createGenre(), null);
    TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

    assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
  }

  @Test
  void shouldInitialiseWithYear() {
    TrackFilter trackFilter = new TrackFilter(null, createYearString());
    TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

    assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
  }

  @Test
  void shouldInitialiseWithGenreAndYear() {
    TrackFilter trackFilter = new TrackFilter(createGenre(), createYearString());
    TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

    assertThat(termsQuery.getTermData().size()).isEqualTo(2L);
  }
}