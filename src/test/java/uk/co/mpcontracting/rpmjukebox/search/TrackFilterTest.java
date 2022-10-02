package uk.co.mpcontracting.rpmjukebox.search;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
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
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) trackFilter.getFilter();
        BooleanQuery booleanQuery = (BooleanQuery) constantScoreQuery.getQuery();
        //TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        //assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
        assertThat(booleanQuery.clauses().size()).isEqualTo(1);
    }

    @Test
    public void shouldInitialiseWithYear() {
        TrackFilter trackFilter = new TrackFilter(null, "2000");
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) trackFilter.getFilter();
        BooleanQuery booleanQuery = (BooleanQuery) constantScoreQuery.getQuery();
        //TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        //assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
        assertThat(booleanQuery.clauses().size()).isEqualTo(1);
    }

    @Test
    public void shouldInitialiseWithGenreAndYear() {
        TrackFilter trackFilter = new TrackFilter("Genre", "2000");
        ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) trackFilter.getFilter();
        BooleanQuery booleanQuery = (BooleanQuery) constantScoreQuery.getQuery();
        //TermsQuery termsQuery = (TermsQuery) trackFilter.getFilter();

        //assertThat(termsQuery.getTermData().size()).isEqualTo(1L);
        assertThat(booleanQuery.clauses().size()).isEqualTo(2);
    }
}
