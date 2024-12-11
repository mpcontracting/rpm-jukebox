package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.event.Event.DATA_INDEXED;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createGenre;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.createTrack;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.UNSPECIFIED_GENRE;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractEventAwareObjectTest;
import uk.co.mpcontracting.rpmjukebox.test.util.TestTermsEnum;

class SearchServiceTest extends AbstractEventAwareObjectTest {

  @Mock
  private RpmJukebox rpmJukebox;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private StringResourceService stringResourceService;

  @Mock
  private ApplicationLifecycleService applicationLifecycleService;

  @Mock
  private DataService dataService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private Directory trackDirectory;

  @Mock
  private IndexWriter trackWriter;

  @Mock
  private SearcherManager trackManager;

  private SearchService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new SearchService(rpmJukebox, applicationProperties, stringResourceService, settingsService));
    setField(underTest, "applicationLifecycleService", applicationLifecycleService);
    setField(underTest, "dataService", dataService);

    lenient().when(applicationProperties.getTrackIndexDirectory()).thenReturn("trackIndex");
    lenient().when(settingsService.getFileFromConfigDirectory("trackIndex")).thenReturn(new File(getConfigDirectory(), "trackIndex"));

    setField(underTest, "trackDirectory", trackDirectory);
    setField(underTest, "trackManager", trackManager);
    setField(underTest, "trackWriter", trackWriter);
  }

  @AfterEach
  @SneakyThrows
  void afterEach() {
    FileUtils.deleteDirectory(getConfigDirectory());
  }

  @Test
  @SneakyThrows
  void shouldInitialise() {
    try {
      doReturn(true).when(underTest).isIndexValid(any());
      doReturn(emptyList()).when(underTest).search(any());
      doReturn(emptyList()).when(underTest).getDistinctTrackFieldValues(any());
      when(settingsService.hasDataFileExpired()).thenReturn(false);

      setField(underTest, "trackDirectory", null);
      setField(underTest, "trackManager", null);
      setField(underTest, "trackWriter", null);

      underTest.initialise();

      Directory trackDirectoryField = getField(underTest, "trackDirectory", Directory.class);
      IndexWriter trackWriterField = getField(underTest, "trackWriter", IndexWriter.class);
      SearcherManager trackManagerField = getField(underTest, "trackManager", SearcherManager.class);
      SecureRandom secureRandomField = getField(underTest, "secureRandom", SecureRandom.class);
      ExecutorService executorServiceField = getField(underTest, "executorService", ExecutorService.class);
      List<String> genreList = underTest.getGenreList();
      List<String> yearList = underTest.getYearList();
      List<TrackSort> trackSortList = underTest.getTrackSortList();

      assertThat(trackDirectoryField).isNotNull();
      assertThat(trackWriterField).isNotNull();
      assertThat(trackManagerField).isNotNull();
      assertThat(secureRandomField).isNotNull();
      assertThat(executorServiceField).isNotNull();
      assertThat(genreList).hasSize(1);
      assertThat(genreList.getFirst()).isEqualTo(UNSPECIFIED_GENRE);
      assertThat(yearList).isEmpty();
      assertThat(trackSortList).hasSize(4);

      verify(underTest, never()).indexData();
      verify(underTest, times(9)).search(any());
    } finally {
      underTest.shutdown();
    }
  }

  @Test
  @SneakyThrows
  void shouldInitialiseAndIndexWhenDataFileHasExpired() {
    try {
      doNothing().when(underTest).indexData();
      doReturn(emptyList()).when(underTest).search(any());
      doReturn(emptyList()).when(underTest).getDistinctTrackFieldValues(any());
      when(settingsService.hasDataFileExpired()).thenReturn(true);

      setField(underTest, "trackDirectory", null);
      setField(underTest, "trackManager", null);
      setField(underTest, "trackWriter", null);

      underTest.initialise();

      Directory trackDirectoryField = getField(underTest, "trackDirectory", Directory.class);
      IndexWriter trackWriterField = getField(underTest, "trackWriter", IndexWriter.class);
      SearcherManager trackManagerField = getField(underTest, "trackManager", SearcherManager.class);
      SecureRandom secureRandomField = getField(underTest, "secureRandom", SecureRandom.class);
      ExecutorService executorServiceField = getField(underTest, "executorService", ExecutorService.class);
      List<String> genreList = underTest.getGenreList();
      List<String> yearList = underTest.getYearList();
      List<TrackSort> trackSortList = underTest.getTrackSortList();

      assertThat(trackDirectoryField).isNotNull();
      assertThat(trackWriterField).isNotNull();
      assertThat(trackManagerField).isNotNull();
      assertThat(secureRandomField).isNotNull();
      assertThat(executorServiceField).isNotNull();
      assertThat(genreList).hasSize(1);
      assertThat(genreList.getFirst()).isEqualTo(UNSPECIFIED_GENRE);
      assertThat(yearList).isEmpty();
      assertThat(trackSortList).hasSize(4);

      verify(underTest).indexData();
      verify(underTest, times(9)).search(any());
    } finally {
      underTest.shutdown();
    }
  }

  @Test
  @SneakyThrows
  void shouldInitialiseAndIndexWhenIndexIsInvalid() {
    try {
      doNothing().when(underTest).indexData();
      doReturn(false).when(underTest).isIndexValid(any());
      doReturn(emptyList()).when(underTest).search(any());
      doReturn(emptyList()).when(underTest).getDistinctTrackFieldValues(any());
      when(settingsService.hasDataFileExpired()).thenReturn(false);

      setField(underTest, "trackDirectory", null);
      setField(underTest, "trackManager", null);
      setField(underTest, "trackWriter", null);

      underTest.initialise();

      Directory trackDirectoryField = getField(underTest, "trackDirectory", Directory.class);
      IndexWriter trackWriterField = getField(underTest, "trackWriter", IndexWriter.class);
      SearcherManager trackManagerField = getField(underTest, "trackManager", SearcherManager.class);
      SecureRandom secureRandomField = getField(underTest, "secureRandom", SecureRandom.class);
      ExecutorService executorServiceField = getField(underTest, "executorService", ExecutorService.class);
      List<String> genreList = underTest.getGenreList();
      List<String> yearList = underTest.getYearList();
      List<TrackSort> trackSortList = underTest.getTrackSortList();

      assertThat(trackDirectoryField).isNotNull();
      assertThat(trackWriterField).isNotNull();
      assertThat(trackManagerField).isNotNull();
      assertThat(secureRandomField).isNotNull();
      assertThat(executorServiceField).isNotNull();
      assertThat(genreList).hasSize(1);
      assertThat(genreList.getFirst()).isEqualTo(UNSPECIFIED_GENRE);
      assertThat(yearList).isEmpty();
      assertThat(trackSortList).hasSize(4);

      verify(underTest).indexData();
      verify(underTest, times(9)).search(any());
    } finally {
      underTest.shutdown();
    }
  }

  @Test
  @SneakyThrows
  void shouldNotInitialiseIfAlreadyInitialised() {
    try {
      doNothing().when(underTest).indexData();
      doReturn(false).when(underTest).isIndexValid(any());
      doReturn(emptyList()).when(underTest).search(any());
      doReturn(emptyList()).when(underTest).getDistinctTrackFieldValues(any());
      when(settingsService.hasDataFileExpired()).thenReturn(false);

      setField(underTest, "trackDirectory", null);
      setField(underTest, "trackManager", null);
      setField(underTest, "trackWriter", null);

      underTest.initialise();
      underTest.initialise();

      verify(applicationLifecycleService).shutdown();
    } finally {
      underTest.shutdown();
    }
  }

  @Test
  @SneakyThrows
  void shouldThrowExceptionOnInitialise() {
    try {
      doThrow(new RuntimeException("SearchManagerTest.shouldThrowExceptionOnInitialise()")).when(underTest).indexData();
      doReturn(false).when(underTest).isIndexValid(any());

      assertThatThrownBy(() -> underTest.initialise()).isInstanceOf(RuntimeException.class);
    } finally {
      underTest.shutdown();
    }
  }

  @Test
  @SneakyThrows
  void shouldReturnIndexValid() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);
    doNothing().when(trackManager).release(indexSearcher);

    List<Track> tracks = new ArrayList<>();
    tracks.add(mock(Track.class));

    doReturn(tracks).when(underTest).search(any());

    boolean isValid = underTest.isIndexValid(trackManager);

    assertThat(isValid).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReturnIndexValidWhenExceptionOnRelease() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);
    doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexValidWhenExceptionOnRelease()"))
        .when(trackManager).release(indexSearcher);

    List<Track> tracks = new ArrayList<>();
    tracks.add(mock(Track.class));

    doReturn(tracks).when(underTest).search(any());

    boolean isValid = underTest.isIndexValid(trackManager);

    assertThat(isValid).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReturnIndexInvalidWithEmptyTracks() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);
    doNothing().when(trackManager).release(indexSearcher);
    doReturn(emptyList()).when(underTest).search(any());

    boolean isValid = underTest.isIndexValid(trackManager);

    assertThat(isValid).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReturnIndexInvalidWithNullTracks() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);
    doNothing().when(trackManager).release(indexSearcher);
    doReturn(null).when(underTest).search(any());

    boolean isValid = underTest.isIndexValid(trackManager);

    assertThat(isValid).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldReturnIndexInvalidOnException() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);
    doNothing().when(trackManager).release(indexSearcher);
    doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexInvalidOnException()")).when(underTest)
        .search(any());

    boolean isValid = underTest.isIndexValid(trackManager);

    assertThat(isValid).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldIndexData() {
    underTest.indexData();

    verify(dataService).parse(any());
    verify(trackWriter).commit();
    verify(trackManager).maybeRefreshBlocking();
    verify(settingsService).setLastIndexedDate(any());
    verify(eventProcessor).fireEvent(DATA_INDEXED);
  }

  @Test
  @SneakyThrows
  void shouldIndexDataButNotCommitOnException() {
    doThrow(new RuntimeException("SearchManagerTest.shouldIndexDataButNotCommitOnException()"))
        .when(trackWriter).commit();

    underTest.indexData();

    verify(dataService).parse(any());
    verify(trackWriter).commit();
    verify(trackManager, never()).maybeRefreshBlocking();
    verify(settingsService).setLastIndexedDate(any());
    verify(eventProcessor).fireEvent(DATA_INDEXED);
  }

  @Test
  @SneakyThrows
  void shouldAddTrack() {
    Track track = createTrack(1, createGenre(), createGenre(), createGenre());
    String keywords = underTest.nullSafeTrim(track.getArtistName()).toLowerCase() + " " +
        underTest.nullSafeTrim(track.getAlbumName()).toLowerCase() + " " +
        underTest.nullSafeTrim(track.getTrackName());
    String defaultSort = underTest.stripWhitespace(track.getArtistName(), false) + underTest.padInteger(track.getYear())
        + underTest.stripWhitespace(track.getAlbumName(), false) + underTest.padInteger(track.getIndex());
    String artistSort = underTest.padInteger(track.getYear()) + underTest.stripWhitespace(track.getArtistName(), false);
    String albumSort = underTest.padInteger(track.getYear()) + underTest.stripWhitespace(track.getAlbumName(), false);
    String trackSort = underTest.padInteger(track.getYear()) + underTest.stripWhitespace(track.getTrackName(), false);

    underTest.addTrack(track);

    ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
    verify(trackWriter).addDocument(document.capture());

    assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).stringValue()).isEqualTo(keywords);
    assertThat(document.getValue().getField(TrackField.ARTIST_ID.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.ARTIST_ID.name()).stringValue()).isEqualTo(track.getArtistId());
    assertThat(document.getValue().getField(TrackField.ARTIST_NAME.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.ARTIST_NAME.name()).stringValue()).isEqualTo(track.getArtistName());
    assertThat(document.getValue().getField(TrackField.ALBUM_ID.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.ALBUM_ID.name()).stringValue()).isEqualTo(track.getAlbumId());
    assertThat(document.getValue().getField(TrackField.ALBUM_NAME.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.ALBUM_NAME.name()).stringValue()).isEqualTo(track.getAlbumName());
    assertThat(document.getValue().getField(TrackField.ALBUM_IMAGE.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.ALBUM_IMAGE.name()).stringValue()).isEqualTo(track.getAlbumImage());
    assertThat(document.getValue().getField(TrackField.YEAR.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.YEAR.name()).stringValue()).isEqualTo(Integer.toString(track.getYear()));
    assertThat(document.getValue().getField(TrackField.TRACK_ID.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.TRACK_ID.name()).stringValue()).isEqualTo(track.getTrackId());
    assertThat(document.getValue().getField(TrackField.TRACK_NAME.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.TRACK_NAME.name()).stringValue()).isEqualTo(track.getTrackName());
    assertThat(document.getValue().getField(TrackField.INDEX.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.INDEX.name()).stringValue()).isEqualTo(Integer.toString(track.getIndex()));
    assertThat(document.getValue().getField(TrackField.LOCATION.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.LOCATION.name()).stringValue()).isEqualTo(track.getLocation());
    assertThat(document.getValue().getField(TrackField.IS_PREFERRED.name()).fieldType().stored()).isTrue();
    assertThat(document.getValue().getField(TrackField.IS_PREFERRED.name()).stringValue()).isEqualTo(Boolean.toString(track.isPreferred()));

    assertThat(document.getValue().getFields(TrackField.GENRE.name())).hasSize(track.getGenres().size());

    for (int i = 0; i < track.getGenres().size(); i++) {
      assertThat(document.getValue().getFields(TrackField.GENRE.name())[i].fieldType().stored()).isTrue();
      assertThat(document.getValue().getFields(TrackField.GENRE.name())[i].stringValue()).isEqualTo(track.getGenres().get(i));
    }

    assertThat(document.getValue().getBinaryValue(TrackSort.DEFAULT_SORT.name()).utf8ToString()).isEqualTo(defaultSort);
    assertThat(document.getValue().getBinaryValue(TrackSort.ARTIST_SORT.name()).utf8ToString()).isEqualTo(artistSort);
    assertThat(document.getValue().getBinaryValue(TrackSort.ALBUM_SORT.name()).utf8ToString()).isEqualTo(albumSort);
    assertThat(document.getValue().getBinaryValue(TrackSort.TRACK_SORT.name()).utf8ToString()).isEqualTo(trackSort);
  }

  @Test
  @SneakyThrows
  void shouldNotAddTrackOnException() {
    doThrow(new RuntimeException("SearchManagerTest.shouldNotAddTrackOnException()")).when(trackWriter)
        .addDocument(any());

    underTest.addTrack(createTrack(1, createGenre(), createGenre()));

    ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
    verify(trackWriter).addDocument(document.capture());

    assertThat(document.getValue().getFields()).hasSize(18);
  }

  @Test
  @SneakyThrows
  void shouldGetDistinctTrackFieldValues() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);

    LeafReaderContext leafReaderContext = mock(LeafReaderContext.class);
    List<LeafReaderContext> leafReaderContexts = singletonList(leafReaderContext);
    when(indexReader.leaves()).thenReturn(leafReaderContexts);

    LeafReader leafReader = mock(LeafReader.class);
    when(leafReaderContext.reader()).thenReturn(leafReader);

    Iterator<BytesRef> bytesRefIterator = List.of(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
    Terms terms = mock(Terms.class);
    when(leafReader.terms(anyString())).thenReturn(terms);
    when(terms.iterator()).thenReturn(new TestTermsEnum(bytesRefIterator));

    List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("Ref 1");
    assertThat(result.get(1)).isEqualTo("Ref 2");
  }

  @Test
  void shouldFailToGetDistinctTrackFieldValuesIfTrackManagerIsNull() {
    setField(underTest, "trackManager", null);

    assertThatThrownBy(() -> underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @SneakyThrows
  void shouldGetEmptyDistinctTrackFieldValuesWhenTermsNull() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);

    LeafReaderContext leafReaderContext = mock(LeafReaderContext.class);
    List<LeafReaderContext> leafReaderContexts = singletonList(leafReaderContext);
    when(indexReader.leaves()).thenReturn(leafReaderContexts);

    LeafReader leafReader = mock(LeafReader.class);
    when(leafReaderContext.reader()).thenReturn(leafReader);
    when(leafReader.terms(anyString())).thenReturn(null);

    List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

    assertThat(result).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown() {
    doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown()"))
        .when(trackManager).acquire();

    List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

    assertThat(result).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease() {
    doThrow(new RuntimeException("SearchManagerTest.shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease()"))
        .when(trackManager).release(any());

    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);

    LeafReaderContext leafReaderContext = mock(LeafReaderContext.class);
    List<LeafReaderContext> leafReaderContexts = singletonList(leafReaderContext);
    when(indexReader.leaves()).thenReturn(leafReaderContexts);

    LeafReader leafReader = mock(LeafReader.class);
    when(leafReaderContext.reader()).thenReturn(leafReader);

    Iterator<BytesRef> bytesRefIterator = List.of(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
    Terms terms = mock(Terms.class);
    when(leafReader.terms(anyString())).thenReturn(terms);
    when(terms.iterator()).thenReturn(new TestTermsEnum(bytesRefIterator));

    List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

    assertThat(result).hasSize(2);
    assertThat(result.get(0)).isEqualTo("Ref 1");
    assertThat(result.get(1)).isEqualTo("Ref 2");
  }

  @Test
  @SneakyThrows
  void shouldGetSearchResults() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    ScoreDoc[] scoreDocs = {new ScoreDoc(1, 0), new ScoreDoc(2, 0)};
    when(indexSearcher.search(any(), anyInt(), any()))
        .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
    setTrackSearcherDocuments(indexSearcher);

    List<Track> result = underTest.search(new TrackSearch("keywords"));

    assertThat(result).hasSize(2);

    Track track1 = result.getFirst();

    assertThat(track1.getArtistId()).isEqualTo("1231");
    assertThat(track1.getArtistName()).isEqualTo("Artist Name 1");
    assertThat(track1.getAlbumId()).isEqualTo("4561");
    assertThat(track1.getAlbumName()).isEqualTo("Album Name 1");
    assertThat(track1.getAlbumImage()).isEqualTo("Album Image 1");
    assertThat(track1.getYear()).isEqualTo(2001);
    assertThat(track1.getTrackId()).isEqualTo("7891");
    assertThat(track1.getTrackName()).isEqualTo("Track Name 1");
    assertThat(track1.getIndex()).isEqualTo(1);
    assertThat(track1.getLocation()).isEqualTo("Location 1");
    assertThat(track1.isPreferred()).isTrue();
    assertThat(track1.getGenres()).hasSize(2);
    assertThat(track1.getGenres().get(0)).isEqualTo("Genre 1 1");
    assertThat(track1.getGenres().get(1)).isEqualTo("Genre 2 1");

    Track track2 = result.get(1);

    assertThat(track2.getArtistId()).isEqualTo("1232");
    assertThat(track2.getArtistName()).isEqualTo("Artist Name 2");
    assertThat(track2.getAlbumId()).isEqualTo("4562");
    assertThat(track2.getAlbumName()).isEqualTo("Album Name 2");
    assertThat(track2.getAlbumImage()).isEqualTo("Album Image 2");
    assertThat(track2.getYear()).isEqualTo(2002);
    assertThat(track2.getTrackId()).isEqualTo("7892");
    assertThat(track2.getTrackName()).isEqualTo("Track Name 2");
    assertThat(track2.getIndex()).isEqualTo(2);
    assertThat(track2.getLocation()).isEqualTo("Location 2");
    assertThat(track2.isPreferred()).isFalse();
    assertThat(track2.getGenres()).hasSize(2);
    assertThat(track2.getGenres().get(0)).isEqualTo("Genre 1 2");
    assertThat(track2.getGenres().get(1)).isEqualTo("Genre 2 2");
  }

  @Test
  void shouldFailToGetSearchResultsIfTrackManagerIsNull() {
    setField(underTest, "trackManager", null);

    assertThatThrownBy(() -> underTest.search(new TrackSearch("keywords"))).isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldGetEmptySearchResultsWithNullTrackSearch() {
    List<Track> result = underTest.search(null);

    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetEmptySearchResultsWithNullKeywords() {
    List<Track> result = underTest.search(new TrackSearch(null));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetEmptySearchResultsWithEmptyKeywords() {
    List<Track> result = underTest.search(new TrackSearch(" "));

    assertThat(result).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldGetEmptySearchResultsOnException() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptySearchResultsOnException()"))
        .when(indexSearcher).search(any(), anyInt(), any());

    List<Track> result = underTest.search(new TrackSearch("keywords"));

    assertThat(result).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldGetSearchResultsWhenExceptionThrownOnRelease() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    ScoreDoc[] scoreDocs = {new ScoreDoc(1, 0), new ScoreDoc(2, 0)};
    when(indexSearcher.search(any(), anyInt(), any()))
        .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
    setTrackSearcherDocuments(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldGetSearchResultsWhenExceptionThrownOnRelease()"))
        .when(trackManager).release(any());

    List<Track> result = underTest.search(new TrackSearch("keywords"));

    assertThat(result).hasSize(2);
  }

  @Test
  @SneakyThrows
  void shouldGetShuffledPlaylist() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);
    when(indexReader.maxDoc()).thenReturn(100);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
    setTrackSearcherDocuments(indexSearcher);

    setField(underTest, "executorService", Executors.newSingleThreadExecutor());
    setField(underTest, "secureRandom", new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

    List<Track> result = underTest.getShuffledPlaylist(3, null);

    assertThat(result).hasSize(3);

    Set<Track> uniqueResult = new HashSet<>(result);

    assertThat(uniqueResult).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldGetShuffledPlaylistWithYearFilter() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);
    when(indexReader.maxDoc()).thenReturn(100);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
    setTrackSearcherDocuments(indexSearcher);

    setField(underTest, "executorService", Executors.newSingleThreadExecutor());
    setField(underTest, "secureRandom", new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

    List<Track> result = underTest.getShuffledPlaylist(3, "2001");

    assertThat(result).hasSize(3);

    Set<Track> uniqueResult = new HashSet<>(result);

    assertThat(uniqueResult).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldGetShuffledPlaylistWhenExceptionThrownOnRelease() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);
    when(indexReader.maxDoc()).thenReturn(100);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
    setTrackSearcherDocuments(indexSearcher);

    setField(underTest, "executorService", Executors.newSingleThreadExecutor());
    setField(underTest, "secureRandom", new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

    doThrow(new RuntimeException("SearchManagerTest.shouldGetShuffledPlaylistWhenExceptionThrownOnRelease()"))
        .when(trackManager).release(any());

    List<Track> result = underTest.getShuffledPlaylist(3, null);

    assertThat(result).hasSize(3);

    Set<Track> uniqueResult = new HashSet<>(result);

    assertThat(uniqueResult).hasSize(3);
  }

  @Test
  @SneakyThrows
  void shouldGetMaxSizeShuffledPlaylistWhenPlaylistSizeGreaterOrEqualToNumberOfSearchResults() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);
    when(indexReader.maxDoc()).thenReturn(100);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
    setTrackSearcherDocuments(indexSearcher);

    List<Track> result = underTest.getShuffledPlaylist(9, null);

    assertThat(result).hasSize(9);

    Set<Track> uniqueResult = new HashSet<>(result);

    assertThat(uniqueResult).hasSize(9);
  }

  @Test
  void shouldFailToGetShuffledPlaylistIfTrackManagerIsNull() {
    setField(underTest, "trackManager", null);

    assertThatThrownBy(() -> underTest.getShuffledPlaylist(3, null)).isInstanceOf(RuntimeException.class);
  }

  @Test
  @SneakyThrows
  void shouldGetEmptyShuffledPlaylistOnException() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    IndexReader indexReader = mock(IndexReader.class);
    when(indexSearcher.getIndexReader()).thenReturn(indexReader);
    when(indexReader.maxDoc()).thenReturn(100);

    doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyShuffledPlaylistOnException()"))
        .when(indexSearcher).search(any(), anyInt());

    List<Track> result = underTest.getShuffledPlaylist(3, null);

    assertThat(result).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldGetTrackById() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    when(indexSearcher.search(any(), anyInt()))
        .thenReturn(new TopDocs(1, new ScoreDoc[]{new ScoreDoc(1, 0)}, 0));
    setTrackSearcherDocuments(indexSearcher);

    Track track = underTest.getTrackById("123").orElse(null);

    assertThat(track).isNotNull();
    assertThat(track.getArtistId()).isEqualTo("1231");
    assertThat(track.getArtistName()).isEqualTo("Artist Name 1");
    assertThat(track.getAlbumId()).isEqualTo("4561");
    assertThat(track.getAlbumName()).isEqualTo("Album Name 1");
    assertThat(track.getAlbumImage()).isEqualTo("Album Image 1");
    assertThat(track.getYear()).isEqualTo(2001);
    assertThat(track.getTrackId()).isEqualTo("7891");
    assertThat(track.getTrackName()).isEqualTo("Track Name 1");
    assertThat(track.getIndex()).isEqualTo(1);
    assertThat(track.getLocation()).isEqualTo("Location 1");
    assertThat(track.isPreferred()).isTrue();
    assertThat(track.getGenres()).hasSize(2);
    assertThat(track.getGenres().get(0)).isEqualTo("Genre 1 1");
    assertThat(track.getGenres().get(1)).isEqualTo("Genre 2 1");
  }

  @Test
  @SneakyThrows
  void shouldGetTrackByIdWhenExceptionThrownOnRelease() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    when(indexSearcher.search(any(), anyInt()))
        .thenReturn(new TopDocs(1, new ScoreDoc[]{new ScoreDoc(1, 0)}, 0));
    setTrackSearcherDocuments(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldGetTrackByIdWhenExceptionThrownOnRelease()"))
        .when(trackManager).release(any());

    Track track = underTest.getTrackById("123").orElse(null);

    assertThat(track).isNotNull();
    assertThat(track.getArtistId()).isEqualTo("1231");
    assertThat(track.getArtistName()).isEqualTo("Artist Name 1");
    assertThat(track.getAlbumId()).isEqualTo("4561");
    assertThat(track.getAlbumName()).isEqualTo("Album Name 1");
    assertThat(track.getAlbumImage()).isEqualTo("Album Image 1");
    assertThat(track.getYear()).isEqualTo(2001);
    assertThat(track.getTrackId()).isEqualTo("7891");
    assertThat(track.getTrackName()).isEqualTo("Track Name 1");
    assertThat(track.getIndex()).isEqualTo(1);
    assertThat(track.getLocation()).isEqualTo("Location 1");
    assertThat(track.isPreferred()).isTrue();
    assertThat(track.getGenres()).hasSize(2);
    assertThat(track.getGenres().get(0)).isEqualTo("Genre 1 1");
    assertThat(track.getGenres().get(1)).isEqualTo("Genre 2 1");
  }

  @Test
  @SneakyThrows
  void shouldFailToGetTrackByIdIfNoSearchResults() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[]{}, 0));

    Track track = underTest.getTrackById("123").orElse(null);

    assertThat(track).isNull();
  }

  @Test
  @SneakyThrows
  void shouldFailToGetTrackByIdOnException() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetTrackByIdOnException()")).when(indexSearcher)
        .search(any(), anyInt());

    Track track = underTest.getTrackById("123").orElse(null);

    assertThat(track).isNull();
  }

  @Test
  void shouldFailToGetTrackByIdIfArtistManagerIsNull() {
    setField(underTest, "trackManager", null);

    assertThatThrownBy(() -> underTest.getTrackById("123")).isInstanceOf(RuntimeException.class);
  }

  @Test
  @SneakyThrows
  void shouldGetAlbumById() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt(), any()))
        .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
    setTrackSearcherDocuments(indexSearcher);

    List<Track> tracks = underTest.getAlbumById("123").orElse(null);

    assertThat(tracks).hasSize(9);
  }

  @Test
  @SneakyThrows
  void shouldGetAlbumByIdWhenExceptionThrownOnRelease() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    List<ScoreDoc> scoreDocsList = new ArrayList<>();
    for (int i = 1; i < 10; i++) {
      scoreDocsList.add(new ScoreDoc(i, 0));
    }
    ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[0]);

    when(indexSearcher.search(any(), anyInt(), any()))
        .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
    setTrackSearcherDocuments(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldGetAlbumByIdWhenExceptionThrownOnRelease()"))
        .when(trackManager).release(any());

    List<Track> tracks = underTest.getAlbumById("123").orElse(null);

    assertThat(tracks).hasSize(9);
  }

  @Test
  @SneakyThrows
  void shouldGetEmptyAlbumByIdIfNoSearchResults() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    when(indexSearcher.search(any(), anyInt(), any())).thenReturn(new TopFieldDocs(0, new ScoreDoc[]{}, null, 0));

    List<Track> tracks = underTest.getAlbumById("123").orElse(null);

    assertThat(tracks).isEmpty();
  }

  @Test
  @SneakyThrows
  void shouldFailToGetAlbumByIdOnException() {
    IndexSearcher indexSearcher = mock(IndexSearcher.class);
    when(trackManager.acquire()).thenReturn(indexSearcher);

    doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetAlbumByIdOnException()")).when(indexSearcher)
        .search(any(), anyInt(), any());

    List<Track> tracks = underTest.getAlbumById("123").orElse(null);

    assertThat(tracks).isNull();
  }

  @Test
  void shouldFailToGetAlbumByIdIfArtistManagerIsNull() {
    setField(underTest, "trackManager", null);

    assertThatThrownBy(() -> underTest.getAlbumById("123")).isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldGetKeywords() {
    String keywords = "This is a sentence, with some punctuation! Don't worry about that punctuation. ";
    String result = underTest.prepareKeywords(keywords);

    assertThat(result).isEqualTo("this is a sentence with some punctuation dont worry about that punctuation");
  }

  @Test
  void shouldGetBlankKeywordsIfNull() {
    assertThat(underTest.prepareKeywords(null)).isEqualTo("");
  }

  @Test
  void shouldGetAsteriskKeywordsIfOnePassedIn() {
    assertThat(underTest.prepareKeywords("* ")).isEqualTo("*");
  }

  @SneakyThrows
  private void setTrackSearcherDocuments(IndexSearcher mockTrackSearcher) {
    for (int i = 1; i < 10; i++) {
      Document document = mock(Document.class);
      lenient().when(document.get(TrackField.ARTIST_ID.name())).thenReturn("123" + i);
      lenient().when(document.get(TrackField.ARTIST_NAME.name())).thenReturn("Artist Name " + i);
      lenient().when(document.get(TrackField.ALBUM_ID.name())).thenReturn("456" + i);
      lenient().when(document.get(TrackField.ALBUM_NAME.name())).thenReturn("Album Name " + i);
      lenient().when(document.get(TrackField.ALBUM_IMAGE.name())).thenReturn("Album Image " + i);
      lenient().when(document.get(TrackField.YEAR.name())).thenReturn("200" + i);
      lenient().when(document.get(TrackField.TRACK_ID.name())).thenReturn("789" + i);
      lenient().when(document.get(TrackField.TRACK_NAME.name())).thenReturn("Track Name " + i);
      lenient().when(document.get(TrackField.INDEX.name())).thenReturn(Integer.toString(i));
      lenient().when(document.get(TrackField.LOCATION.name())).thenReturn("Location " + i);
      lenient().when(document.get(TrackField.IS_PREFERRED.name())).thenReturn((i % 2 == 0) ? "false" : "true");
      lenient().when(document.getValues(TrackField.GENRE.name())).thenReturn(new String[]{"Genre 1 " + i, "Genre 2 " + i});

      lenient().when(mockTrackSearcher.doc(i)).thenReturn(document);
    }
  }
}