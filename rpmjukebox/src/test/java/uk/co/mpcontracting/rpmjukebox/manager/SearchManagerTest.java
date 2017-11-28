package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
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
    private MainPanelController mockMainPanelController;
    
    private SearchManager spySearchManager;
    
    @Before
    public void setup() {
        spySearchManager = spy(searchManager);
        ReflectionTestUtils.setField(spySearchManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(spySearchManager, "settingsManager", mockSettingsManager);
        ReflectionTestUtils.setField(spySearchManager, "mainPanelController", mockMainPanelController);
        
        when(mockSettingsManager.getFileFromConfigDirectory(directoryArtistIndex)).thenReturn(settingsManager.getFileFromConfigDirectory(directoryArtistIndex));
        when(mockSettingsManager.getFileFromConfigDirectory(directoryTrackIndex)).thenReturn(settingsManager.getFileFromConfigDirectory(directoryTrackIndex));
    }
    
    @Test
    public void shouldInitialise() throws Exception {
        try {
            doNothing().when(spySearchManager).indexData(anyBoolean());
            doReturn(true).when(spySearchManager).isIndexValid(any());
            doReturn(Collections.emptyList()).when(spySearchManager).search(any());
            doReturn(Collections.emptyList()).when(spySearchManager).getDistinctTrackFieldValues(any());
            when(mockSettingsManager.hasDataFileExpired()).thenReturn(false);
            
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
}
