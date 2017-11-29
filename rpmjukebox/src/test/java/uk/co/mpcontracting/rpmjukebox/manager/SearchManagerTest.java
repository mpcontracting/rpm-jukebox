package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.model.Artist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.search.ArtistField;
import uk.co.mpcontracting.rpmjukebox.search.TrackSort;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

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
    private MainPanelController mockMainPanelController;
    
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
        ReflectionTestUtils.setField(spySearchManager, "mainPanelController", mockMainPanelController);
        
        when(mockSettingsManager.getFileFromConfigDirectory(directoryArtistIndex)).thenReturn(settingsManager.getFileFromConfigDirectory(directoryArtistIndex));
        when(mockSettingsManager.getFileFromConfigDirectory(directoryTrackIndex)).thenReturn(settingsManager.getFileFromConfigDirectory(directoryTrackIndex));
        
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
            doNothing().when(spySearchManager).indexData(anyBoolean());
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
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager, "executorService");
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
            
            verify(spySearchManager, never()).indexData(anyBoolean());
            verify(spySearchManager, times(9)).search(any());
            verify(mockMainPanelController, never()).showMessageWindow(anyString(), anyBoolean());
        } finally {
            spySearchManager.shutdown();
        }
    }
    
    @Test
    public void shouldInitialiseAndIndexWhenDataFileHasExpired() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData(anyBoolean());
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
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager, "executorService");
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
            
            verify(spySearchManager, times(1)).indexData(true);
            verify(spySearchManager, times(9)).search(any());
            verify(mockMainPanelController, never()).showMessageWindow(anyString(), anyBoolean());
        } finally {
            spySearchManager.shutdown();
        }
    }
    
    @Test
    public void shouldInitialiseAndIndexWhenIndexIsInvalid() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData(anyBoolean());
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
            SearcherManager artistManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "artistManager");
            Directory trackDirectory = (Directory)ReflectionTestUtils.getField(spySearchManager, "trackDirectory");
            IndexWriter trackWriter = (IndexWriter)ReflectionTestUtils.getField(spySearchManager, "trackWriter");
            SearcherManager trackManager = (SearcherManager)ReflectionTestUtils.getField(spySearchManager, "trackManager");
            SecureRandom secureRandom = (SecureRandom)ReflectionTestUtils.getField(spySearchManager, "random");
            ExecutorService executorService = (ExecutorService)ReflectionTestUtils.getField(spySearchManager, "executorService");
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
            
            verify(spySearchManager, times(1)).indexData(true);
            verify(spySearchManager, times(9)).search(any());
            verify(mockMainPanelController, never()).showMessageWindow(anyString(), anyBoolean());
        } finally {
            spySearchManager.shutdown();
        }
    }
    
    @Test
    public void shouldNotInitialiseIfAlreadyInitialised() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData(anyBoolean());
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
            
            verify(mockMainPanelController, times(1)).showMessageWindow(anyString(), anyBoolean());
            verify(mockApplicationManager, times(1)).shutdown();
        } finally {
            spySearchManager.shutdown();
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionOnInitialise() throws Exception {
        try {
            doThrow(new RuntimeException("SearchManagerTest.shouldThrowExceptionOnInitialise()")).when(spySearchManager).indexData(anyBoolean());
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
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexValidWhenExceptionOnRelease()")).when(mockArtistManager).release(mockIndexSearcher);
        
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
        doThrow(new RuntimeException("SearchManagerTest.shouldReturnIndexInvalidOnException()")).when(spySearchManager).search(any());
        
        boolean isValid = spySearchManager.isIndexValid(mockArtistManager);
        
        assertThat("Index should be invalid", isValid, equalTo(false));
    }
    
    @Test
    public void shouldIndexData() throws Exception {
        spySearchManager.indexData(true);
        
        verify(mockMainPanelController, times(1)).showMessageWindow(anyString(), anyBoolean());
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
        doThrow(new RuntimeException("SearchManagerTest.shouldIndexDataButNotCommitOnException()")).when(mockArtistWriter).commit();
        
        spySearchManager.indexData(true);
        
        verify(mockMainPanelController, times(1)).showMessageWindow(anyString(), anyBoolean());
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
        Artist mockArtist = mock(Artist.class);
        when(mockArtist.getArtistId()).thenReturn("123");
        when(mockArtist.getArtistName()).thenReturn("Artist Name");
        when(mockArtist.getArtistImage()).thenReturn("Artist Image");
        when(mockArtist.getBiography()).thenReturn(null);
        when(mockArtist.getMembers()).thenReturn("Members");

        spySearchManager.addArtist(mockArtist);
        
        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());
        
        assertThat("Artist ID should be stored", document.getValue().getField(ArtistField.ARTISTID.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist ID should be 123", document.getValue().getField(ArtistField.ARTISTID.name()).stringValue(), equalTo("123"));
        assertThat("Artist name should be stored", document.getValue().getField(ArtistField.ARTISTNAME.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist name should be 'Artist Name'", document.getValue().getField(ArtistField.ARTISTNAME.name()).stringValue(), equalTo("Artist Name"));
        assertThat("Artist image should be stored", document.getValue().getField(ArtistField.ARTISTIMAGE.name()).fieldType().stored(), equalTo(true));
        assertThat("Artist image should be 'Artist Image'", document.getValue().getField(ArtistField.ARTISTIMAGE.name()).stringValue(), equalTo("Artist Image"));
        assertThat("Biography should be stored", document.getValue().getField(ArtistField.BIOGRAPHY.name()).fieldType().stored(), equalTo(true));
        assertThat("Biography should be ''", document.getValue().getField(ArtistField.BIOGRAPHY.name()).stringValue(), equalTo(""));
        assertThat("Members should be stored", document.getValue().getField(ArtistField.MEMBERS.name()).fieldType().stored(), equalTo(true));
        assertThat("Members should be 'Members'", document.getValue().getField(ArtistField.MEMBERS.name()).stringValue(), equalTo("Members"));
    }
    
    @Test
    public void shouldNotAddArtistOnException() throws Exception {
        doThrow(new RuntimeException("SearchManagerTest.shouldNotAddArtistOnException()")).when(mockArtistWriter).addDocument(any());
        
        Artist mockArtist = mock(Artist.class);
        when(mockArtist.getArtistId()).thenReturn("123");
        when(mockArtist.getArtistName()).thenReturn("Artist Name");
        when(mockArtist.getArtistImage()).thenReturn("Artist Image");
        when(mockArtist.getBiography()).thenReturn(null);
        when(mockArtist.getMembers()).thenReturn("Members");

        spySearchManager.addArtist(mockArtist);
        
        ArgumentCaptor<Document> document = ArgumentCaptor.forClass(Document.class);
        verify(mockArtistWriter, times(1)).addDocument(document.capture());
        
        assertThat("Document should have 5 fields", document.getValue().getFields(), hasSize(5));
    }
}
