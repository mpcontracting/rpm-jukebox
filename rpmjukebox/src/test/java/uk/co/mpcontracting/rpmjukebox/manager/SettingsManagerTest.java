package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;

public class SettingsManagerTest extends AbstractTest {

	@Autowired
	private SettingsManager settingsManager;
	
	@Value("${file.last.indexed}")
	private String fileLastIndexed;
    
    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private PlaylistManager mockPlaylistManager;
    
    @Mock
    private MediaManager mockMediaManager;
    
    @Mock
    private MainPanelController mockMainPanelController;
    
    @Mock
    private URL mockDataFile;
    
    private SettingsManager spySettingsManager;
	
	@Before
	public void setup() {
		spySettingsManager = spy(settingsManager);
		ReflectionTestUtils.setField(spySettingsManager, "searchManager", mockSearchManager);
		ReflectionTestUtils.setField(spySettingsManager, "playlistManager", mockPlaylistManager);
		ReflectionTestUtils.setField(spySettingsManager, "mediaManager", mockMediaManager);
		ReflectionTestUtils.setField(spySettingsManager, "mainPanelController", mockMainPanelController);
		ReflectionTestUtils.setField(spySettingsManager, "dataFile", mockDataFile);
	}
	
	@Test
	public void shouldGetFileFromConfigDirectory() {
		File correctValue = new File(RpmJukebox.getConfigDirectory(), "test");
		File result = spySettingsManager.getFileFromConfigDirectory("test");
		
		assertThat("Resulting file should be '" + correctValue.getAbsolutePath() + "'", result.getAbsolutePath(), equalTo(correctValue.getAbsolutePath()));
	}
	
	@Test
	public void shouldShowHttpDataFileHasExpired() throws Exception {
		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		when(mockDataFile.getProtocol()).thenReturn("http");
		when(mockDataFile.openConnection()).thenReturn(mockConnection);
		when(mockConnection.getLastModified()).thenReturn(getDateTimeInMillis(1975, 1, 1, 0, 0));
		doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should have expired", result, equalTo(true));
	}
	
	@Test
	public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedNotOneHourOld() throws Exception {
		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		when(mockDataFile.getProtocol()).thenReturn("http");
		when(mockDataFile.openConnection()).thenReturn(mockConnection);
		when(mockConnection.getLastModified()).thenReturn(getLocalDateTimeInMillis(LocalDateTime.now().minusMinutes(30)));
		doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should not have expired", result, equalTo(false));
	}
	
	@Test
	public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedBeforeLastIndexed() throws Exception {
		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		when(mockDataFile.getProtocol()).thenReturn("http");
		when(mockDataFile.openConnection()).thenReturn(mockConnection);
		when(mockConnection.getLastModified()).thenReturn(getDateTimeInMillis(1971, 1, 1, 0, 0));
		doReturn(LocalDateTime.of(1975, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should not have expired", result, equalTo(false));
	}
	
	@Test
	public void shouldShowHttpDataFileHasNotExpiredOnLastModifiedError() throws Exception {
		HttpURLConnection mockConnection = mock(HttpURLConnection.class);
		when(mockDataFile.getProtocol()).thenReturn("http");
		when(mockDataFile.openConnection()).thenReturn(mockConnection);
		doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnLastModifiedError()")).when(mockConnection).getLastModified();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should not have expired", result, equalTo(false));
	}
	
	@Test
	public void shouldShowHttpDataFileHasNotExpiredOnConnectionError() throws Exception {
		when(mockDataFile.getProtocol()).thenReturn("http");
		doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnConnectionError()")).when(mockDataFile).openConnection();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should not have expired", result, equalTo(false));
	}

	@Test
	public void shouldShowFileSystemDataFileHasExpired() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File lastIndexedFile = new File(RpmJukebox.getConfigDirectory(), "last-indexed");
		lastIndexedFile.createNewFile();
		lastIndexedFile.setLastModified(getDateTimeInMillis(1975, 1, 1, 0, 0));
		when(mockDataFile.getProtocol()).thenReturn("file");
		when(mockDataFile.toURI()).thenReturn(new URL("file://" + lastIndexedFile.getAbsolutePath()).toURI());
		doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should have expired", result, equalTo(true));
	}
	
	@Test
	public void shouldShowFileSystemDataFileHasNotExpiredOnFileReadError() throws Exception {
		when(mockDataFile.getProtocol()).thenReturn("file");
		doThrow(new RuntimeException("SettingsManagerTest.shouldShowFileSystemDataFileHasNotExpiredOnFileReadError()")).when(mockDataFile).toURI();
		
		boolean result = spySettingsManager.hasDataFileExpired();
		
		assertThat("Data file should not have expired", result, equalTo(false));
	}
	
	@Test
	public void shouldGetLastIndexedDate() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		LocalDateTime now = LocalDateTime.now();
		File lastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write(Long.toString(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }

		doNothing().when(spySettingsManager).setLastIndexedDate(any());
		
		LocalDateTime result = spySettingsManager.getLastIndexedDate();
		
		assertThat("Result should be '" + now + "'", result, equalTo(now));
	}
	
	@Test
	public void shouldGetDefaultLastIndexedDate() {
		doNothing().when(spySettingsManager).setLastIndexedDate(any());
		
		LocalDateTime result = spySettingsManager.getLastIndexedDate();
		
		assertThat("Result should be greater that now minus 1 minute", result.isAfter(LocalDateTime.now().minusMinutes(1)), equalTo(true));
	}
	
	@Test
	public void shouldNotGetLastIndexedDateOnFileReadError() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File lastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write("Unparseable");
            writer.newLine();
        }

		doNothing().when(spySettingsManager).setLastIndexedDate(any());
		
		LocalDateTime result = spySettingsManager.getLastIndexedDate();
		
		assertThat("Result should be null", result, nullValue());
	}
}
