package uk.co.mpcontracting.rpmjukebox.search;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.apache.lucene.queries.TermsQuery;
import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class TrackFilterTest extends AbstractTest {

    @Test
    public void shouldInitialiseWithNoGenreOrYear() {
        TrackFilter trackFilter = new TrackFilter(null, null);

        assertThat("Filter query should be null", trackFilter.getFilter(), nullValue());
    }

    @Test
    public void shouldInitialiseWithBlankGenreOrYear() {
        TrackFilter trackFilter = new TrackFilter("", "");

        assertThat("Filter query should be null", trackFilter.getFilter(), nullValue());
    }

    @Test
    public void shouldInitialiseWithGenre() {
        TrackFilter trackFilter = new TrackFilter("Genre", null);
        TermsQuery termsQuery = (TermsQuery)trackFilter.getFilter();

        assertThat("Filter query should have 1 term", termsQuery.getTermData().size(), equalTo(1l));
    }

    @Test
    public void shouldInitialiseWithYear() {
        TrackFilter trackFilter = new TrackFilter(null, "2000");
        TermsQuery termsQuery = (TermsQuery)trackFilter.getFilter();

        assertThat("Filter query should have 1 term", termsQuery.getTermData().size(), equalTo(1l));
    }

    @Test
    public void shouldInitialiseWithGenreAndYear() {
        TrackFilter trackFilter = new TrackFilter("Genre", "2000");
        TermsQuery termsQuery = (TermsQuery)trackFilter.getFilter();

        assertThat("Filter query should have 2 terms", termsQuery.getTermData().size(), equalTo(2l));
    }
}
