package uk.co.mpcontracting.rpmjukebox.search;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

@EqualsAndHashCode
@RequiredArgsConstructor
public class TrackFilter {
  private final String genre;
  private final String year;

  public List<TermQuery> getTermQueries() {
    //boolean hasFilters = false;
    List<TermQuery> termQueries = new ArrayList<>();

    if (nonNull(genre) && !genre.trim().isEmpty()) {
      //hasFilters = true;
      termQueries.add(new TermQuery(new Term(TrackField.GENRE.name(), genre.trim())));
    }

    if (nonNull(year) && !year.trim().isEmpty()) {
      //hasFilters = true;
      termQueries.add(new TermQuery(new Term(TrackField.YEAR.name(), year.trim())));
    }


//    if (hasFilters) {
//      return new TermInSetQuery(termsList);
//    }

    return termQueries;
  }
}
