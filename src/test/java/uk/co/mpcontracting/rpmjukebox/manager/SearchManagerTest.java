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
    private EventManager eventManager;

    @Mock
    private AppProperties appProperties;

    @Mock
    private RpmJukebox rpmJukebox;

    @Mock
    private MessageManager messageManager;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private ApplicationManager applicationManager;

    @Mock
    private DataManager dataManager;

    @Mock
    private Directory artistDirectory;

    @Mock
    private IndexWriter artistWriter;

    @Mock
    private SearcherManager artistManager;

    @Mock
    private Directory trackDirectory;

    @Mock
    private IndexWriter trackWriter;

    @Mock
    private SearcherManager trackManager;

    private SearchManager underTest;

    @Before
    public void setup() {
        underTest = spy(new SearchManager(appProperties, rpmJukebox, messageManager));
        underTest.wireSettingsManager(settingsManager);
        underTest.wireApplicationManager(applicationManager);
        underTest.wireDataManager(dataManager);

        setField(underTest, "eventManager", eventManager);

        when(appProperties.getArtistIndexDirectory()).thenReturn("artistIndex");
        when(appProperties.getTrackIndexDirectory()).thenReturn("trackIndex");
        when(settingsManager.getFileFromConfigDirectory("artistIndex"))
                .thenReturn(new File(getConfigDirectory(), "artistIndex"));
        when(settingsManager.getFileFromConfigDirectory("trackIndex"))
                .thenReturn(new File(getConfigDirectory(), "trackIndex"));

        setField(underTest, "artistDirectory", artistDirectory);
        setField(underTest, "artistManager", artistManager);
        setField(underTest, "artistWriter", artistWriter);
        setField(underTest, "trackDirectory", trackDirectory);
        setField(underTest, "trackManager", trackManager);
        setField(underTest, "trackWriter", trackWriter);
    }

    @Test
    @SneakyThrows
    public void shouldInitialise() {
        try {
            doReturn(true).when(underTest).isIndexValid(any());
            doReturn(Collections.emptyList()).when(underTest).search(any());
            doReturn(Collections.emptyList()).when(underTest).getDistinctTrackFieldValues(any());
            when(settingsManager.hasDataFileExpired()).thenReturn(false);

            setField(underTest, "artistDirectory", null);
            setField(underTest, "artistManager", null);
            setField(underTest, "artistWriter", null);
            setField(underTest, "trackDirectory", null);
            setField(underTest, "trackManager", null);
            setField(underTest, "trackWriter", null);

            underTest.initialise();

            Directory artistDirectoryField = (Directory) getField(underTest, "artistDirectory");
            IndexWriter artistWriterField = (IndexWriter) getField(underTest, "artistWriter");
            SearcherManager artistManagerField = (SearcherManager) getField(underTest, "artistManager");
            Directory trackDirectoryField = (Directory) getField(underTest, "trackDirectory");
            IndexWriter trackWriterField = (IndexWriter) getField(underTest, "trackWriter");
            SearcherManager trackManagerField = (SearcherManager) getField(underTest, "trackManager");
            SecureRandom secureRandomField = (SecureRandom) getField(underTest, "random");
            ExecutorService executorServiceField = (ExecutorService) getField(underTest, "executorService");
            List<String> genreList = underTest.getGenreList();
            List<String> yearList = underTest.getYearList();
            List<TrackSort> trackSortList = underTest.getTrackSortList();

            assertThat(artistDirectoryField).isNotNull();
            assertThat(artistWriterField).isNotNull();
            assertThat(artistManagerField).isNotNull();
            assertThat(trackDirectoryField).isNotNull();
            assertThat(trackWriterField).isNotNull();
            assertThat(trackManagerField).isNotNull();
            assertThat(secureRandomField).isNotNull();
            assertThat(executorServiceField).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
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
    public void shouldInitialiseAndIndexWhenDataFileHasExpired() {
        try {
            doNothing().when(underTest).indexData();
            doReturn(Collections.emptyList()).when(underTest).search(any());
            doReturn(Collections.emptyList()).when(underTest).getDistinctTrackFieldValues(any());
            when(settingsManager.hasDataFileExpired()).thenReturn(true);

            setField(underTest, "artistDirectory", null);
            setField(underTest, "artistManager", null);
            setField(underTest, "artistWriter", null);
            setField(underTest, "trackDirectory", null);
            setField(underTest, "trackManager", null);
            setField(underTest, "trackWriter", null);

            underTest.initialise();

            Directory artistDirectoryField = (Directory) getField(underTest, "artistDirectory");
            IndexWriter artistWriterField = (IndexWriter) getField(underTest, "artistWriter");
            SearcherManager artistManagerField = (SearcherManager) getField(underTest, "artistManager");
            Directory trackDirectoryField = (Directory) getField(underTest, "trackDirectory");
            IndexWriter trackWriterField = (IndexWriter) getField(underTest, "trackWriter");
            SearcherManager trackManagerField = (SearcherManager) getField(underTest, "trackManager");
            SecureRandom secureRandomField = (SecureRandom) getField(underTest, "random");
            ExecutorService executorServiceField = (ExecutorService) getField(underTest, "executorService");
            List<String> genreList = underTest.getGenreList();
            List<String> yearList = underTest.getYearList();
            List<TrackSort> trackSortList = underTest.getTrackSortList();

            assertThat(artistDirectoryField).isNotNull();
            assertThat(artistWriterField).isNotNull();
            assertThat(artistManagerField).isNotNull();
            assertThat(trackDirectoryField).isNotNull();
            assertThat(trackWriterField).isNotNull();
            assertThat(trackManagerField).isNotNull();
            assertThat(secureRandomField).isNotNull();
            assertThat(executorServiceField).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
            assertThat(yearList).isEmpty();
            assertThat(trackSortList).hasSize(4);

            verify(underTest, times(1)).indexData();
            verify(underTest, times(9)).search(any());
        } finally {
            underTest.shutdown();
        }
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseAndIndexWhenIndexIsInvalid() {
        try {
            doNothing().when(underTest).indexData();
            doReturn(false).when(underTest).isIndexValid(any());
            doReturn(Collections.emptyList()).when(underTest).search(any());
            doReturn(Collections.emptyList()).when(underTest).getDistinctTrackFieldValues(any());
            when(settingsManager.hasDataFileExpired()).thenReturn(false);

            setField(underTest, "artistDirectory", null);
            setField(underTest, "artistManager", null);
            setField(underTest, "artistWriter", null);
            setField(underTest, "trackDirectory", null);
            setField(underTest, "trackManager", null);
            setField(underTest, "trackWriter", null);

            underTest.initialise();

            Directory artistDirectoryField = (Directory) getField(underTest, "artistDirectory");
            IndexWriter artistWriterField = (IndexWriter) getField(underTest, "artistWriter");
            SearcherManager artistManagerField = (SearcherManager) getField(underTest, "artistManager");
            Directory trackDirectoryField = (Directory) getField(underTest, "trackDirectory");
            IndexWriter trackWriterField = (IndexWriter) getField(underTest, "trackWriter");
            SearcherManager trackManagerField = (SearcherManager) getField(underTest, "trackManager");
            SecureRandom secureRandomField = (SecureRandom) getField(underTest, "random");
            ExecutorService executorServiceField = (ExecutorService) getField(underTest, "executorService");
            List<String> genreList = underTest.getGenreList();
            List<String> yearList = underTest.getYearList();
            List<TrackSort> trackSortList = underTest.getTrackSortList();

            assertThat(artistDirectoryField).isNotNull();
            assertThat(artistWriterField).isNotNull();
            assertThat(artistManagerField).isNotNull();
            assertThat(trackDirectoryField).isNotNull();
            assertThat(trackWriterField).isNotNull();
            assertThat(trackManagerField).isNotNull();
            assertThat(secureRandomField).isNotNull();
            assertThat(executorServiceField).isNotNull();
            assertThat(genreList).hasSize(1);
            assertThat(genreList.get(0)).isEqualTo(UNSPECIFIED_GENRE);
            assertThat(yearList).isEmpty();
            assertThat(trackSortList).hasSize(4);

            verify(underTest, times(1)).indexData();
            verify(underTest, times(9)).search(any());
        } finally {
            underTest.shutdown();
        }
    }

    @Test
    @SneakyThrows
    public void shouldNotInitialiseIfAlreadyInitialised() {
        try {
            doNothing().when(underTest).indexData();
            doReturn(false).when(underTest).isIndexValid(any());
            doReturn(Collections.emptyList()).when(underTest).search(any());
            doReturn(Collections.emptyList()).when(underTest).getDistinctTrackFieldValues(any());
            when(settingsManager.hasDataFileExpired()).thenReturn(false);

            setField(underTest, "artistDirectory", null);
            setField(underTest, "artistManager", null);
            setField(underTest, "artistWriter", null);
            setField(underTest, "trackDirectory", null);
            setField(underTest, "trackManager", null);
            setField(underTest, "trackWriter", null);

            underTest.initialise();
            underTest.initialise();

            verify(applicationManager, times(1)).shutdown();
        } finally {
            underTest.shutdown();
        }
    }

    @Test
    @SneakyThrows
    public void shouldThrowExceptionOnInitialise() {
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
    public void shouldReturnIndexValid() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);
        doNothing().when(artistManager).release(indexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(underTest).search(any());

        boolean isValid = underTest.isIndexValid(artistManager);

        assertThat(isValid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexValidWhenExceptionOnRelease() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexValidWhenExceptionOnRelease()"))
                .when(artistManager).release(indexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(underTest).search(any());

        boolean isValid = underTest.isIndexValid(artistManager);

        assertThat(isValid).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidWithEmptyTracks() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);
        doNothing().when(artistManager).release(indexSearcher);
        doReturn(Collections.emptyList()).when(underTest).search(any());

        boolean isValid = underTest.isIndexValid(artistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidWithNullTracks() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);
        doNothing().when(artistManager).release(indexSearcher);
        doReturn(null).when(underTest).search(any());

        boolean isValid = underTest.isIndexValid(artistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldReturnIndexInvalidOnException() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);
        doNothing().when(artistManager).release(indexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexInvalidOnException()")).when(underTest)
                .search(any());

        boolean isValid = underTest.isIndexValid(artistManager);

        assertThat(isValid).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldIndexData() {
        underTest.indexData();

        verify(dataManager, times(1)).parse(any());
        verify(artistWriter, times(1)).commit();
        verify(trackWriter, times(1)).commit();
        verify(artistManager, times(1)).maybeRefreshBlocking();
        verify(trackManager, times(1)).maybeRefreshBlocking();
        verify(settingsManager, times(1)).setLastIndexedDate(any());
        verify(eventManager, times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    @SneakyThrows
    public void shouldIndexDataButNotCommitOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldIndexDataButNotCommitOnException()"))
                .when(artistWriter).commit();

        underTest.indexData();

        verify(dataManager, times(1)).parse(any());
        verify(artistWriter, times(1)).commit();
        verify(trackWriter, never()).commit();
        verify(artistManager, never()).maybeRefreshBlocking();
        verify(trackManager, never()).maybeRefreshBlocking();
        verify(settingsManager, times(1)).setLastIndexedDate(any());
        verify(eventManager, times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    @SneakyThrows
    public void shouldAddArtist() {
        underTest.addArtist(generateArtist(1));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(artistWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getField(ArtistField.ARTIST_ID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTIST_ID.name()).stringValue()).isEqualTo("1231");
        assertThat(document.getValue().getField(ArtistField.ARTIST_NAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTIST_NAME.name()).stringValue()).isEqualTo("Artist Name 1");
        assertThat(document.getValue().getField(ArtistField.ARTIST_IMAGE.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.ARTIST_IMAGE.name()).stringValue()).isEqualTo("Artist Image 1");
        assertThat(document.getValue().getField(ArtistField.BIOGRAPHY.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.BIOGRAPHY.name()).stringValue()).isEqualTo("Biography 1");
        assertThat(document.getValue().getField(ArtistField.MEMBERS.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(ArtistField.MEMBERS.name()).stringValue()).isEqualTo("Members 1");
    }

    @Test
    @SneakyThrows
    public void shouldNotAddArtistOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddArtistOnException()")).when(artistWriter)
                .addDocument(any());

        underTest.addArtist(generateArtist(1));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(artistWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getFields()).hasSize(5);
    }

    @Test
    @SneakyThrows
    public void shouldAddTrack() {
        underTest.addTrack(generateTrack(1, "Genre 1", "Genre 2"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(trackWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.KEYWORDS.name()).stringValue()).isEqualTo("artist name 1 album name 1 track name 1");
        assertThat(document.getValue().getField(TrackField.ARTIST_ID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ARTIST_ID.name()).stringValue()).isEqualTo("1231");
        assertThat(document.getValue().getField(TrackField.ARTIST_NAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ARTIST_NAME.name()).stringValue()).isEqualTo("Artist Name 1");
        assertThat(document.getValue().getField(TrackField.ALBUM_ID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUM_ID.name()).stringValue()).isEqualTo("4561");
        assertThat(document.getValue().getField(TrackField.ALBUM_NAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUM_NAME.name()).stringValue()).isEqualTo("Album Name 1");
        assertThat(document.getValue().getField(TrackField.ALBUM_IMAGE.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.ALBUM_IMAGE.name()).stringValue()).isEqualTo("Album Image 1");
        assertThat(document.getValue().getField(TrackField.YEAR.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.YEAR.name()).stringValue()).isEqualTo("2001");
        assertThat(document.getValue().getField(TrackField.TRACK_ID.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.TRACK_ID.name()).stringValue()).isEqualTo("7891");
        assertThat(document.getValue().getField(TrackField.TRACK_NAME.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.TRACK_NAME.name()).stringValue()).isEqualTo("Track Name 1");
        assertThat(document.getValue().getField(TrackField.INDEX.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.INDEX.name()).stringValue()).isEqualTo("1");
        assertThat(document.getValue().getField(TrackField.LOCATION.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.LOCATION.name()).stringValue()).isEqualTo("Location 1");
        assertThat(document.getValue().getField(TrackField.IS_PREFERRED.name()).fieldType().stored()).isTrue();
        assertThat(document.getValue().getField(TrackField.IS_PREFERRED.name()).stringValue()).isEqualTo("true");

        assertThat(document.getValue().getFields(TrackField.GENRE.name())).hasSize(2);
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[0].fieldType().stored()).isTrue();
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[0].stringValue()).isEqualTo("Genre 1");
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[1].fieldType().stored()).isTrue();
        assertThat(document.getValue().getFields(TrackField.GENRE.name())[1].stringValue()).isEqualTo("Genre 2");

        assertThat(document.getValue().getBinaryValue(TrackSort.DEFAULT_SORT.name()).utf8ToString()).isEqualTo("ArtistName10000002001AlbumName10000000001");
        assertThat(document.getValue().getBinaryValue(TrackSort.ARTIST_SORT.name()).utf8ToString()).isEqualTo("0000002001ArtistName1");
        assertThat(document.getValue().getBinaryValue(TrackSort.ALBUM_SORT.name()).utf8ToString()).isEqualTo("0000002001AlbumName1");
        assertThat(document.getValue().getBinaryValue(TrackSort.TRACK_SORT.name()).utf8ToString()).isEqualTo("0000002001TrackName1");
    }

    @Test
    @SneakyThrows
    public void shouldNotAddTrackOnException() {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddTrackOnException()")).when(trackWriter)
                .addDocument(any());

        underTest.addTrack(generateTrack(1, "Genre 1", "Genre 2"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(trackWriter, times(1)).addDocument(document.capture());

        assertThat(document.getValue().getFields()).hasSize(18);
    }

    @Test
    @SneakyThrows
    public void shouldGetDistinctTrackFieldValues() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        IndexReader indexReader = mock(IndexReader.class);
        when(indexSearcher.getIndexReader()).thenReturn(indexReader);

        LeafReaderContext leafReaderContext = mock(LeafReaderContext.class);
        List<LeafReaderContext> leafReaderContexts = singletonList(leafReaderContext);
        when(indexReader.leaves()).thenReturn(leafReaderContexts);

        LeafReader leafReader = mock(LeafReader.class);
        when(leafReaderContext.reader()).thenReturn(leafReader);

        Iterator<BytesRef> bytesRefIterator = Arrays
                .asList(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
        Terms terms = mock(Terms.class);
        when(leafReader.terms(anyString())).thenReturn(terms);
        when(terms.iterator()).thenReturn(new TestTermsEnum(bytesRefIterator));

        List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo("Ref 1");
        assertThat(result.get(1)).isEqualTo("Ref 2");
    }

    @Test
    public void shouldFailToGetDistinctTrackFieldValuesIfTrackManagerIsNull() {
        setField(underTest, "trackManager", null);

        assertThatThrownBy(() -> underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyDistinctTrackFieldValuesWhenTermsNull() {
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
    public void shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown() {
        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown()"))
                .when(trackManager).acquire();

        List<String> result = underTest.getDistinctTrackFieldValues(TrackField.ALBUM_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease() {
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

        Iterator<BytesRef> bytesRefIterator = Arrays
                .asList(new BytesRef("Ref 1"), new BytesRef("Ref 1"), new BytesRef("Ref 2")).iterator();
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
    public void shouldGetSearchResults() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        ScoreDoc[] scoreDocs = {new ScoreDoc(1, 0), new ScoreDoc(2, 0)};
        when(indexSearcher.search(any(), anyInt(), any()))
                .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(indexSearcher);

        List<Track> result = underTest.search(new TrackSearch("keywords"));

        assertThat(result).hasSize(2);

        Track track1 = result.get(0);

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
    public void shouldFailToGetSearchResultsIfTrackManagerIsNull() {
        setField(underTest, "trackManager", null);

        assertThatThrownBy(() -> underTest.search(new TrackSearch("keywords"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullTrackSearch() {
        List<Track> result = underTest.search(null);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullKeywords() {
        List<Track> result = underTest.search(new TrackSearch(null));

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldGetEmptySearchResultsWithEmptyKeywords() {
        List<Track> result = underTest.search(new TrackSearch(" "));

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptySearchResultsOnException() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptySearchResultsOnException()"))
                .when(indexSearcher).search(any(), anyInt(), any());

        List<Track> result = underTest.search(new TrackSearch("keywords"));

        assertThat(result).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldGetSearchResultsWhenExceptionThrownOnRelease() {
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
    public void shouldGetShuffledPlaylist() {
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
        setField(underTest, "random",
                new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = underTest.getShuffledPlaylist(3, null);

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetShuffledPlaylistWithYearFilter() {
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
        setField(underTest, "random",
                new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = underTest.getShuffledPlaylist(3, "2001");

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetShuffledPlaylistWhenExceptionThrownOnRelease() {
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
        setField(underTest, "random",
                new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        doThrow(new RuntimeException("SearchManagerTest.shouldGetShuffledPlaylistWhenExceptionThrownOnRelease()"))
                .when(trackManager).release(any());

        List<Track> result = underTest.getShuffledPlaylist(3, null);

        assertThat(result).hasSize(3);

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat(uniqueResult).hasSize(3);
    }

    @Test
    @SneakyThrows
    public void shouldGetMaxSizeShuffledPlaylistWhenPlaylistSizeGreaterOrEqualToNumberOfSearchResults() {
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
    public void shouldFailToGetShuffledPlaylistIfTrackManagerIsNull() {
        setField(underTest, "trackManager", null);

        assertThatThrownBy(() -> underTest.getShuffledPlaylist(3, null)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetEmptyShuffledPlaylistOnException() {
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
    public void shouldGetArtistById() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);

        when(indexSearcher.search(any(), anyInt()))
                .thenReturn(new TopDocs(1, new ScoreDoc[]{new ScoreDoc(1, 0)}, 0));
        setArtistSearcherDocuments(indexSearcher);

        Artist artist = underTest.getArtistById("123").orElse(null);

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
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);

        when(indexSearcher.search(any(), anyInt()))
                .thenReturn(new TopDocs(1, new ScoreDoc[]{new ScoreDoc(1, 0)}, 0));
        setArtistSearcherDocuments(indexSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetArtistByIdWhenExceptionThrownOnRelease()"))
                .when(artistManager).release(any());

        Artist artist = underTest.getArtistById("123").orElse(null);

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
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);

        when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[]{}, 0));
        setArtistSearcherDocuments(indexSearcher);

        Artist artist = underTest.getArtistById("123").orElse(null);

        assertThat(artist).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetArtistByIdOnException() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(artistManager.acquire()).thenReturn(indexSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetArtistByIdOnException()"))
                .when(indexSearcher).search(any(), anyInt());

        Artist artist = underTest.getArtistById("123").orElse(null);

        assertThat(artist).isNull();
    }

    @Test
    public void shouldFailToGetArtistByIdIfArtistManagerIsNull() {
        setField(underTest, "artistManager", null);

        assertThatThrownBy(() -> underTest.getArtistById("123")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetTrackById() {
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
    public void shouldGetTrackByIdWhenExceptionThrownOnRelease() {
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
    public void shouldFailToGetTrackByIdIfNoSearchResults() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        when(indexSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[]{}, 0));
        setArtistSearcherDocuments(indexSearcher);

        Track track = underTest.getTrackById("123").orElse(null);

        assertThat(track).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetTrackByIdOnException() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetTrackByIdOnException()")).when(indexSearcher)
                .search(any(), anyInt());

        Track track = underTest.getTrackById("123").orElse(null);

        assertThat(track).isNull();
    }

    @Test
    public void shouldFailToGetTrackByIdIfArtistManagerIsNull() {
        setField(underTest, "trackManager", null);

        assertThatThrownBy(() -> underTest.getTrackById("123")).isInstanceOf(RuntimeException.class);
    }

    @Test
    @SneakyThrows
    public void shouldGetAlbumById() {
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
    public void shouldGetAlbumByIdWhenExceptionThrownOnRelease() {
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
    public void shouldGetEmptyAlbumByIdIfNoSearchResults() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        when(indexSearcher.search(any(), anyInt(), any()))
                .thenReturn(new TopFieldDocs(0, new ScoreDoc[]{}, null, 0));
        setArtistSearcherDocuments(indexSearcher);

        List<Track> tracks = underTest.getAlbumById("123").orElse(null);

        assertThat(tracks).isEmpty();
    }

    @Test
    @SneakyThrows
    public void shouldFailToGetAlbumByIdOnException() {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        when(trackManager.acquire()).thenReturn(indexSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetAlbumByIdOnException()")).when(indexSearcher)
                .search(any(), anyInt(), any());

        List<Track> tracks = underTest.getAlbumById("123").orElse(null);

        assertThat(tracks).isNull();
    }

    @Test
    public void shouldFailToGetAlbumByIdIfArtistManagerIsNull() {
        setField(underTest, "trackManager", null);

        assertThatThrownBy(() -> underTest.getAlbumById("123")).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldGetKeywords() {
        String keywords = "This is a sentence, with some punctuation! Don't worry about that punctuation. ";
        String result = underTest.prepareKeywords(keywords);

        assertThat(result).isEqualTo("this is a sentence with some punctuation dont worry about that punctuation");
    }

    @Test
    public void shouldGetBlankKeywordsIfNull() {
        assertThat(underTest.prepareKeywords(null)).isEqualTo("");
    }

    @Test
    public void shouldGetAsteriskKeywordsIfOnePassedIn() {
        assertThat(underTest.prepareKeywords("* ")).isEqualTo("*");
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }

    @SneakyThrows
    private void setArtistSearcherDocuments(IndexSearcher mockArtistSearcher) {
        for (int i = 1; i < 10; i++) {
            Document document = mock(Document.class);
            when(document.get(ArtistField.ARTIST_ID.name())).thenReturn("123" + i);
            when(document.get(ArtistField.ARTIST_NAME.name())).thenReturn("Artist Name " + i);
            when(document.get(ArtistField.ARTIST_IMAGE.name())).thenReturn("Artist Image " + i);
            when(document.get(ArtistField.BIOGRAPHY.name())).thenReturn("Biography " + i);
            when(document.get(ArtistField.MEMBERS.name())).thenReturn("Members " + i);

            when(mockArtistSearcher.doc(i)).thenReturn(document);
        }
    }

    @SneakyThrows
    private void setTrackSearcherDocuments(IndexSearcher mockTrackSearcher) {
        for (int i = 1; i < 10; i++) {
            Document document = mock(Document.class);
            when(document.get(TrackField.ARTIST_ID.name())).thenReturn("123" + i);
            when(document.get(TrackField.ARTIST_NAME.name())).thenReturn("Artist Name " + i);
            when(document.get(TrackField.ALBUM_ID.name())).thenReturn("456" + i);
            when(document.get(TrackField.ALBUM_NAME.name())).thenReturn("Album Name " + i);
            when(document.get(TrackField.ALBUM_IMAGE.name())).thenReturn("Album Image " + i);
            when(document.get(TrackField.YEAR.name())).thenReturn("200" + i);
            when(document.get(TrackField.TRACK_ID.name())).thenReturn("789" + i);
            when(document.get(TrackField.TRACK_NAME.name())).thenReturn("Track Name " + i);
            when(document.get(TrackField.INDEX.name())).thenReturn(Integer.toString(i));
            when(document.get(TrackField.LOCATION.name())).thenReturn("Location " + i);
            when(document.get(TrackField.IS_PREFERRED.name())).thenReturn((i % 2 == 0) ? "false" : "true");
            when(document.getValues(TrackField.GENRE.name()))
                    .thenReturn(new String[]{"Genre 1 " + i, "Genre 2 " + i});

            when(mockTrackSearcher.doc(i)).thenReturn(document);
        }
    }
}
