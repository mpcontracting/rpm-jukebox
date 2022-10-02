package uk.co.mpcontracting.rpmjukebox.search;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

@EqualsAndHashCode
@RequiredArgsConstructor
public class TrackFilter {
    private final String genre;
    private final String year;

    public Query getFilter() {
        boolean hasFilters = false;
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

        if (genre != null && genre.trim().length() > 0) {
            hasFilters = true;
            queryBuilder.add(new TermQuery(new Term(TrackField.GENRE.name(), genre.trim())), BooleanClause.Occur.MUST);
        }

        if (year != null && year.trim().length() > 0) {
            hasFilters = true;
            queryBuilder.add(new TermQuery(new Term(TrackField.YEAR.name(), year.trim())), BooleanClause.Occur.MUST);
        }

        if (hasFilters) {
            return new ConstantScoreQuery(queryBuilder.build());
        }

        return null;
    }
}
