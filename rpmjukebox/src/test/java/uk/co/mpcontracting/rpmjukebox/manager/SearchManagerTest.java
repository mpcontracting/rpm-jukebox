package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.ArtistField;
import uk.co.mpcontracting.rpmjukebox.search.TrackField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSearch;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.test.support.TestTermsEnum;

public class SearchManagerTest extends AbstractTest implements Constants {

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private SettingsManager settingsManager;

    @Value("${directory.artist.index}")
    private String directoryArtistIndex;

    @Value("${directory.track.index}")
    private String directoryTrackIndex;

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
        spySearchManager = spy(searchManager);
        ReflectionTestUtils.setField(spySearchManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spySearchManager, "settingsManager", mockSettingsManager);
        ReflectionTestUtils.setField(spySearchManager, "applicationManager", mockApplicationManager);
        ReflectionTestUtils.setField(spySearchManager, "dataManager", mockDataManager);

        when(mockSettingsManager.getFileFromConfigDirectory(directoryArtistIndex))
            .thenReturn(settingsManager.getFileFromConfigDirectory(directoryArtistIndex));
        when(mockSettingsManager.getFileFromConfigDirectory(directoryTrackIndex))
            .thenReturn(settingsManager.getFileFromConfigDirectory(directoryTrackIndex));

        ReflectionTestUtils.setField(spySearchManager, "artistDirectory", mockArtistDirectory);
        ReflectionTestUtils.setField(spySearchManager, "artistManager", mockArtistManager);
        ReflectionTestUtils.setField(spySearchManager, "artistWriter", mockArtistWriter);
        ReflectionTestUtils.setField(spySearchManager, "trackDirectory", mockTrackDirectory);
        ReflectionTestUtils.setField(spySearchManager, "trackManager", mockTrackManager);
        ReflectionTestUtils.setField(spySearchManager, "trackWriter", mockTrackWriter);
    }

    @Test
    public void shouldInitialise() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData();
            doReturn(true).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(false);

            ReflectionTestUtils.setField(spySearchManager, "artistDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "artistManager", null);
            ReflectionTestUtils.setField(spySearchManager, "artistWriter", null);
            ReflectionTestUtils.setField(spySearchManager, "trackDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "trackManager", null);
            ReflectionTestUtils.setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Analyzer analyzer = (Analyzer)ReflectionTestUtils.getField(spySearchManager, "analyzer");
            Directory artistDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager,
                "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat("Analyzer should not be null", analyzer, notNullValue());
            assertThat("Artist directory should not be null", artistDirectory, notNullValue());
            assertThat("Artist writer should not be null", artistWriter, notNullValue());
            assertThat("Artist manager should not be null", artistManager, notNullValue());
            assertThat("Track directory should not be null", trackDirectory, notNullValue());
            assertThat("Track writer should not be null", trackWriter, notNullValue());
            assertThat("Track manager should not be null", trackManager, notNullValue());
            assertThat("Secure random should not be null", secureRandom, notNullValue());
            assertThat("Executor service should not be null", executorService, notNullValue());
            assertThat("Genre list should have a size of 1", genreList, hasSize(1));
            assertThat("Genre list should contain the unspecified genre", genreList.get(0), equalTo(UNSPECIFIED_GENRE));
            assertThat("Year list should be empty", yearList.isEmpty(), equalTo(true));
            assertThat("Track sort list should have a size of 4", trackSortList, hasSize(4));

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
            doReturn(true).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(true);

            ReflectionTestUtils.setField(spySearchManager, "artistDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "artistManager", null);
            ReflectionTestUtils.setField(spySearchManager, "artistWriter", null);
            ReflectionTestUtils.setField(spySearchManager, "trackDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "trackManager", null);
            ReflectionTestUtils.setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Analyzer analyzer = (Analyzer)ReflectionTestUtils.getField(spySearchManager, "analyzer");
            Directory artistDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager,
                "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat("Analyzer should not be null", analyzer, notNullValue());
            assertThat("Artist directory should not be null", artistDirectory, notNullValue());
            assertThat("Artist writer should not be null", artistWriter, notNullValue());
            assertThat("Artist manager should not be null", artistManager, notNullValue());
            assertThat("Track directory should not be null", trackDirectory, notNullValue());
            assertThat("Track writer should not be null", trackWriter, notNullValue());
            assertThat("Track manager should not be null", trackManager, notNullValue());
            assertThat("Secure random should not be null", secureRandom, notNullValue());
            assertThat("Executor service should not be null", executorService, notNullValue());
            assertThat("Genre list should have a size of 1", genreList, hasSize(1));
            assertThat("Genre list should contain the unspecified genre", genreList.get(0), equalTo(UNSPECIFIED_GENRE));
            assertThat("Year list should be empty", yearList.isEmpty(), equalTo(true));
            assertThat("Track sort list should have a size of 4", trackSortList, hasSize(4));

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

            ReflectionTestUtils.setField(spySearchManager, "artistDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "artistManager", null);
            ReflectionTestUtils.setField(spySearchManager, "artistWriter", null);
            ReflectionTestUtils.setField(spySearchManager, "trackDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "trackManager", null);
            ReflectionTestUtils.setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();

            Analyzer analyzer = (Analyzer)ReflectionTestUtils.getField(spySearchManager, "analyzer");
            Directory artistDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "artistDirectory");
            IndexWriter artistWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "artistWriter");
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager,
                "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager,
                "executorService");
            List<String> genreList = spySearchManager.getGenreList();
            List<String> yearList = spySearchManager.getYearList();
            List<TrackSort> trackSortList = spySearchManager.getTrackSortList();

            assertThat("Analyzer should not be null", analyzer, notNullValue());
            assertThat("Artist directory should not be null", artistDirectory, notNullValue());
            assertThat("Artist writer should not be null", artistWriter, notNullValue());
            assertThat("Artist manager should not be null", artistManager, notNullValue());
            assertThat("Track directory should not be null", trackDirectory, notNullValue());
            assertThat("Track writer should not be null", trackWriter, notNullValue());
            assertThat("Track manager should not be null", trackManager, notNullValue());
            assertThat("Secure random should not be null", secureRandom, notNullValue());
            assertThat("Executor service should not be null", executorService, notNullValue());
            assertThat("Genre list should have a size of 1", genreList, hasSize(1));
            assertThat("Genre list should contain the unspecified genre", genreList.get(0), equalTo(UNSPECIFIED_GENRE));
            assertThat("Year list should be empty", yearList.isEmpty(), equalTo(true));
            assertThat("Track sort list should have a size of 4", trackSortList, hasSize(4));

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

            ReflectionTestUtils.setField(spySearchManager, "artistDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "artistManager", null);
            ReflectionTestUtils.setField(spySearchManager, "artistWriter", null);
            ReflectionTestUtils.setField(spySearchManager, "trackDirectory", null);
            ReflectionTestUtils.setField(spySearchManager, "trackManager", null);
            ReflectionTestUtils.setField(spySearchManager, "trackWriter", null);

            spySearchManager.initialise();
            spySearchManager.initialise();

            verify(mockApplicationManager, times(1)).shutdown();
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnInitialise() throws Exception {
        try {
            doThrow(new RuntimeException("SearchManagerTest.shouldThrowExceptionOnInitialise()")).when(spySearchManager)
                .indexData();
            doReturn(false).when(spySearchManager).isIndexValid(any());

            spySearchManager.initialise();
        } finally {
            spySearchManager.shutdown();
        }
    }

    @Test
    public void shouldReturnIndexValid() throws Exception {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat("Index should be valid", isValid, equalTo(true));
    }

    @Test
    public void shouldReturnIndexValidWhenExceptionOnRelease() throws Exception {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexValidWhenExceptionOnRelease()"))
            .when(mockArtistManager).release(mockIndexSearcher);

        List<Track> tracks = new ArrayList<>();
        tracks.add(mock(Track.class));

        doReturn(tracks).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat("Index should be valid", isValid, equalTo(true));
    }

    @Test
    public void shouldReturnIndexInvalidWithEmptyTracks() throws Exception {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doReturn(Collections.emptyList()).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat("Index should be invalid", isValid, equalTo(false));
    }

    @Test
    public void shouldReturnIndexInvalidWithNullTracks() throws Exception {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doReturn(null).when(spySearchManager).search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat("Index should be invalid", isValid, equalTo(false));
    }

    @Test
    public void shouldReturnIndexInvalidOnException() throws Exception {
        IndexSearcher mockIndexSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockIndexSearcher);
        doNothing().when(mockArtistManager).release(mockIndexSearcher);
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexInvalidOnException()")).when(spySearchManager)
            .search(any());

        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);

        assertThat("Index should be invalid", isValid, equalTo(false));
    }

    @Test
    public void shouldIndexData() throws Exception {
        spySearchManager.indexData();

        verify(mockDataManager, times(1)).parse(any());
        verify(mockArtistWriter, times(1)).commit();
        verify(mockTrackWriter, times(1)).commit();
        verify(mockArtistManager, times(1)).maybeRefreshBlocking();
        verify(mockTrackManager, times(1)).maybeRefreshBlocking();
        verify(mockSettingsManager, times(1)).setLastIndexedDate(any());
        verify(getMockEventManager(), times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    public void shouldIndexDataButNotCommitOnException() throws Exception {
        doThrow(new RuntimeException("SearchManagerTest.shouldIndexDataButNotCommitOnException()"))
            .when(mockArtistWriter).commit();

        spySearchManager.indexData();

        verify(mockDataManager, times(1)).parse(any());
        verify(mockArtistWriter, times(1)).commit();
        verify(mockTrackWriter, never()).commit();
        verify(mockArtistManager, never()).maybeRefreshBlocking();
        verify(mockTrackManager, never()).maybeRefreshBlocking();
        verify(mockSettingsManager, times(1)).setLastIndexedDate(any());
        verify(getMockEventManager(), times(1)).fireEvent(Event.DATA_INDEXED);
    }

    @Test
    public void shouldAddArtist() throws Exception {
        spySearchManager.addArtist(new Artist("123", "Artist Name", "Artist Image", null, "Members"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());

        assertThat("Artist ID should be stored",
            document.getValue().getField(ArtistField.ARTISTID.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist ID should be 123", document.getValue().getField(ArtistField.ARTISTID.name()).stringValue(),
            equalTo("123"));
        assertThat("Artist name should be stored",
            document.getValue().getField(ArtistField.ARTISTNAME.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist name should be 'Artist Name'",
            document.getValue().getField(ArtistField.ARTISTNAME.name()).stringValue(), equalTo("Artist Name"));
        assertThat("Artist image should be stored",
            document.getValue().getField(ArtistField.ARTISTIMAGE.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist image should be 'Artist Image'",
            document.getValue().getField(ArtistField.ARTISTIMAGE.name()).stringValue(), equalTo("Artist Image"));
        assertThat("Biography should be stored",
            document.getValue().getField(ArtistField.BIOGRAPHY.name()).fieldType().stored(), equalTo(true));
        assertThat("Biography should be ''", document.getValue().getField(ArtistField.BIOGRAPHY.name()).stringValue(),
            equalTo(""));
        assertThat("Members should be stored",
            document.getValue().getField(ArtistField.MEMBERS.name()).fieldType().stored(), equalTo(true));
        assertThat("Members should be 'Members'",
            document.getValue().getField(ArtistField.MEMBERS.name()).stringValue(), equalTo("Members"));
    }

    @Test
    public void shouldNotAddArtistOnException() throws Exception {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddArtistOnException()")).when(mockArtistWriter)
            .addDocument(any());

        spySearchManager.addArtist(new Artist("123", "Artist Name", "Artist Image", null, "Members"));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());

        assertThat("Document should have 5 fields", document.getValue().getFields(), hasSize(5));
    }

    @Test
    public void shouldAddTrack() throws Exception {
        spySearchManager.addTrack(new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image",
            2000, "789", "Track Name", 1, "Location", true, Arrays.asList("Genre 1", "Genre 2")));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockTrackWriter, times(1)).addDocument(document.capture());

        assertThat("Track keywords should be stored",
            document.getValue().getField(TrackField.KEYWORDS.name()).fieldType().stored(), equalTo(true));
        assertThat("Track keywords should be 'artist name album name track name'",
            document.getValue().getField(TrackField.KEYWORDS.name()).stringValue(),
            equalTo("artist name album name track name"));
        assertThat("Track artist ID should be stored",
            document.getValue().getField(TrackField.ARTISTID.name()).fieldType().stored(), equalTo(true));
        assertThat("Track artist ID should be 123",
            document.getValue().getField(TrackField.ARTISTID.name()).stringValue(), equalTo("123"));
        assertThat("Track artist name should be stored",
            document.getValue().getField(TrackField.ARTISTNAME.name()).fieldType().stored(), equalTo(true));
        assertThat("Track artist name should be 'Artist Name'",
            document.getValue().getField(TrackField.ARTISTNAME.name()).stringValue(), equalTo("Artist Name"));
        assertThat("Track artist image should be stored",
            document.getValue().getField(TrackField.ARTISTIMAGE.name()).fieldType().stored(), equalTo(true));
        assertThat("Track artist image should be 'Artist Image'",
            document.getValue().getField(TrackField.ARTISTIMAGE.name()).stringValue(), equalTo("Artist Image"));
        assertThat("Track album ID should be stored",
            document.getValue().getField(TrackField.ALBUMID.name()).fieldType().stored(), equalTo(true));
        assertThat("Track album ID should be 456",
            document.getValue().getField(TrackField.ALBUMID.name()).stringValue(), equalTo("456"));
        assertThat("Track album name should be stored",
            document.getValue().getField(TrackField.ALBUMNAME.name()).fieldType().stored(), equalTo(true));
        assertThat("Track album name should be 'Album Name'",
            document.getValue().getField(TrackField.ALBUMNAME.name()).stringValue(), equalTo("Album Name"));
        assertThat("Track album image should be stored",
            document.getValue().getField(TrackField.ALBUMIMAGE.name()).fieldType().stored(), equalTo(true));
        assertThat("Track album image should be 'Album Image'",
            document.getValue().getField(TrackField.ALBUMIMAGE.name()).stringValue(), equalTo("Album Image"));
        assertThat("Track year should be stored",
            document.getValue().getField(TrackField.YEAR.name()).fieldType().stored(), equalTo(true));
        assertThat("Track year should be 2000", document.getValue().getField(TrackField.YEAR.name()).stringValue(),
            equalTo("2000"));
        assertThat("Track ID should be stored",
            document.getValue().getField(TrackField.TRACKID.name()).fieldType().stored(), equalTo(true));
        assertThat("Track ID should be 789", document.getValue().getField(TrackField.TRACKID.name()).stringValue(),
            equalTo("789"));
        assertThat("Track name should be stored",
            document.getValue().getField(TrackField.TRACKNAME.name()).fieldType().stored(), equalTo(true));
        assertThat("Track name should be 'Track Name'",
            document.getValue().getField(TrackField.TRACKNAME.name()).stringValue(), equalTo("Track Name"));
        assertThat("Track number should be stored",
            document.getValue().getField(TrackField.NUMBER.name()).fieldType().stored(), equalTo(true));
        assertThat("Track number should be 1", document.getValue().getField(TrackField.NUMBER.name()).stringValue(),
            equalTo("1"));
        assertThat("Track location should be stored",
            document.getValue().getField(TrackField.LOCATION.name()).fieldType().stored(), equalTo(true));
        assertThat("Track location should be Location",
            document.getValue().getField(TrackField.LOCATION.name()).stringValue(), equalTo("Location"));
        assertThat("Track is preferred should be stored",
            document.getValue().getField(TrackField.ISPREFERRED.name()).fieldType().stored(), equalTo(true));
        assertThat("Track is preferred should be true",
            document.getValue().getField(TrackField.ISPREFERRED.name()).stringValue(), equalTo("true"));

        assertThat("Track genres should have a size of 2",
            document.getValue().getFields(TrackField.GENRE.name()).length, equalTo(2));
        assertThat("Track genre 0 should be stored",
            document.getValue().getFields(TrackField.GENRE.name())[0].fieldType().stored(), equalTo(true));
        assertThat("Track genre 0 should be 'Genre 1'",
            document.getValue().getFields(TrackField.GENRE.name())[0].stringValue(), equalTo("Genre 1"));
        assertThat("Track genre 1 should be stored",
            document.getValue().getFields(TrackField.GENRE.name())[1].fieldType().stored(), equalTo(true));
        assertThat("Track genre 1 should be 'Genre 2'",
            document.getValue().getFields(TrackField.GENRE.name())[1].stringValue(), equalTo("Genre 2"));

        assertThat("Track default sort should be 'ArtistName0000002000AlbumName0000000001'",
            document.getValue().getBinaryValue(TrackSort.DEFAULTSORT.name()).utf8ToString(),
            equalTo("ArtistName0000002000AlbumName0000000001"));
        assertThat("Track artist sort should be '0000002000ArtistName'",
            document.getValue().getBinaryValue(TrackSort.ARTISTSORT.name()).utf8ToString(),
            equalTo("0000002000ArtistName"));
        assertThat("Track album sort should be '0000002000AlbumName'",
            document.getValue().getBinaryValue(TrackSort.ALBUMSORT.name()).utf8ToString(),
            equalTo("0000002000AlbumName"));
        assertThat("Track sort should be '0000002000TrackName'",
            document.getValue().getBinaryValue(TrackSort.TRACKSORT.name()).utf8ToString(),
            equalTo("0000002000TrackName"));
    }

    @Test
    public void shouldNotAddTrackOnException() throws Exception {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddTrackOnException()")).when(mockTrackWriter)
            .addDocument(any());

        spySearchManager.addTrack(new Track("123", "Artist Name", "Artist Image", "456", "Album Name", "Album Image",
            2000, "789", "Track Name", 1, "Location", true, Arrays.asList("Genre 1", "Genre 2")));

        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockTrackWriter, times(1)).addDocument(document.capture());

        assertThat("Document should have 19 fields", document.getValue().getFields(), hasSize(19));
    }

    @Test
    public void shouldGetDistinctTrackFieldValues() throws Exception {
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

        assertThat("Result should have a size of 2", result, hasSize(2));
        assertThat("Result 0 should be 'Ref 1'", result.get(0), equalTo("Ref 1"));
        assertThat("Result 1 should be 'Ref 2'", result.get(1), equalTo("Ref 2"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetDistinctTrackFieldValuesIfTrackManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "trackManager", null);

        spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);
    }

    @Test
    public void shouldGetEmptyDistinctTrackFieldValuesWhenTermsNull() throws Exception {
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

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown() throws Exception {
        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyDistinctTrackFieldValuesWhenExceptionThrown()"))
            .when(mockTrackManager).acquire();

        List<String> result = spySearchManager.getDistinctTrackFieldValues(TrackField.ALBUMID);

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetDistinctTrackFieldValuesIsExceptionThrownOnRelease() throws Exception {
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

        assertThat("Result should have a size of 2", result, hasSize(2));
        assertThat("Result 0 should be 'Ref 1'", result.get(0), equalTo("Ref 1"));
        assertThat("Result 1 should be 'Ref 2'", result.get(1), equalTo("Ref 2"));
    }

    @Test
    public void shouldGetSearchResults() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        ScoreDoc[] scoreDocs = { new ScoreDoc(1, 0), new ScoreDoc(2, 0) };
        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat("Result should have 2 tracks", result, hasSize(2));

        Track track1 = result.get(0);

        assertThat("Track 1 artist ID should be 1231", track1.getArtistId(), equalTo("1231"));
        assertThat("Track 1 artist name should be 'Artist Name 1", track1.getArtistName(), equalTo("Artist Name 1"));
        assertThat("Track 1 artist image should be 'Artist Image 1", track1.getArtistImage(),
            equalTo("Artist Image 1"));
        assertThat("Track 1 album ID should be 4561", track1.getAlbumId(), equalTo("4561"));
        assertThat("Track 1 album name should be 'Album Name 1", track1.getAlbumName(), equalTo("Album Name 1"));
        assertThat("Track 1 album image should be 'Album Image 1", track1.getAlbumImage(), equalTo("Album Image 1"));
        assertThat("Track 1 year should be 2001", track1.getYear(), equalTo(2001));
        assertThat("Track 1 track ID should be 7891", track1.getTrackId(), equalTo("7891"));
        assertThat("Track 1 track name should be 'Track Name 1", track1.getTrackName(), equalTo("Track Name 1"));
        assertThat("Track 1 number should be 1", track1.getNumber(), equalTo(1));
        assertThat("Track 1 location should be 'Location 1'", track1.getLocation(), equalTo("Location 1"));
        assertThat("Track 1 is preferred should be true", track1.isPreferred(), equalTo(true));
        assertThat("Track 1 should have 2 genres", track1.getGenres(), hasSize(2));
        assertThat("Track 1 genre 0 should be 'Genre 1 1", track1.getGenres().get(0), equalTo("Genre 1 1"));
        assertThat("Track 1 genre 1 should be 'Genre 2 1", track1.getGenres().get(1), equalTo("Genre 2 1"));

        Track track2 = result.get(1);

        assertThat("Track 2 artist ID should be 1232", track2.getArtistId(), equalTo("1232"));
        assertThat("Track 2 artist name should be 'Artist Name 2", track2.getArtistName(), equalTo("Artist Name 2"));
        assertThat("Track 2 artist image should be 'Artist Image 2", track2.getArtistImage(),
            equalTo("Artist Image 2"));
        assertThat("Track 2 album ID should be 4562", track2.getAlbumId(), equalTo("4562"));
        assertThat("Track 2 album name should be 'Album Name 2", track2.getAlbumName(), equalTo("Album Name 2"));
        assertThat("Track 2 album image should be 'Album Image 2", track2.getAlbumImage(), equalTo("Album Image 2"));
        assertThat("Track 2 year should be 2002", track2.getYear(), equalTo(2002));
        assertThat("Track 2 track ID should be 7892", track2.getTrackId(), equalTo("7892"));
        assertThat("Track 2 track name should be 'Track Name 2", track2.getTrackName(), equalTo("Track Name 2"));
        assertThat("Track 2 number should be 2", track2.getNumber(), equalTo(2));
        assertThat("Track 2 location should be 'Location 2'", track2.getLocation(), equalTo("Location 2"));
        assertThat("Track 2 is preferred should be true", track2.isPreferred(), equalTo(false));
        assertThat("Track 2 should have 2 genres", track2.getGenres(), hasSize(2));
        assertThat("Track 2 genre 0 should be 'Genre 1 2", track2.getGenres().get(0), equalTo("Genre 1 2"));
        assertThat("Track 2 genre 1 should be 'Genre 2 2", track2.getGenres().get(1), equalTo("Genre 2 2"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetSearchResultsIfTrackManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "trackManager", null);

        spySearchManager.search(new TrackSearch("keywords"));
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullTrackSearch() {
        List<Track> result = spySearchManager.search(null);

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetEmptySearchResultsWithNullKeywords() {
        List<Track> result = spySearchManager.search(new TrackSearch(null));

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetEmptySearchResultsWithEmptyKeywords() {
        List<Track> result = spySearchManager.search(new TrackSearch(" "));

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetEmptySearchResultsOnException() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptySearchResultsOnException()"))
            .when(mockTrackSearcher).search(any(), anyInt(), any());

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetSearchResultsWhenExceptionThrownOnRelease() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        ScoreDoc[] scoreDocs = { new ScoreDoc(1, 0), new ScoreDoc(2, 0) };
        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(scoreDocs.length, scoreDocs, null, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetSearchResultsWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        List<Track> result = spySearchManager.search(new TrackSearch("keywords"));

        assertThat("Result should have 2 tracks", result, hasSize(2));
    }

    @Test
    public void shouldGetShuffledPlaylist() throws Exception {
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

        ReflectionTestUtils.setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        ReflectionTestUtils.setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat("Result should have 3 tracks", result, hasSize(3));

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat("Unique result should have 3 tracks", uniqueResult, hasSize(3));
    }

    @Test
    public void shouldGetShuffledPlaylistWithYearFilter() throws Exception {
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

        ReflectionTestUtils.setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        ReflectionTestUtils.setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        List<Track> result = spySearchManager.getShuffledPlaylist(3, "2001");

        assertThat("Result should have 3 tracks", result, hasSize(3));

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat("Unique result should have 3 tracks", uniqueResult, hasSize(3));
    }

    @Test
    public void shouldGetShuffledPlaylistWhenExceptionThrownOnRelease() throws Exception {
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

        ReflectionTestUtils.setField(spySearchManager, "executorService", Executors.newSingleThreadExecutor());
        ReflectionTestUtils.setField(spySearchManager, "random",
            new SecureRandom(Long.toString(System.currentTimeMillis()).getBytes()));

        doThrow(new RuntimeException("SearchManagerTest.shouldGetShuffledPlaylistWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat("Result should have 3 tracks", result, hasSize(3));

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat("Unique result should have 3 tracks", uniqueResult, hasSize(3));
    }

    @Test
    public void shouldGetMaxSizeShuffledPlaylistWhenPlaylistSizeGreaterOrEqualToNumberOfSearchResults()
        throws Exception {
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

        assertThat("Result should have 9 tracks", result, hasSize(9));

        Set<Track> uniqueResult = new HashSet<>(result);

        assertThat("Unique result should have 9 tracks", uniqueResult, hasSize(9));
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetShuffledPlaylistIfTrackManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "trackManager", null);

        spySearchManager.getShuffledPlaylist(3, null);
    }

    @Test
    public void shouldGetEmptyShuffledPlaylistOnException() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetEmptyShuffledPlaylistOnException()"))
            .when(mockTrackSearcher).search(any(), anyInt());

        List<Track> result = spySearchManager.getShuffledPlaylist(3, null);

        assertThat("Result should be empty", result.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldGetArtistById() throws Exception {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        Artist artist = spySearchManager.getArtistById("123");

        assertThat("Artist should not be null", artist, notNullValue());
        assertThat("Artist ID should be 1231", artist.getArtistId(), equalTo("1231"));
        assertThat("Artist name should be 'Artist Name 1'", artist.getArtistName(), equalTo("Artist Name 1"));
        assertThat("Artist image should be 'Artist Image 1'", artist.getArtistImage(), equalTo("Artist Image 1"));
        assertThat("Biography should be 'Biography 1'", artist.getBiography(), equalTo("Biography 1"));
        assertThat("Members should be 'Members 1'", artist.getMembers(), equalTo("Members 1"));
    }

    @Test
    public void shouldGetArtistByIdWhenExceptionThrownOnRelease() throws Exception {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetArtistByIdWhenExceptionThrownOnRelease()"))
            .when(mockArtistManager).release(any());

        Artist artist = spySearchManager.getArtistById("123");

        assertThat("Artist should not be null", artist, notNullValue());
        assertThat("Artist ID should be 1231", artist.getArtistId(), equalTo("1231"));
        assertThat("Artist name should be 'Artist Name 1'", artist.getArtistName(), equalTo("Artist Name 1"));
        assertThat("Artist image should be 'Artist Image 1'", artist.getArtistImage(), equalTo("Artist Image 1"));
        assertThat("Biography should be 'Biography 1'", artist.getBiography(), equalTo("Biography 1"));
        assertThat("Members should be 'Members 1'", artist.getMembers(), equalTo("Members 1"));
    }

    @Test
    public void shouldFailToGetArtistByIdIfNoSearchResults() throws Exception {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        when(mockArtistSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[] {}, 0));
        setArtistSearcherDocuments(mockArtistSearcher);

        Artist artist = spySearchManager.getArtistById("123");

        assertThat("Artist should be null", artist, nullValue());
    }

    @Test
    public void shouldFailToGetArtistByIdOnException() throws Exception {
        IndexSearcher mockArtistSearcher = mock(IndexSearcher.class);
        when(mockArtistManager.acquire()).thenReturn(mockArtistSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetArtistByIdOnException()"))
            .when(mockArtistSearcher).search(any(), anyInt());

        Artist artist = spySearchManager.getArtistById("123");

        assertThat("Artist should be null", artist, nullValue());
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetArtistByIdIfArtistManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "artistManager", null);

        spySearchManager.getArtistById("123");
    }

    @Test
    public void shouldGetTrackById() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        Track track = spySearchManager.getTrackById("123");

        assertThat("Track should not be null", track, notNullValue());
        assertThat("Track artist ID should be 1231", track.getArtistId(), equalTo("1231"));
        assertThat("Track artist name should be 'Artist Name 1'", track.getArtistName(), equalTo("Artist Name 1"));
        assertThat("Track artist image should be 'Artist Image 1'", track.getArtistImage(), equalTo("Artist Image 1"));
        assertThat("Track album ID should be 4561", track.getAlbumId(), equalTo("4561"));
        assertThat("Track album name should be 'Album Name 1'", track.getAlbumName(), equalTo("Album Name 1"));
        assertThat("Track album image should be 'Album Image 1'", track.getAlbumImage(), equalTo("Album Image 1"));
        assertThat("Track year should be 2001", track.getYear(), equalTo(2001));
        assertThat("Track ID should be 7891", track.getTrackId(), equalTo("7891"));
        assertThat("Track name should be 'Track Name 1'", track.getTrackName(), equalTo("Track Name 1"));
        assertThat("Track number should be 1", track.getNumber(), equalTo(1));
        assertThat("Track location should be 'Location 1'", track.getLocation(), equalTo("Location 1"));
        assertThat("Track is preferred should be true", track.isPreferred(), equalTo(true));
        assertThat("Track genres should have a size of 2", track.getGenres(), hasSize(2));
        assertThat("Track genre 0 should be 'Genre 1 1", track.getGenres().get(0), equalTo("Genre 1 1"));
        assertThat("Track genre 1 should be 'Genre 2 1", track.getGenres().get(1), equalTo("Genre 2 1"));
    }

    @Test
    public void shouldGetTrackByIdWhenExceptionThrownOnRelease() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt()))
            .thenReturn(new TopDocs(1, new ScoreDoc[] { new ScoreDoc(1, 0) }, 0));
        setTrackSearcherDocuments(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldGetTrackByIdWhenExceptionThrownOnRelease()"))
            .when(mockTrackManager).release(any());

        Track track = spySearchManager.getTrackById("123");

        assertThat("Track should not be null", track, notNullValue());
        assertThat("Track artist ID should be 1231", track.getArtistId(), equalTo("1231"));
        assertThat("Track artist name should be 'Artist Name 1'", track.getArtistName(), equalTo("Artist Name 1"));
        assertThat("Track artist image should be 'Artist Image 1'", track.getArtistImage(), equalTo("Artist Image 1"));
        assertThat("Track album ID should be 4561", track.getAlbumId(), equalTo("4561"));
        assertThat("Track album name should be 'Album Name 1'", track.getAlbumName(), equalTo("Album Name 1"));
        assertThat("Track album image should be 'Album Image 1'", track.getAlbumImage(), equalTo("Album Image 1"));
        assertThat("Track year should be 2001", track.getYear(), equalTo(2001));
        assertThat("Track ID should be 7891", track.getTrackId(), equalTo("7891"));
        assertThat("Track name should be 'Track Name 1'", track.getTrackName(), equalTo("Track Name 1"));
        assertThat("Track number should be 1", track.getNumber(), equalTo(1));
        assertThat("Track location should be 'Location 1'", track.getLocation(), equalTo("Location 1"));
        assertThat("Track is preferred should be true", track.isPreferred(), equalTo(true));
        assertThat("Track genres should have a size of 2", track.getGenres(), hasSize(2));
        assertThat("Track genre 0 should be 'Genre 1 1", track.getGenres().get(0), equalTo("Genre 1 1"));
        assertThat("Track genre 1 should be 'Genre 2 1", track.getGenres().get(1), equalTo("Genre 2 1"));
    }

    @Test
    public void shouldFailToGetTrackByIdIfNoSearchResults() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt())).thenReturn(new TopDocs(0, new ScoreDoc[] {}, 0));
        setArtistSearcherDocuments(mockTrackSearcher);

        Track track = spySearchManager.getTrackById("123");

        assertThat("Track should be null", track, nullValue());
    }

    @Test
    public void shouldFailToGetTrackByIdOnException() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetTrackByIdOnException()")).when(mockTrackSearcher)
            .search(any(), anyInt());

        Track track = spySearchManager.getTrackById("123");

        assertThat("Track should be null", track, nullValue());
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetTrackByIdIfArtistManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "trackManager", null);

        spySearchManager.getTrackById("123");
    }

    @Test
    public void shouldGetAlbumById() throws Exception {
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

        assertThat("Tracks should have a size of 9", tracks, hasSize(9));
    }

    @Test
    public void shouldGetAlbumByIdWhenExceptionThrownOnRelease() throws Exception {
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

        assertThat("Tracks should have a size of 9", tracks, hasSize(9));
    }

    @Test
    public void shouldGetEmptyAlbumByIdIfNoSearchResults() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        when(mockTrackSearcher.search(any(), anyInt(), any()))
            .thenReturn(new TopFieldDocs(0, new ScoreDoc[] {}, null, 0));
        setArtistSearcherDocuments(mockTrackSearcher);

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat("Tracks should be empty", tracks.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldFailToGetAlbumByIdOnException() throws Exception {
        IndexSearcher mockTrackSearcher = mock(IndexSearcher.class);
        when(mockTrackManager.acquire()).thenReturn(mockTrackSearcher);

        doThrow(new RuntimeException("SearchManagerTest.shouldFailToGetAlbumByIdOnException()")).when(mockTrackSearcher)
            .search(any(), anyInt(), any());

        List<Track> tracks = spySearchManager.getAlbumById("123");

        assertThat("Tracks should be null", tracks, nullValue());
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailToGetAlbumByIdIfArtistManagerIsNull() {
        ReflectionTestUtils.setField(spySearchManager, "trackManager", null);

        spySearchManager.getAlbumById("123");
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
