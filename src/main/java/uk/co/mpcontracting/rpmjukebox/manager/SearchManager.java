package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.ArtistField;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.stripAccents;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchManager extends EventAwareObject implements Constants {

    private final AppProperties appProperties;
    private final RpmJukebox rpmJukebox;
    private final MessageManager messageManager;

    private SettingsManager settingsManager;
    private ApplicationManager applicationManager;
    private DataManager dataManager;

    @Getter
    private List<String> genreList;
    @Getter
    private List<String> yearList;
    @Getter
    private List<TrackSort> trackSortList;

    private Directory artistDirectory;
    private IndexWriter artistWriter;
    private SearcherManager artistManager;

    private Directory trackDirectory;
    private IndexWriter trackWriter;
    private SearcherManager trackManager;

    private SecureRandom random;

    private ExecutorService executorService;

    @Autowired
    public void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    public void wireApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    @Autowired
    public void wireDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void initialise() throws Exception {
        log.info("Initialising SearchManager");

        try {
            // Initialise the executor service
            executorService = Executors.newSingleThreadExecutor();

            // Initialise the indexes
            Analyzer analyzer = new WhitespaceAnalyzer();
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);

            artistDirectory = FSDirectory.open(settingsManager.getFileFromConfigDirectory(appProperties.getArtistIndexDirectory()).toPath());
            IndexWriterConfig artistWriterConfig = new IndexWriterConfig(analyzer);
            artistWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            artistWriter = new IndexWriter(artistDirectory, artistWriterConfig);
            artistManager = new SearcherManager(artistWriter, null);

            trackDirectory = FSDirectory.open(settingsManager.getFileFromConfigDirectory(appProperties.getTrackIndexDirectory()).toPath());
            IndexWriterConfig trackWriterConfig = new IndexWriterConfig(analyzer);
            trackWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            trackWriter = new IndexWriter(trackDirectory, trackWriterConfig);
            trackManager = new SearcherManager(trackWriter, null);

            random = new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes());

            // See if we already have valid indexes, if not, build them
            if (settingsManager.hasDataFileExpired() || settingsManager.isNewVersion() || !isIndexValid(artistManager) || !isIndexValid(trackManager)) {
                indexData();
            }

            rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_INITIALISING_SEARCH));

            // Initialise the filters and sorts
            genreList = new ArrayList<>();
            genreList.add(UNSPECIFIED_GENRE);
            genreList.addAll(getDistinctTrackFieldValues(TrackField.GENRE));
            Collections.sort(genreList);

            yearList = getDistinctTrackFieldValues(TrackField.YEAR);
            Collections.sort(yearList);

            trackSortList = Arrays.asList(TrackSort.values());

            // Warm up the search
            String searchWarmer = "test song";

            for (int i = 0; i < searchWarmer.length(); i++) {
                search(new TrackSearch(searchWarmer.substring(0, i + 1)));
            }

            log.debug("SearchManager initialised");
        } catch (LockObtainFailedException e) {
            rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_ALREADY_RUNNING));

            try {
                Thread.sleep(5000);
            } catch (Exception e2) {
                // Do nothing
            }

            applicationManager.shutdown();
        } catch (Exception e) {
            log.error("Error initialising SearchManager", e);

            throw e;
        }
    }

    @SneakyThrows
    void shutdown() {
        artistWriter.close();
        artistDirectory.close();
        trackWriter.close();
        trackDirectory.close();
    }

    // Package level for testing purposes
    boolean isIndexValid(SearcherManager searcherManager) {
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
        rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_DOWNLOAD_INDEX));

        artistWriter.deleteAll();
        trackWriter.deleteAll();

        dataManager.parse(settingsManager.getDataFile());
        commitIndexes();
        settingsManager.setLastIndexedDate(LocalDateTime.now());

        fireEvent(Event.DATA_INDEXED);
    }

    private void commitIndexes() {
        log.debug("Committing indexes");

        try {
            artistWriter.commit();
            trackWriter.commit();

            artistManager.maybeRefreshBlocking();
            trackManager.maybeRefreshBlocking();

            log.debug("Indexes committed");
        } catch (Exception e) {
            log.error("Unable to commit indexes", e);
        }
    }

    void addArtist(Artist artist) {
        Document document = new Document();

        document.add(new StringField(ArtistField.ARTIST_ID.name(), artist.getArtistId(), Field.Store.YES));
        document.add(new StringField(ArtistField.ARTIST_NAME.name(), artist.getArtistName(), Field.Store.YES));
        document.add(new StringField(ArtistField.ARTIST_IMAGE.name(), nullIsBlank(artist.getArtistImage()), Field.Store.YES));
        document.add(new StringField(ArtistField.BIOGRAPHY.name(), nullIsBlank(artist.getBiography()), Field.Store.YES));
        document.add(new StringField(ArtistField.MEMBERS.name(), nullIsBlank(artist.getMembers()), Field.Store.YES));

        try {
            artistWriter.addDocument(document);
        } catch (Exception e) {
            log.error("Unable to index artist - {}", artist.getArtistId());
        }
    }

    void addTrack(Track track) {
        Document document = new Document();

        // Keywords
        document.add(new TextField(TrackField.KEYWORDS.name(),
                prepareKeywords(
                        nullSafeTrim(track.getArtistName()).toLowerCase() + " " +
                        nullSafeTrim(track.getAlbumName()).toLowerCase() + " " +
                        nullSafeTrim(track.getTrackName())
                ),
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

    // Package level for testing purposes
    @Synchronized
    List<String> getDistinctTrackFieldValues(TrackField trackField) {
        log.debug("Getting distinct track field values - {}", trackField);

        long startTime = System.currentTimeMillis();

        if (trackManager == null) {
            throw new RuntimeException("Cannot search before track index is initialised");
        }

        IndexSearcher trackSearcher = null;

        try {
            trackSearcher = trackManager.acquire();

            Set<String> fieldValues = new LinkedHashSet<>();
            IndexReader indexReader = trackSearcher.getIndexReader();

            for (LeafReaderContext context : indexReader.leaves()) {
                Terms terms = context.reader().terms(trackField.name());

                if (terms != null) {
                    TermsEnum termsEnum = terms.iterator();
                    BytesRef bytesRef;

                    while ((bytesRef = termsEnum.next()) != null) {
                        fieldValues.add(bytesRef.utf8ToString());
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

    @Synchronized
    public List<Track> search(TrackSearch trackSearch) {
        log.debug("Performing search");

        long startTime = System.currentTimeMillis();

        if (trackManager == null) {
            throw new RuntimeException("Cannot search before track index is initialised");
        }

        if (trackSearch == null || trackSearch.getKeywords() == null || trackSearch.getKeywords().trim().length() < 1) {
            return emptyList();
        }

        IndexSearcher trackSearcher = null;

        try {
            trackSearcher = trackManager.acquire();
            TopFieldDocs results = trackSearcher.search(
                    buildKeywordsQuery(prepareKeywords(trackSearch.getKeywords()),
                            trackSearch.getTrackFilter().getFilter()),
                    appProperties.getMaxSearchHits(), new Sort(new SortField(trackSearch.getTrackSort().name(), SortField.Type.STRING)));

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

            int maxSearchHits = trackSearcher.getIndexReader().maxDoc();
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

            if (playlistSize < results.totalHits) {
                final IndexSearcher finalSearcher = trackSearcher;
                Future<Integer> future = executorService.submit(() -> {
                    while (playlist.size() < playlistSize) {
                        int docId = (int) (random.nextDouble() * results.totalHits);
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

    @Synchronized
    Optional<Artist> getArtistById(String artistId) {
        if (artistManager == null) {
            throw new RuntimeException("Cannot search before artist index is initialised");
        }

        IndexSearcher artistSearcher = null;

        try {
            artistSearcher = artistManager.acquire();
            TopDocs results = artistSearcher.search(new TermQuery(new Term(ArtistField.ARTIST_ID.name(), artistId)), 1);

            if (results.totalHits < 1) {
                return empty();
            }

            return of(getArtistByDocId(artistSearcher, results.scoreDocs[0].doc));
        } catch (Exception e) {
            log.error("Unable to run get artist by id", e);

            return empty();
        } finally {
            try {
                artistManager.release(artistSearcher);
            } catch (Exception e) {
                log.warn("Unable to release artist searcher");
            }
        }
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

            if (results.totalHits < 1) {
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
                    appProperties.getMaxSearchHits(), new Sort(new SortField(TrackSort.DEFAULT_SORT.name(), SortField.Type.STRING)));

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

    private Artist getArtistByDocId(IndexSearcher artistSearcher, int docId) throws Exception {
        Document document = artistSearcher.doc(docId);

        return Artist.builder()
                .artistId(document.get(ArtistField.ARTIST_ID.name()))
                .artistName(document.get(ArtistField.ARTIST_NAME.name()))
                .artistImage(document.get(ArtistField.ARTIST_IMAGE.name()))
                .biography(document.get(ArtistField.BIOGRAPHY.name()))
                .members(document.get(ArtistField.MEMBERS.name()))
                .build();
    }

    private List<Track> getTracksFromScoreDocs(IndexSearcher trackSearcher, ScoreDoc[] scoreDocs) throws Exception {
        List<Track> tracks = new ArrayList<>();

        for (ScoreDoc scoreDoc : scoreDocs) {
            tracks.add(getTrackByDocId(trackSearcher, scoreDoc.doc));
        }

        return tracks;
    }

    private Track getTrackByDocId(IndexSearcher trackSearcher, int docId) throws Exception {
        Document document = trackSearcher.doc(docId);

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
                .genres(Arrays.asList(document.getValues(TrackField.GENRE.name())))
                .build();
    }

    private Query buildKeywordsQuery(String keywords, Query trackFilter) {
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

        if (trackFilter != null) {
            builder.add(trackFilter, BooleanClause.Occur.MUST);
        }

        return builder.build();
    }

    private String nullIsBlank(String string) {
        if (string == null) {
            return "";
        }

        return string;
    }

    private String padInteger(int toPad) {
        String string = "0000000000" + toPad;

        return string.substring(string.length() - 10);
    }

    private String nullSafeTrim(String string) {
        if (string == null) {
            return ("");
        }

        return string.trim();
    }

    private String stripWhitespace(String string, boolean keepSpaces) {
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

    // Package level for testing purposes
    String prepareKeywords(String keywords) {
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
}
