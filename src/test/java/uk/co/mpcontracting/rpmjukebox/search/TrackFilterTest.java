package uk.co.mpcontracting.rpmjukebox.search;

import org.apache.lucene.queries.TermsQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TrackFilterTest {

    @Test
    public void shouldInitialiseWithNoGenreOrYear() {
        TrackFilter trackFilter = new TrackFilter(null, null);

        assertThat(trackFilter.getFilter()).isNull();
    }

    @Test
    public void shouldInitialiseWithBlankGenreOrYear() {
        TrackFilter trackFilter = new TrackFilter("", "");

        assertThat(trackFilter.getFilter()).isNull();
    }

    @Test
    public void shouldInitialiseWithGenre() {
        TrackFilter trackFilter = new TrackFilter("Genre", null);
        TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
    }

    @Test
    public void shouldInitialiseWithYear() {
        TrackFilter trackFilter = new TrackFilter(null, "2000");
        TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
    }

    @Test
    public void shouldInitialiseWithGenreAndYear() {
        TrackFilter trackFilter = new TrackFilter("Genre", "2000");
        TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        assertThat(termsQuery.getTermData().size()).isEqualTo(2L);
    }
}
