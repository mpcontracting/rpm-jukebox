package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.ArtistField;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.TestTermsEnum;

import java.io.File;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

@RunWith(MockitoJUnitRunner.class)
public class SearchManagerTest implements Constants {

    @Mock
    private EventManager mockEventManager;

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private RpmJukebox mockRpmJukebox;

    @Mock
    private MessageManager mockMessageManager;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private ApplicationManager mockApplicationManager;

    @Mock
    private DataManager mockDataManager;

    @Mock
    private Directory mockArtistDirectory;

    @Mock
    private IndexWriter mockArtistWriter;

    @Mock
    private SearcherManager mockArtistManager;

    @Mock
    private Directory mockTrackDirectory;

    @Mock
    private IndexWriter mockTrackWriter;

    @Mock
    private SearcherManager mockTrackManager;

    private SearchManager spySearchManager;

    @Before
    public void setup() {
        spySearchManager = spy(new SearchManager(mockAppProperties, mockRpmJukebox, mockMessageManager));
        spySearchManager.wireSettingsManager(mockSettingsManager);
        spySearchManager.wireApplicationManager(mockApplicationManager);
        spySearchManager.wireDataManager(mockDataManager);

        setField(spySearchManager, "eventManager", mockEventManager);

        when(mockAppProperties.getArtistIndexDirectory()).thenReturn("artistIndex");
        when(mockAppProperties.getTrackIndexDirectory()).thenReturn("trackIndex");
        when(mockSettingsManager.getFileFromConfigDirectory("artistIndex"))
                .thenReturn(new File(getConfigDirectory(), "artistIndex"));
        when(mockSettingsManager.getFileFromConfigDirectory("trackIndex"))
                .thenReturn(new File(getConfigDirectory(), "trackIndex"));

        setField(spySearchManager, "artistDirectory", mockArtistDirectory);
        setField(spySearchManager, "artistManager", mockArtistManager);
        setField(spySearchManager, "artistWriter", mockArtistWriter);
        setField(spySearchManager, "trackDirectory", mockTrackDirectory);
        setField(spySearchManager, "trackManager", mockTrackManager);
        setField(spySearchManager, "trackWriter", mockTrackWriter);
    }

    @Test
    @SneakyThrows
    public void shouldInitialise() throws Exception {
        try {
            doReturn(true).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(false);

            setField(spySearchManager, "artistDirectory", null);
            setField(spySearchManager, "artistManager", null);
            setField(spySearchManager, "artistWriter", null);
            setField(spySearchManager, "trackDirectory", null);
            setField(spySearchManager, "trackManager", null);
            setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Directory artistDirectory = (Directory) getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter) getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager) getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory) getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter) getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager) getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom) getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService) getField(spySearchManager, "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat(artistDirectory).isNotNull();
            assertThat(artistWriter).isNotNull();
            assertThat(artistManager).isNotNull();
            assertThat(trackDirectory).isNotNull();
            assertThat(trackWriter).isNotNull();
            assertThat(trackManager).isNotNull();
            assertThat(secureRandom).isNotNull();
            assertThat(executorService).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
            assertThat(yearList).isEmpty();
            assertThat(trackSortList).hasSize(4);

            verify(spySearchManager, never()).indexData();
            verify(spySearchManager, times(9)).search(any());
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    public void shouldInitialiseAndIndexWhenDataFileHasExpired() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData();
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(true);

            setField(spySearchManager, "artistDirectory", null);
            setField(spySearchManager, "artistManager", null);
            setField(spySearchManager, "artistWriter", null);
            setField(spySearchManager, "trackDirectory", null);
            setField(spySearchManager, "trackManager", null);
            setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Directory artistDirectory = (Directory) getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter) getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager) getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory) getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter) getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager) getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom) getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService) getField(spySearchManager, "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat(artistDirectory).isNotNull();
            assertThat(artistWriter).isNotNull();
            assertThat(artistManager).isNotNull();
            assertThat(trackDirectory).isNotNull();
            assertThat(trackWriter).isNotNull();
            assertThat(trackManager).isNotNull();
            assertThat(secureRandom).isNotNull();
            assertThat(executorService).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
            assertThat(yearList).isEmpty();
            assertThat(trackSortList).hasSize(4);

            verify(spySearchManager, times(1)).indexData();
            verify(spySearchManager, times(9)).search(any());
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    public void shouldInitialiseAndIndexWhenIndexIsInvalid() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData();
            doReturn(false).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(false);

            setField(spySearchManager, "artistDirectory", null);
            setField(spySearchManager, "artistManager", null);
            setField(spySearchManager, "artistWriter", null);
            setField(spySearchManager, "trackDirectory", null);
            setField(spySearchManager, "trackManager", null);
            setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Directory artistDirectory = (Directory) getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter) getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager) getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory) getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter) getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager) getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom) getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService) getField(spySearchManager, "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat(artistDirectory).isNotNull();
            assertThat(artistWriter).isNotNull();
            assertThat(artistManager).isNotNull();
            assertThat(trackDirectory).isNotNull();
            assertThat(trackWriter).isNotNull();
            assertThat(trackManager).isNotNull();
            assertThat(secureRandom).isNotNull();
            assertThat(executorService).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
            assertThat(yearList).isEmpty();
            assertThat(trackSortList).hasSize(4);

            verify(spySearchManager, times(1)).indexData();
            verify(spySearchManager, times(9)).search(any());
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    public void shouldNotInitialiseIfAlreadyInitialised() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData();
            doReturn(false).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(false);

            setField(spySearchManager, "artistDirectory", null);
            setField(spySearchManager, "artistManager", null);
            setField(spySearchManager, "artistWriter", null);
            setField(spySearchManager, "trackDirectory", null);
            setField(spySearchManager, "trackManager", null);
            setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();
            spySearchManager.initialise();

            verify(mockApplicationManager, times(1)).shutdown();
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    public void shouldThrowExceptionOnInitialise() throws Exception {
        try {
            doThrow(new RuntimeException("SearchManagerTest.shouldThrowExceptionOnInitialise()")).when(spySearchManager)
                .indexData();
            doReturn(false).when(spySearchManager).isIndexValid(any());

            assertThatThrownBy(() -> spySearchManager.initialise()).isInstanceOf(RuntimeException.class);
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexValid() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat(isValid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexValidWhenExceptionOnRelease() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexValidWhenExceptionOnRelease()"))
            .when(mockArtistManager).release(mockIndexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat(isValid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidWithEmptyTracks() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doReturn(Collections.emptyList()).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidWithNullTracks() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doReturn(null).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidOnException() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexInvalidOnException()")).when(spySearchManager)
            .search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldIndexData() {
        spySearchManager.indexData();

        verify(mockDataManager, times(1)).parse(any());
        verify(mockArtistWriter, times(1)).commit();
        verify(mockTrackWriter, times(1)).commit();
        verify(mockArtistManager, times(1)).maybeRefreshBlocking();
        verify(mockTrackManager, times(1)).maybeRefreshBlocking();
        verify(mockSettingsManager, times(1)).setLastIndexedDate(any());
        verify(mockEventManager, times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    @SneakyThrows
    public void shouldIndexDataButNotCommitOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldIndexDataButNotCommitOnException()"))
            .when(mockArtistWriter).commit();

        spySearchManager.indexData();

        verify(mockDataManager, times(1)).parse(any());
        verify(mockArtistWriter, times(1)).commit();
        verify(mockTrackWriter, never()).commit();
        verify(mockArtistManager, never()).maybeRefreshBlocking();
        verify(mockTrackManager, never()).maybeRefreshBlocking();
        verify(mockSettingsManager, times(1)).setLastIndexedDate(any());
        verify(mockEventManager, times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    @SneakyThrows
    public void shouldAddArtist() {
        spySearchManager.addArtist(generateArtist(1));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getField(ArtistField.ARTISTID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTISTID.name()).stringValue()).isEqualTo("1231");
        assertThat(document.getValue().getField(ArtistField.ARTISTNAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTISTNAME.name()).stringValue()).isEqualTo("Artist Name 1");
        assertThat(document.getValue().getField(ArtistField.ARTISTIMAGE.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTISTIMAGE.name()).stringValue()).isEqualTo("Artist Image 1");
        assertThat(document.getValue().getField(ArtistField.BIOGRAPHY.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.BIOGRAPHY.name()).stringValue()).isEqualTo("Biography 1");
        assertThat(document.getValue().getField(ArtistField.MEMBERS.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.MEMBERS.name()).stringValue()).isEqualTo("Members 1");
    }

    @Test
    @SneakyThrows
    public void shouldNotAddArtistOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddArtistOnException()")).when(mockArtistWriter)
            .addDocument(any());

        spySearchManager.addArtist(generateArtist(1));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getFields()).hasSize(5);
    }

    @Test
    @SneakyThrows
    public void shouldAddTrack() {
        spySearchManager.addTrack(generateTrack(1, "Genre 1", "Genre 2"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockTrackWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).stringValue()).isEqualTo("artist name 1 album name 1 track name 1");
        assertThat(document.getValue().getField(TrackField.ARTISTID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ARTISTID.name()).stringValue()).isEqualTo("1231");
        assertThat(document.getValue().getField(TrackField.ARTISTNAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ARTISTNAME.name()).stringValue()).isEqualTo("Artist Name 1");
        assertThat(document.getValue().getField(TrackField.ARTISTIMAGE.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ARTISTIMAGE.name()).stringValue()).isEqualTo("Artist Image 1");
        assertThat(document.getValue().getField(TrackField.ALBUMID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUMID.name()).stringValue()).isEqualTo("4561");
        assertThat(document.getValue().getField(TrackField.ALBUMNAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUMNAME.name()).stringValue()).isEqualTo("Album Name 1");
        assertThat(document.getValue().getField(TrackField.ALBUMIMAGE.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUMIMAGE.name()).stringValue()).isEqualTo("Album Image 1");
        assertThat(document.getValue().getField(TrackField.YEAR.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.YEAR.name()).stringValue()).isEqualTo("2001");
        assertThat(document.getValue().getField(TrackField.TRACKID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.TRACKID.name()).stringValue()).isEqualTo("7891");
        assertThat(document.getValue().getField(TrackField.TRACKNAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.TRACKNAME.name()).stringValue()).isEqualTo("Track Name 1");
        assertThat(document.getValue().getField(TrackField.NUMBER.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.NUMBER.name()).stringValue()).isEqualTo("1");
        assertThat(document.getValue().getField(TrackField.LOCATION.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.LOCATION.name()).stringValue()).isEqualTo("Location 1");
        assertThat(document.getValue().getField(TrackField.ISPREFERRED.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ISPREFERRED.name()).stringValue()).isEqualTo("true");

        assertThat(document.getValue().getFields(TrackField.GENRE.name())).hasSize(2);
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[0].fieldType().stored()).isTrue();
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[0].stringValue()).isEqualTo("Genre 1");
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[1].fieldType().stored()).isTrue();
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[1].stringValue()).isEqualTo("Genre 2");

        assertThat(document.getValue().getBinaryValue(TrackSort.DEFAULTSORT.name()).utf8ToString()).isEqualTo("ArtistName10000002001AlbumName10000000001");
        assertThat(document.getValue().getBinaryValue(TrackSort.ARTISTSORT.name()).utf8ToString()).isEqualTo("0000002001ArtistName1");
        assertThat(document.getValue().getBinaryValue(TrackSort.ALBUMSORT.name()).utf8ToString()).isEqualTo("0000002001AlbumName1");
        assertThat(document.getValue().getBinaryValue(TrackSort.TRACKSORT.name()).utf8ToString()).isEqualTo("0000002001TrackName1");
    }

    @Test
    @SneakyThrows
    public void shouldNotAddTrackOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddTrackOnException()")).when(mockTrackWriter)
            .addDocument(any());

        spySearchManager.addTrack(generateTrack(1, "Genre 1", "Genre 2"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockTrackWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getFields()).hasSize(19);
    }

    @Test
    @SneakyThrows
    public void shouldGetDistinctTrackFieldValues() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockIndexSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockIndexSearcher.getIndexReader()).thenReturn(mockIndexReader);

        LeafReaderContext mockLeafReaderContext = mock(LeafReaderContext.class);
        List<LeafReaderContext> mockLeafReaderContexts = singletonList(mockLeafReaderContext);
        when(mockIndexReader.leaves()).thenReturn(mockLeafReaderContexts);

        LeafReader mockLeafReader = mock(LeafReader.class);
        when(mockLeafReaderContext.reader()).thenReturn(mockLeafReader);

        Iterator<BytesRef> bytesRefIterator = Arrays
            .asList(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
        Terms mockTerms = mock(Terms.class);
        when(mockLeafReader.terms(anyString())).thenReturn(mockTerms);
        when(mockTerms.iterator()).thenReturn(new TestTermsEnum(bytesRefIterator));

        List<String> result = spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("Ref 1");
        assertThat(result.get(1)).isEqualTo("Ref 2");
    }

    @Test
    public void shouldFailToGetDistinctTrackFieldValuesIfTrackManagerIsNull() {
        setField(spySearchManager, "trackManager", null);

        assertThatThrownBy(() -> spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyDistinctTrackFieldValuesWhenTermsNull() {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockIndexSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockIndexSearcher.getIndexReader()).thenReturn(mockIndexReader);

        LeafReaderContext mockLeafReaderContext = mock(LeafReaderContext.class);
        List<LeafReaderContext> mockLeafReaderContexts = Arrays.asList(mockLeafReaderContext);
        when(mockIndexReader.leaves()).thenReturn(mockLeafReaderContexts);

        LeafReader mockLeafReader = mock(LeafReader.class);
        when(mockLeafReaderContext.reader()).thenReturn(mockLeafReader);
        when(mockLeafReader.terms(anyString())).thenReturn(null);

        List<String> result = spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown() {
        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown()"))
            .when(mockTrackManager).acquire();

        List<String> result = spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease() {
        doThrow(new RuntimeException("SearchManagerTest.shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockIndexSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockIndexSearcher.getIndexReader()).thenReturn(mockIndexReader);

        LeafReaderContext mockLeafReaderContext = mock(LeafReaderContext.class);
        List<LeafReaderContext> mockLeafReaderContexts = Arrays.asList(mockLeafReaderContext);
        when(mockIndexReader.leaves()).thenReturn(mockLeafReaderContexts);

        LeafReader mockLeafReader = mock(LeafReader.class);
        when(mockLeafReaderContext.reader()).thenReturn(mockLeafReader);

        Iterator<BytesRef> bytesRefIterator = Arrays
            .asList(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
        Terms mockTerms = mock(Terms.class);
        when(mockLeafReader.terms(anyString())).thenReturn(mockTerms);
        when(mockTerms.iterator()).thenReturn(new TestTermsEnum(bytesRefIterator));

        List<String> result = spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("Ref 1");
        assertThat(result.get(1)).isEqualTo("Ref 2");
    }

    @Test
    @SneakyThrows
    public void shouldGetSearchResults() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        ScoreDoc[] scoreDocs = { new ScoreDoc(1, 0), new ScoreDoc(2, 0) };
        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat(result).hasSize(2);

        Track track1 = result.get(0);

        assertThat(track1.getArtistId()).isEqualTo("1231");
        assertThat(track1.getArtistName()).isEqualTo("Artist Name 1");
        assertThat(track1.getArtistImage()).isEqualTo("Artist Image 1");
        assertThat(track1.getAlbumId()).isEqualTo("4561");
        assertThat(track1.getAlbumName()).isEqualTo("Album Name 1");
        assertThat(track1.getAlbumImage()).isEqualTo("Album Image 1");
        assertThat(track1.getYear()).isEqualTo(2001);
        assertThat(track1.getTrackId()).isEqualTo("7891");
        assertThat(track1.getTrackName()).isEqualTo("Track Name 1");
        assertThat(track1.getNumber()).isEqualTo(1);
        assertThat(track1.getLocation()).isEqualTo("Location 1");
        assertThat(track1.isPreferred()).isTrue();
        assertThat(track1.getGenres()).hasSize(2);
        assertThat(track1.getGenres().get(0)).isEqualTo("Genre 1 1");
        assertThat(track1.getGenres().get(1)).isEqualTo("Genre 2 1");

        Track track2 = result.get(1);

        assertThat(track2.getArtistId()).isEqualTo("1232");
        assertThat(track2.getArtistName()).isEqualTo("Artist Name 2");
        assertThat(track2.getArtistImage()).isEqualTo("Artist Image 2");
        assertThat(track2.getAlbumId()).isEqualTo("4562");
        assertThat(track2.getAlbumName()).isEqualTo("Album Name 2");
        assertThat(track2.getAlbumImage()).isEqualTo("Album Image 2");
        assertThat(track2.getYear()).isEqualTo(2002);
        assertThat(track2.getTrackId()).isEqualTo("7892");
        assertThat(track2.getTrackName()).isEqualTo("Track Name 2");
        assertThat(track2.getNumber()).isEqualTo(2);
        assertThat(track2.getLocation()).isEqualTo("Location 2");
        assertThat(track2.isPreferred()).isFalse();
        assertThat(track2.getGenres()).hasSize(2);
        assertThat(track2.getGenres().get(0)).isEqualTo("Genre 1 2");
        assertThat(track2.getGenres().get(1)).isEqualTo("Genre 2 2");
    }

    @Test
    public void shouldFailToGetSearchResultsIfTrackManagerIsNull() {
        setField(spySearchManager, "trackManager", null);

        assertThatThrownBy(() -> spySearchManager.search(new TrackSearch("keywords"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullTrackSearch() {
        List<Track> result = spySearchManager.search(null);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullKeywords() {
        List<Track> result = spySearchManager.search(new TrackSearch(null));

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldGetEmptySearchResultsWithEmptyKeywords() {
        List<Track> result = spySearchManager.search(new TrackSearch(" "));

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptySearchResultsOnException() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptySearchResultsOnException()"))
            .when(mockTrackSearcher).search(any(), anyInt(), any());

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetSearchResultsWhenExceptionThrownOnRelease() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        ScoreDoc[] scoreDocs = { new ScoreDoc(1, 0), new ScoreDoc(2, 0) };
        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetSearchResultsWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat(result).hasSize(2);
    }

    @Test
    @SneakyThrows
    public void shouldGetShuffledPlaylist() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockTrackSearcher.getIndexReader()).thenReturn(mockIndexReader);
        when(mockIndexReader.maxDoc()).thenReturn(100);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetShuffledPlaylistWithYearFilter() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockTrackSearcher.getIndexReader()).thenReturn(mockIndexReader);
        when(mockIndexReader.maxDoc()).thenReturn(100);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = spySearchManager.getShuffledPlaylist(3, "2001");

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetShuffledPlaylistWhenExceptionThrownOnRelease() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockTrackSearcher.getIndexReader()).thenReturn(mockIndexReader);
        when(mockIndexReader.maxDoc()).thenReturn(100);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        doThrow(new RuntimeException("SearchManagerTest.shouldGetShuffledPlaylistWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetMaxSizeShuffledPlaylistWhenPlaylistSizeGreaterOrEqualToNumberOfSearchResults() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockTrackSearcher.getIndexReader()).thenReturn(mockIndexReader);
        when(mockIndexReader.maxDoc()).thenReturn(100);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(scoreDocs.length, scoreDocs, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        List<Track> result = spySearchManager.getShuffledPlaylist(9, null);

        assertThat(result).hasSize(9);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(9);
    }

    @Test
    public void shouldFailToGetShuffledPlaylistIfTrackManagerIsNull() {
        setField(spySearchManager, "trackManager", null);

        assertThatThrownBy(() -> spySearchManager.getShuffledPlaylist(3, null)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyShuffledPlaylistOnException() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        IndexReader mockIndexReader = mock(IndexReader.class);
        when(mockTrackSearcher.getIndexReader()).thenReturn(mockIndexReader);
        when(mockIndexReader.maxDoc()).thenReturn(100);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyShuffledPlaylistOnException()"))
            .when(mockTrackSearcher).search(any(), anyInt());

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetArtistById() {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        Artist artist = spySearchManager.getArtistById("123").orElse(null);

        assertThat(artist).isNotNull();
        assertThat(artist.getArtistId()).isEqualTo("1231");
        assertThat(artist.getArtistName()).isEqualTo("Artist Name 1");
        assertThat(artist.getArtistImage()).isEqualTo("Artist Image 1");
        assertThat(artist.getBiography()).isEqualTo("Biography 1");
        assertThat(artist.getMembers()).isEqualTo("Members 1");
    }

    @Test
    @SneakyThrows
    public void shouldGetArtistByIdWhenExceptionThrownOnRelease() {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetArtistByIdWhenExceptionThrownOnRelease()"))
            .when(mockArtistManager).release(any());

        Artist artist = spySearchManager.getArtistById("123").orElse(null);

        assertThat(artist).isNotNull();
        assertThat(artist.getArtistId()).isEqualTo("1231");
        assertThat(artist.getArtistName()).isEqualTo("Artist Name 1");
        assertThat(artist.getArtistImage()).isEqualTo("Artist Image 1");
        assertThat(artist.getBiography()).isEqualTo("Biography 1");
        assertThat(artist.getMembers()).isEqualTo("Members 1");
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetArtistByIdIfNoSearchResults() {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[] {}, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        Artist artist = spySearchManager.getArtistById("123").orElse(null);

        assertThat(artist).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetArtistByIdOnException() {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetArtistByIdOnException()"))
            .when(mockArtistSearcher).search(any(), anyInt());

        Artist artist = spySearchManager.getArtistById("123").orElse(null);

        assertThat(artist).isNull();
    }

    @Test
    public void shouldFailToGetArtistByIdIfArtistManagerIsNull() {
        setField(spySearchManager, "artistManager", null);

        assertThatThrownBy(() -> spySearchManager.getArtistById("123")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetTrackById() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        Track track = spySearchManager.getTrackById("123").orElse(null);

        assertThat(track).isNotNull();
        assertThat(track.getArtistId()).isEqualTo("1231");
        assertThat(track.getArtistName()).isEqualTo("Artist Name 1");
        assertThat(track.getArtistImage()).isEqualTo("Artist Image 1");
        assertThat(track.getAlbumId()).isEqualTo("4561");
        assertThat(track.getAlbumName()).isEqualTo("Album Name 1");
        assertThat(track.getAlbumImage()).isEqualTo("Album Image 1");
        assertThat(track.getYear()).isEqualTo(2001);
        assertThat(track.getTrackId()).isEqualTo("7891");
        assertThat(track.getTrackName()).isEqualTo("Track Name 1");
        assertThat(track.getNumber()).isEqualTo(1);
        assertThat(track.getLocation()).isEqualTo("Location 1");
        assertThat(track.isPreferred()).isTrue();
        assertThat(track.getGenres()).hasSize(2);
        assertThat(track.getGenres().get(0)).isEqualTo("Genre 1 1");
        assertThat(track.getGenres().get(1)).isEqualTo("Genre 2 1");
    }

    @Test
    @SneakyThrows
    public void shouldGetTrackByIdWhenExceptionThrownOnRelease() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetTrackByIdWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        Track track = spySearchManager.getTrackById("123").orElse(null);

        assertThat(track).isNotNull();
        assertThat(track.getArtistId()).isEqualTo("1231");
        assertThat(track.getArtistName()).isEqualTo("Artist Name 1");
        assertThat(track.getArtistImage()).isEqualTo("Artist Image 1");
        assertThat(track.getAlbumId()).isEqualTo("4561");
        assertThat(track.getAlbumName()).isEqualTo("Album Name 1");
        assertThat(track.getAlbumImage()).isEqualTo("Album Image 1");
        assertThat(track.getYear()).isEqualTo(2001);
        assertThat(track.getTrackId()).isEqualTo("7891");
        assertThat(track.getTrackName()).isEqualTo("Track Name 1");
        assertThat(track.getNumber()).isEqualTo(1);
        assertThat(track.getLocation()).isEqualTo("Location 1");
        assertThat(track.isPreferred()).isTrue();
        assertThat(track.getGenres()).hasSize(2);
        assertThat(track.getGenres().get(0)).isEqualTo("Genre 1 1");
        assertThat(track.getGenres().get(1)).isEqualTo("Genre 2 1");
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetTrackByIdIfNoSearchResults() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[] {}, 0));
        setArtistSearcherDocuments(mockTrackSearcher);

        Track track = spySearchManager.getTrackById("123").orElse(null);

        assertThat(track).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetTrackByIdOnException() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetTrackByIdOnException()")).when(mockTrackSearcher)
            .search(any(), anyInt());

        Track track = spySearchManager.getTrackById("123").orElse(null);

        assertThat(track).isNull();
    }

    @Test
    public void shouldFailToGetTrackByIdIfArtistManagerIsNull() {
        setField(spySearchManager, "trackManager", null);

        assertThatThrownBy(() -> spySearchManager.getTrackById("123")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetAlbumById() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat(tracks).hasSize(9);
    }

    @Test
    @SneakyThrows
    public void shouldGetAlbumByIdWhenExceptionThrownOnRelease() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        List<ScoreDoc> scoreDocsList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            scoreDocsList.add(new ScoreDoc(i, 0));
        }
        ScoreDoc[] scoreDocs = scoreDocsList.toArray(new ScoreDoc[scoreDocsList.size()]);

        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetAlbumByIdWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat(tracks).hasSize(9);
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyAlbumByIdIfNoSearchResults() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(0, new ScoreDoc[] {}, null, 0));
        setArtistSearcherDocuments(mockTrackSearcher);

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat(tracks).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetAlbumByIdOnException() {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetAlbumByIdOnException()")).when(mockTrackSearcher)
            .search(any(), anyInt(), any());

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat(tracks).isNull();
    }

    @Test
    public void shouldFailToGetAlbumByIdIfArtistManagerIsNull() {
        setField(spySearchManager, "trackManager", null);

        assertThatThrownBy(() -> spySearchManager.getAlbumById("123")).isInstanceOf(RuntimeException.class);
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }

    private void setArtistSearcherDocuments(IndexSearcher mockArtistSearcher) throws Exception {
        for (int i = 1; i < 10; i++) {
            Document document = mock(Document.class);
            when(document.get(ArtistField.ARTISTID.name())).thenReturn("123" + i);
            when(document.get(ArtistField.ARTISTNAME.name())).thenReturn("Artist Name " + i);
            when(document.get(ArtistField.ARTISTIMAGE.name())).thenReturn("Artist Image " + i);
            when(document.get(ArtistField.BIOGRAPHY.name())).thenReturn("Biography " + i);
            when(document.get(ArtistField.MEMBERS.name())).thenReturn("Members " + i);

            when(mockArtistSearcher.doc(i)).thenReturn(document);
        }
    }

    private void setTrackSearcherDocuments(IndexSearcher mockTrackSearcher) throws Exception {
        for (int i = 1; i < 10; i++) {
            Document document = mock(Document.class);
            when(document.get(TrackField.ARTISTID.name())).thenReturn("123" + i);
            when(document.get(TrackField.ARTISTNAME.name())).thenReturn("Artist Name " + i);
            when(document.get(TrackField.ARTISTIMAGE.name())).thenReturn("Artist Image " + i);
            when(document.get(TrackField.ALBUMID.name())).thenReturn("456" + i);
            when(document.get(TrackField.ALBUMNAME.name())).thenReturn("Album Name " + i);
            when(document.get(TrackField.ALBUMIMAGE.name())).thenReturn("Album Image " + i);
            when(document.get(TrackField.YEAR.name())).thenReturn("200" + i);
            when(document.get(TrackField.TRACKID.name())).thenReturn("789" + i);
            when(document.get(TrackField.TRACKNAME.name())).thenReturn("Track Name " + i);
            when(document.get(TrackField.NUMBER.name())).thenReturn(Integer.toString(i));
            when(document.get(TrackField.LOCATION.name())).thenReturn("Location " + i);
            when(document.get(TrackField.ISPREFERRED.name())).thenReturn((i % 2 == 0) ? "false" : "true");
            when(document.getValues(TrackField.GENRE.name()))
                .thenReturn(new String[] { "Genre 1 " + i, "Genre 2 " + i });

            when(mockTrackSearcher.doc(i)).thenReturn(document);
        }
    }
}
