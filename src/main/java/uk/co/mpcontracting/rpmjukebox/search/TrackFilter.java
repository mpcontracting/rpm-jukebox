package uk.co.mpcontracting.rpmjukebox.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsQuery;
import org.apache.lucene.search.Query;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@AllArgsConstructor
public class TrackFilter {
    private String genre;
    private String year;

    public Query getFilter() {
        boolean hasFilters = false;
        List<Term> termsList = new ArrayList<>();

        if (genre != null && genre.trim().length() > 0) {
            hasFilters = true;
            termsList.add(new Term(TrackField.GENRE.name(), genre.trim()));
        }

        if (year != null && year.trim().length() > 0) {
            hasFilters = true;
            termsList.add(new Term(TrackField.YEAR.name(), year.trim()));
        }

        if (hasFilters) {
            return new TermsQuery(termsList);
        }

        return null;
    }
}