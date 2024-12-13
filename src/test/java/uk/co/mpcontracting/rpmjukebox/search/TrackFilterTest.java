package uk.co.mpcontracting.rpmjukebox.search;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.search.TrackField.GENRE;
import static uk.co.mpcontracting.rpmjukebox.search.TrackField.YEAR;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createYearString;

import java.util.List;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackFilterTest {

  @Test
  void shouldInitialiseWithNoGenreOrYear() {
    TrackFilter trackFilter = new TrackFilter(null, null);

    assertThat(trackFilter.getTermQueries()).isEmpty();
  }

  @Test
  void shouldInitialiseWithBlankGenreOrYear() {
    TrackFilter trackFilter = new TrackFilter("", "");

    assertThat(trackFilter.getTermQueries()).isEmpty();
  }

  @Test
  void shouldInitialiseWithGenre() {
    TrackFilter trackFilter = new TrackFilter(createGenre(), null);
    List<TermQuery> termQueries = trackFilter.getTermQueries();

    assertThat(termQueries.size()).isEqualTo(1);
    assertThat(termQueries.getFirst().getTerm().field()).isEqualTo(GENRE.name());
  }

  @Test
  void shouldInitialiseWithYear() {
    TrackFilter trackFilter = new TrackFilter(null, createYearString());
    List<TermQuery> termQueries = trackFilter.getTermQueries();

    assertThat(termQueries.size()).isEqualTo(1);
    assertThat(termQueries.getFirst().getTerm().field()).isEqualTo(YEAR.name());
  }

  @Test
  void shouldInitialiseWithGenreAndYear() {
    TrackFilter trackFilter = new TrackFilter(createGenre(), createYearString());
    List<TermQuery> termQueries = trackFilter.getTermQueries();

    assertThat(termQueries.size()).isEqualTo(2);
    assertThat(termQueries.getFirst().getTerm().field()).isEqualTo(GENRE.name());
    assertThat(termQueries.get(1).getTerm().field()).isEqualTo(YEAR.name());
  }
}