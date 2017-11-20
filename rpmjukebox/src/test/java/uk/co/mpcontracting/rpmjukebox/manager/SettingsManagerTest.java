package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.Gson;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.settings.Window;

public class SettingsManagerTest extends AbstractTest {

	@Autowired
	private SettingsManager settingsManager;
	
	@Value("${file.last.indexed}")
	private String fileLastIndexed;
	
	@Value("${file.window.settings}")
    private String fileWindowSettings;
    
    @Value("${file.settings}")
    private String fileSettings;
    
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
	
	@Test
	public void shouldSetLastIndexedDate() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		LocalDateTime now = LocalDateTime.now();
		
		spySettingsManager.setLastIndexedDate(now);
		
		LocalDateTime lastIndexed = null;
		File lastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
		try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
			lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())), ZoneId.systemDefault());
		}
		
		assertThat("Last indexed should equal '" + now + "'", now, equalTo(lastIndexed));
	}
	
	@Test
	public void shouldSetLastIndexedDateIfAlreadyExists() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File writeLastIndexedFile = new File(RpmJukebox.getConfigDirectory(), "last-indexed");
		LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
            writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }
        
		LocalDateTime now = LocalDateTime.now();
		
		spySettingsManager.setLastIndexedDate(now);
		
		LocalDateTime lastIndexed = null;
		File lastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
		try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
			lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())), ZoneId.systemDefault());
		}
		
		assertThat("Last indexed should equal '" + now + "'", now, equalTo(lastIndexed));
	}
	
	@Test
	public void shouldNotSetLastIndexedDateOnExceptionIfNotAlreadyExists() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		LocalDateTime mockLocalDateTime = mock(LocalDateTime.class);
		doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()")).when(mockLocalDateTime).atZone(any());
		
		spySettingsManager.setLastIndexedDate(mockLocalDateTime);
		
		File lastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
		
		assertThat("Last indexed file should not exist", lastIndexedFile.exists(), equalTo(false));
	}
	
	@Test
	public void shouldLeaveLastIndexedDateOnExceptionIfAlreadyExists() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File writeLastIndexedFile = new File(RpmJukebox.getConfigDirectory(), "last-indexed");
		LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
            writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }
		
        LocalDateTime mockLocalDateTime = mock(LocalDateTime.class);
		doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()")).when(mockLocalDateTime).atZone(any());
		
		spySettingsManager.setLastIndexedDate(mockLocalDateTime);
		
		LocalDateTime lastIndexed = null;
		File readLastIndexedFile = settingsManager.getFileFromConfigDirectory(fileLastIndexed);
		try (BufferedReader reader = new BufferedReader(new FileReader(readLastIndexedFile))) {
			lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())), ZoneId.systemDefault());
		}
		
		assertThat("Last indexed should equal '" + originalLastIndexed + "'", lastIndexed, equalTo(originalLastIndexed));
	}
	
	@Test
	public void shouldLoadWindowSettingsFromDefault() throws Exception {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		double width = (bounds.getWidth() / 100d) * 75d;
		double height = (bounds.getHeight() / 100d) * 75d;
		double x = (bounds.getWidth() - width) / 2d;
		double y = (bounds.getHeight() - height) / 2d;
		
		Stage mockStage = mock(Stage.class);
		spySettingsManager.loadWindowSettings(mockStage);

		verify(mockStage, times(1)).setX(x);
		verify(mockStage, times(1)).setY(y);
		verify(mockStage, times(1)).setWidth(width);
		verify(mockStage, times(1)).setHeight(height);
	}
	
	@Test
	public void shouldLoadWindowSettingsFromFile() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File settingsFile = settingsManager.getFileFromConfigDirectory(fileWindowSettings);
		Window window = new Window(100, 200, 300, 400);
        
        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(new Gson().toJson(window));
        }
		
        Stage mockStage = mock(Stage.class);
        spySettingsManager.loadWindowSettings(mockStage);

		verify(mockStage, times(1)).setX(100d);
		verify(mockStage, times(1)).setY(200d);
		verify(mockStage, times(1)).setWidth(300d);
		verify(mockStage, times(1)).setHeight(400d);
	}
	
	@Test
	public void shouldNotLoadWindowSettingsFromFileOnException() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		File settingsFile = settingsManager.getFileFromConfigDirectory(fileWindowSettings);
		Window window = new Window(100, 200, 300, 400);
        
        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(new Gson().toJson(window));
        }
        
        Gson mockGson = mock(Gson.class);
        Stage mockStage = mock(Stage.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotLoadWindowSettingsFromFileOnException()")).when(mockGson).fromJson(any(FileReader.class), (Class<?>)any(Class.class));
        
        ReflectionTestUtils.setField(spySettingsManager, "gson", mockGson);
        
        spySettingsManager.loadWindowSettings(mockStage);

		verify(mockStage, never()).setX(anyDouble());
		verify(mockStage, never()).setY(anyDouble());
		verify(mockStage, never()).setWidth(anyDouble());
		verify(mockStage, never()).setHeight(anyDouble());
	}
	
	@Test
	public void shouldSaveWindowSettings() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		Stage mockStage = mock(Stage.class);
		when(mockStage.getX()).thenReturn(100d);
		when(mockStage.getY()).thenReturn(200d);
		when(mockStage.getWidth()).thenReturn(300d);
		when(mockStage.getHeight()).thenReturn(400d);
		
		spySettingsManager.saveWindowSettings(mockStage);
		
		File settingsFile = settingsManager.getFileFromConfigDirectory(fileWindowSettings);
		Window window = null;
		
		try (FileReader fileReader = new FileReader(settingsFile)) {
			window = new Gson().fromJson(fileReader, Window.class);
		}
		
		assertThat("X should be 100", window.getX(), equalTo(100d));
		assertThat("Y should be 200", window.getY(), equalTo(200d));
		assertThat("Width should be 300", window.getWidth(), equalTo(300d));
		assertThat("Height should be 400", window.getHeight(), equalTo(400d));
	}
	
	@Test
	public void shouldNotSaveWindowSettingsOnException() throws Exception {
		RpmJukebox.getConfigDirectory().mkdirs();
		Stage mockStage = mock(Stage.class);
		when(mockStage.getX()).thenReturn(100d);
		when(mockStage.getY()).thenReturn(200d);
		when(mockStage.getWidth()).thenReturn(300d);
		when(mockStage.getHeight()).thenReturn(400d);
		
		Gson mockGson = mock(Gson.class);
		doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveWindowSettingsOnException()")).when(mockGson).toJson(any(Window.class));

		ReflectionTestUtils.setField(spySettingsManager, "gson", mockGson);
		
		spySettingsManager.saveWindowSettings(mockStage);
		
		File settingsFile = settingsManager.getFileFromConfigDirectory(fileWindowSettings);
		Window window = null;
		
		try (FileReader fileReader = new FileReader(settingsFile)) {
			window = new Gson().fromJson(fileReader, Window.class);
		}
		
		assertThat("Window should be null", window, nullValue());
	}
}
