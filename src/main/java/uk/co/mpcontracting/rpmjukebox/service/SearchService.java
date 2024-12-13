package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.stripAccents;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static uk.co.mpcontracting.rpmjukebox.event.Event.DATA_INDEXED;
import static uk.co.mpcontracting.rpmjukebox.search.TrackField.GENRE;
import static uk.co.mpcontracting.rpmjukebox.search.TrackField.YEAR;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_ALREADY_RUNNING;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_DOWNLOAD_INDEX;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_INITIALISING_SEARCH;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.UNSPECIFIED_GENRE;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService extends EventAwareObject {

  private final RpmJukebox rpmJukebox;
  private final ApplicationProperties applicationProperties;
  private final StringResourceService stringResourceService;
  private final SettingsService settingsService;

  @Lazy
  @Autowired
  private ApplicationLifecycleService applicationLifecycleService;

  @Lazy
  @Autowired
  private DataService dataService;

  @Getter
  private List<String> genreList;
  @Getter
  private List<String> yearList;
  @Getter
  private List<TrackSort> trackSortList;

  private Directory trackDirectory;
  private IndexWriter trackWriter;
  private SearcherManager trackManager;

  private SecureRandom secureRandom;
  private ExecutorService executorService;

  public void initialise() throws Exception {
    log.info("Initialising {}", getClass().getSimpleName());

    try {
      // Initialise the executor service
      executorService = Executors.newSingleThreadExecutor();

      // Initialise the indexes
      Analyzer analyzer = new WhitespaceAnalyzer();
      IndexSearcher.setMaxClauseCount(Integer.MAX_VALUE);

      try {
        trackDirectory = FSDirectory.open(settingsService.getFileFromConfigDirectory(applicationProperties.getTrackIndexDirectory()).toPath());
      } catch (Throwable e) {
        log.error("Error", e);
      }
      IndexWriterConfig trackWriterConfig = new IndexWriterConfig(analyzer);
      trackWriterConfig.setOpenMode(CREATE_OR_APPEND);
      trackWriter = new IndexWriter(trackDirectory, trackWriterConfig);
      trackManager = new SearcherManager(trackWriter, null);

      secureRandom = new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes());

      // See if we already have valid indexes, if not, build them
      if (settingsService.hasDataFileExpired() || settingsService.isNewVersion() || !isIndexValid(trackManager)) {
        indexData();
      }

      rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_INITIALISING_SEARCH));

      // Initialise the filters and sorts
      genreList = new ArrayList<>();
      genreList.add(UNSPECIFIED_GENRE);
      genreList.addAll(getDistinctTrackFieldValues(GENRE));
      Collections.sort(genreList);

      yearList = getDistinctTrackFieldValues(YEAR);
      Collections.sort(yearList);

      trackSortList = List.of(TrackSort.values());

      // Warm up the search
      String searchWarmer = "test song";

      for (int i = 0; i < searchWarmer.length(); i++) {
        search(new TrackSearch(searchWarmer.substring(0, i + 1)));
      }

      log.debug("{} initialised", getClass().getSimpleName());
    } catch (LockObtainFailedException e) {
      log.error("{} already initialised", getClass().getSimpleName(), e);
      rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_ALREADY_RUNNING));

      try {
        Thread.sleep(5000);
      } catch (Exception e2) {
        // Do nothing
      }

      applicationLifecycleService.shutdown();
    } catch (Exception e) {
      log.error("Error initialising {}", getClass().getSimpleName(), e);

      throw e;
    }
  }

  @SneakyThrows
  void shutdown() {
    trackWriter.close();
    trackDirectory.close();
  }

  protected boolean isIndexValid(SearcherManager searcherManager) {
    IndexSearcher indexSearcher = null;

    try {
      indexSearcher = searcherManager.acquire();

      return !search(new TrackSearch("*")).isEmpty();
    } catch (Exception e) {
      log.error("Unable to check if index is valid", e);

      return false;
    } finally {
      try {
        searcherManager.release(indexSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }
    }
  }

  @Synchronized
  public void indexData() throws Exception {
    rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_DOWNLOAD_INDEX));

    trackWriter.deleteAll();

    dataService.parse(settingsService.getDataFile());
    commitIndexes();
    settingsService.setLastIndexedDate(LocalDateTime.now());

    fireEvent(DATA_INDEXED);
  }

  private void commitIndexes() {
    log.debug("Committing indexes");

    try {
      trackWriter.commit();
      trackManager.maybeRefreshBlocking();

      log.debug("Indexes committed");
    } catch (Exception e) {
      log.error("Unable to commit indexes", e);
    }
  }

  void addTrack(Track track) {
    Document document = new Document();

    // Keywords
    document.add(new TextField(TrackField.KEYWORDS.name(),
        prepareKeywords(
            nullSafeTrim(track.getArtistName()).toLowerCase() + " " +
                nullSafeTrim(track.getAlbumName()).toLowerCase() + " " +
                nullSafeTrim(track.getTrackName())),
        Field.Store.YES));

    // Result data
    document.add(new StringField(TrackField.ARTIST_ID.name(), track.getArtistId(), Field.Store.YES));
    document.add(new StringField(TrackField.ARTIST_NAME.name(), track.getArtistName(), Field.Store.YES));
    document.add(new StringField(TrackField.ALBUM_ID.name(), track.getAlbumId(), Field.Store.YES));
    document.add(new StringField(TrackField.ALBUM_NAME.name(), track.getAlbumName(), Field.Store.YES));
    document.add(new StringField(TrackField.ALBUM_IMAGE.name(), nullIsBlank(track.getAlbumImage()), Field.Store.YES));
    document.add(new StringField(TrackField.YEAR.name(), Integer.toString(track.getYear()), Field.Store.YES));
    document.add(new StringField(TrackField.TRACK_ID.name(), track.getTrackId(), Field.Store.YES));
    document.add(new StringField(TrackField.TRACK_NAME.name(), track.getTrackName(), Field.Store.YES));
    document.add(new StoredField(TrackField.INDEX.name(), track.getIndex()));
    document.add(new StringField(TrackField.LOCATION.name(), track.getLocation(), Field.Store.YES));
    document.add(new StringField(TrackField.IS_PREFERRED.name(), Boolean.toString(track.isPreferred()), Field.Store.YES));

    for (String genre : track.getGenres()) {
      document.add(new StringField(TrackField.GENRE.name(), genre, Field.Store.YES));
    }

    // Sorts
    document.add(new SortedDocValuesField(TrackSort.DEFAULT_SORT.name(),
        new BytesRef(stripWhitespace(track.getArtistName(), false) + padInteger(track.getYear())
            + stripWhitespace(track.getAlbumName(), false) + padInteger(track.getIndex()))));
    document.add(new SortedDocValuesField(TrackSort.ARTIST_SORT.name(),
        new BytesRef(padInteger(track.getYear()) + stripWhitespace(track.getArtistName(), false))));
    document.add(new SortedDocValuesField(TrackSort.ALBUM_SORT.name(),
        new BytesRef(padInteger(track.getYear()) + stripWhitespace(track.getAlbumName(), false))));
    document.add(new SortedDocValuesField(TrackSort.TRACK_SORT.name(),
        new BytesRef(padInteger(track.getYear()) + stripWhitespace(track.getTrackName(), false))));

    try {
      trackWriter.addDocument(document);
    } catch (Exception e) {
      log.error("Unable to index track - {}", track.getTrackId());
    }
  }

  @Synchronized
  protected List<String> getDistinctTrackFieldValues(TrackField trackField) {
    log.debug("Getting distinct track field values - {}", trackField);

    long startTime = System.currentTimeMillis();

    if (trackManager == null) {
      throw new RuntimeException("Cannot search before track index is initialised");
    }

    IndexSearcher trackSearcher = null;

    try {
      trackSearcher = trackManager.acquire();

      Set<String> fieldValues = new LinkedHashSet<>();

      for (LeafReaderContext context : getLeafReaderContexts(trackSearcher)) {
        try (LeafReader leafReader = context.reader()) {
          Terms terms = leafReader.terms(trackField.name());

          if (terms != null) {
            TermsEnum termsEnum = terms.iterator();
            BytesRef bytesRef;

            while ((bytesRef = termsEnum.next()) != null) {
              fieldValues.add(bytesRef.utf8ToString());
            }
          }
        }
      }

      return new ArrayList<>(fieldValues);
    } catch (Exception e) {
      log.error("Unable to get distinct track field values - {}", trackField, e);

      return emptyList();
    } finally {
      try {
        trackManager.release(trackSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }

      long queryTime = System.currentTimeMillis() - startTime;

      log.debug("Distinct track field values query time - {} milliseconds", queryTime);
    }
  }

  protected List<LeafReaderContext> getLeafReaderContexts(IndexSearcher indexSearcher) {
    return indexSearcher.getIndexReader().leaves();
  }

  @Synchronized
  public List<Track> search(TrackSearch trackSearch) {
    log.debug("Performing search");

    long startTime = System.currentTimeMillis();

    if (trackManager == null) {
      throw new RuntimeException("Cannot search before track index is initialised");
    }

    if (trackSearch == null || trackSearch.getKeywords() == null || trackSearch.getKeywords().trim().isEmpty()) {
      return emptyList();
    }

    IndexSearcher trackSearcher = null;

    try {
      trackSearcher = trackManager.acquire();
      TopFieldDocs results = trackSearcher.search(
          buildKeywordsQuery(prepareKeywords(trackSearch.getKeywords()),
              trackSearch.getTrackFilter().getTermQueries()),
          applicationProperties.getMaxSearchHits(), new Sort(new SortField(trackSearch.getTrackSort().name(), SortField.Type.STRING)));

      return getTracksFromScoreDocs(trackSearcher, results.scoreDocs);
    } catch (Exception e) {
      log.error("Unable to run track search", e);

      return emptyList();
    } finally {
      try {
        trackManager.release(trackSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }

      long queryTime = System.currentTimeMillis() - startTime;

      log.debug("Search query time - {} milliseconds", queryTime);
    }
  }

  @Synchronized
  public List<Track> getShuffledPlaylist(int playlistSize, String yearFilter) {
    log.debug("Getting shuffled playlist size - {} - {}", playlistSize, yearFilter);

    long startTime = System.currentTimeMillis();

    if (trackManager == null) {
      throw new RuntimeException("Cannot search before track index is initialised");
    }

    IndexSearcher trackSearcher = null;

    try {
      trackSearcher = trackManager.acquire();

      int maxSearchHits = getMaxDoc(trackSearcher);
      List<Track> playlist = new ArrayList<>();

      log.debug("Max search hits - {}", maxSearchHits);

      Query query;

      if (yearFilter != null) {
        query = new BooleanQuery.Builder()
            .add(new TermQuery(new Term(TrackField.YEAR.name(), yearFilter)), BooleanClause.Occur.MUST).build();
      } else {
        query = new MatchAllDocsQuery();
      }

      TopDocs results = trackSearcher.search(query, maxSearchHits);
      ScoreDoc[] scoreDocs = results.scoreDocs;

      log.debug("Hits - {}", results.totalHits);
      log.debug("Score docs - {}", scoreDocs.length);

      if (playlistSize < results.totalHits.value()) {
        final IndexSearcher finalSearcher = trackSearcher;
        Future<Integer> future = executorService.submit(() -> {
          while (playlist.size() < playlistSize) {
            int docId = (int) (secureRandom.nextDouble() * results.totalHits.value());
            Track track = getTrackByDocId(finalSearcher, scoreDocs[docId].doc);

            if (!playlist.contains(track)) {
              playlist.add(track);
            }

            if (Thread.interrupted()) {
              log.debug("Random playlist future interrupted");
              return 1;
            }
          }

          return 0;
        });

        try {
          future.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          // Ignore this as we will return what we've got
          if (!future.isDone()) {
            log.debug("Timed out getting random playlist, killing future");
            future.cancel(true);
          }
        }
      } else {
        for (ScoreDoc scoreDoc : scoreDocs) {
          playlist.add(getTrackByDocId(trackSearcher, scoreDoc.doc));
        }

        shuffle(playlist);
      }

      return playlist;
    } catch (Exception e) {
      log.error("Unable to get shuffled playlist", e);

      return emptyList();
    } finally {
      try {
        trackManager.release(trackSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }

      long queryTime = System.currentTimeMillis() - startTime;

      log.debug("Shuffled playlist query time - {} milliseconds", queryTime);
    }
  }

  protected int getMaxDoc(IndexSearcher indexSearcher) {
    return indexSearcher.getIndexReader().maxDoc();
  }

  @Synchronized
  public Optional<Track> getTrackById(String trackId) {
    if (trackManager == null) {
      throw new RuntimeException("Cannot search before track index is initialised");
    }

    IndexSearcher trackSearcher = null;

    try {
      trackSearcher = trackManager.acquire();
      TopDocs results = trackSearcher.search(new TermQuery(new Term(TrackField.TRACK_ID.name(), trackId)), 1);

      if (results.totalHits.value() < 1) {
        return empty();
      }

      return of(getTrackByDocId(trackSearcher, results.scoreDocs[0].doc));
    } catch (Exception e) {
      log.error("Unable to run get track by id", e);

      return empty();
    } finally {
      try {
        trackManager.release(trackSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }
    }
  }

  @Synchronized
  Optional<List<Track>> getAlbumById(String albumId) {
    if (trackManager == null) {
      throw new RuntimeException("Cannot search before track index is initialised");
    }

    IndexSearcher trackSearcher = null;

    try {
      trackSearcher = trackManager.acquire();
      TopDocs results = trackSearcher.search(new TermQuery(new Term(TrackField.ALBUM_ID.name(), albumId)),
          applicationProperties.getMaxSearchHits(), new Sort(new SortField(TrackSort.DEFAULT_SORT.name(), SortField.Type.STRING)));

      return of(getTracksFromScoreDocs(trackSearcher, results.scoreDocs));
    } catch (Exception e) {
      log.error("Unable to run get album by id", e);

      return empty();
    } finally {
      try {
        trackManager.release(trackSearcher);
      } catch (Exception e) {
        log.warn("Unable to release track searcher");
      }
    }
  }

  private List<Track> getTracksFromScoreDocs(IndexSearcher trackSearcher, ScoreDoc[] scoreDocs) throws Exception {
    List<Track> tracks = new ArrayList<>();

    for (ScoreDoc scoreDoc : scoreDocs) {
      tracks.add(getTrackByDocId(trackSearcher, scoreDoc.doc));
    }

    return tracks;
  }

  private Track getTrackByDocId(IndexSearcher trackSearcher, int docId) throws Exception {
    Document document = getStoredFields(trackSearcher).document(docId);

    return Track.builder()
        .artistId(document.get(TrackField.ARTIST_ID.name()))
        .artistName(document.get(TrackField.ARTIST_NAME.name()))
        .albumId(document.get(TrackField.ALBUM_ID.name()))
        .albumName(document.get(TrackField.ALBUM_NAME.name()))
        .albumImage(document.get(TrackField.ALBUM_IMAGE.name()))
        .year(Integer.parseInt(document.get(TrackField.YEAR.name())))
        .trackId(document.get(TrackField.TRACK_ID.name()))
        .trackName(document.get(TrackField.TRACK_NAME.name()))
        .index(Integer.parseInt(document.get(TrackField.INDEX.name())))
        .location(document.get(TrackField.LOCATION.name()))
        .isPreferred(Boolean.parseBoolean(document.get(TrackField.IS_PREFERRED.name())))
        .genres(List.of(document.getValues(TrackField.GENRE.name())))
        .build();
  }

  protected StoredFields getStoredFields(IndexSearcher indexSearcher) throws IOException {
    return indexSearcher.getIndexReader().storedFields();
  }

  private Query buildKeywordsQuery(String keywords, List<TermQuery> termQueries) {
    Builder builder = new BooleanQuery.Builder();

    if ("*".equals(keywords)) {
      builder.add(new WildcardQuery(new Term(TrackField.KEYWORDS.name(), keywords)), BooleanClause.Occur.MUST);
    } else {
      // Split into whole words with the last word having
      // a wildcard '*' on the end
      for (StringTokenizer tokens = new StringTokenizer(keywords, " "); tokens.hasMoreTokens(); ) {
        String token = tokens.nextToken();

        if (tokens.hasMoreElements()) {
          builder.add(new TermQuery(new Term(TrackField.KEYWORDS.name(), token)), BooleanClause.Occur.MUST);
        } else {
          builder.add(new WildcardQuery(new Term(TrackField.KEYWORDS.name(), (token + "*"))),
              BooleanClause.Occur.MUST);
        }
      }
    }

    ofNullable(termQueries).ifPresent(t -> t.forEach(termQuery -> builder.add(termQuery, Occur.MUST)));

    return builder.build();
  }

  protected String prepareKeywords(String keywords) {
    if (keywords == null) {
      return "";
    }

    String trimmedKeywords = keywords.trim();

    if ("*".equals(trimmedKeywords)) {
      return trimmedKeywords;
    }

    String accentsStripped = stripAccents(trimmedKeywords);
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < accentsStripped.length(); i++) {
      char nextChar = accentsStripped.charAt(i);

      if (nextChar == ' ' || Character.isLetterOrDigit(nextChar)) {
        builder.append(nextChar);
      }
    }

    return builder.toString().trim().toLowerCase();
  }

  protected String nullSafeTrim(String string) {
    if (string == null) {
      return ("");
    }

    return string.trim();
  }

  private String nullIsBlank(String string) {
    if (string == null) {
      return "";
    }

    return string;
  }

  protected String stripWhitespace(String string, boolean keepSpaces) {
    if (string == null) {
      return ("");
    }

    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < string.length(); i++) {
      char nextChar = string.charAt(i);

      if (keepSpaces && (nextChar == ' ')) {
        builder.append(nextChar);
      } else if (!Character.isWhitespace(nextChar)) {
        builder.append(nextChar);
      }
    }

    return builder.toString().trim();
  }

  protected String padInteger(int toPad) {
    String string = "0000000000" + toPad;

    return string.substring(string.length() - 10);
  }
}
