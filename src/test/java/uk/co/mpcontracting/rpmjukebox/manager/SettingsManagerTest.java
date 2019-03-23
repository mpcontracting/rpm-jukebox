package uk.co.mpcontracting.rpmjukebox.manager;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.igormaznitsa.commons.version.Version;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Repeat;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.settings.Settings;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Window;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class SettingsManagerTest extends AbstractGUITest implements Constants {

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private RpmJukebox mockRpmJukebox;

    @Mock
    private MessageManager mockMessageManager;

    @Mock
    private SearchManager mockSearchManager;

    @Mock
    private PlaylistManager mockPlaylistManager;

    @Mock
    private MediaManager mockMediaManager;

    @Mock
    private InternetManager mockInternetManager;

    @Mock
    private URL mockDataFile;

    private SettingsManager spySettingsManager;

    @Before
    @SneakyThrows
    public void setup() {
        spySettingsManager = spy(new SettingsManager(mockAppProperties, mockRpmJukebox, mockMessageManager));
        spySettingsManager.wireSearchManager(mockSearchManager);
        spySettingsManager.wirePlaylistManager(mockPlaylistManager);
        spySettingsManager.wireMediaManager(mockMediaManager);
        spySettingsManager.wireInternetManager(mockInternetManager);

        setField(spySettingsManager, "gson", new GsonBuilder().setPrettyPrinting().create());
        setField(spySettingsManager, "version", new Version("1.0.0"));
        setField(spySettingsManager, "dataFile", mockDataFile);

        getConfigDirectory().mkdirs();

        when(mockAppProperties.getVersion()).thenReturn("1.0.0");
        when(mockAppProperties.getCacheSizeMb()).thenReturn(250);

        when(mockAppProperties.getLastIndexedFile()).thenReturn("last-indexed");
        when(spySettingsManager.getFileFromConfigDirectory("last-indexed")).thenReturn(new File(getConfigDirectory(), "last-indexed"));

        when(mockAppProperties.getWindowSettingsFile()).thenReturn("window.json");
        when(spySettingsManager.getFileFromConfigDirectory("window.json")).thenReturn(new File(getConfigDirectory(), "window.json"));

        when(mockAppProperties.getSystemSettingsFile()).thenReturn("system.json");
        when(spySettingsManager.getFileFromConfigDirectory("system.json")).thenReturn(new File(getConfigDirectory(), "system.json"));

        when(mockAppProperties.getUserSettingsFile()).thenReturn("rpm-jukebox.json");
        when(spySettingsManager.getFileFromConfigDirectory("rpm-jukebox.json")).thenReturn(new File(getConfigDirectory(), "rpm-jukebox.json"));
    }

    @Test
    public void shouldGetFileFromConfigDirectory() {
        File correctValue = new File(RpmJukebox.getConfigDirectory(), "test");
        File result = spySettingsManager.getFileFromConfigDirectory("test");

        assertThat(result.getAbsolutePath()).isEqualTo(correctValue.getAbsolutePath());
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasExpired() {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockDataFile.getProtocol()).thenReturn("http");
        when(mockInternetManager.openConnection(mockDataFile)).thenReturn(mockConnection);
        when(mockConnection.getLastModified()).thenReturn(getDateTimeInMillis(1975, 1, 1, 0, 0));
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();

        boolean result = spySettingsManager.hasDataFileExpired();

        assertThat(result).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedNotOneHourOld() {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockDataFile.getProtocol()).thenReturn("http");
        when(mockInternetManager.openConnection(mockDataFile)).thenReturn(mockConnection);
        when(mockConnection.getLastModified())
            .thenReturn(getLocalDateTimeInMillis(LocalDateTime.now().minusMinutes(30)));
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();

        boolean result = spySettingsManager.hasDataFileExpired();
        
        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedBeforeLastIndexed() {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockDataFile.getProtocol()).thenReturn("http");
        when(mockInternetManager.openConnection(mockDataFile)).thenReturn(mockConnection);
        when(mockConnection.getLastModified()).thenReturn(getDateTimeInMillis(1971, 1, 1, 0, 0));
        doReturn(LocalDateTime.of(1975, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();

        boolean result = spySettingsManager.hasDataFileExpired();
        
        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredOnLastModifiedError() {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockDataFile.getProtocol()).thenReturn("http");
        when(mockInternetManager.openConnection(mockDataFile)).thenReturn(mockConnection);
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnLastModifiedError()"))
            .when(mockConnection).getLastModified();

        boolean result = spySettingsManager.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredOnConnectionError() {
        when(mockDataFile.getProtocol()).thenReturn("http");
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnConnectionError()"))
            .when(mockInternetManager).openConnection(mockDataFile);

        boolean result = spySettingsManager.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowFileSystemDataFileHasExpired() {
        File lastIndexedFile = new File(getConfigDirectory(), "last-indexed");
        lastIndexedFile.createNewFile();
        lastIndexedFile.setLastModified(getDateTimeInMillis(1975, 1, 1, 0, 0));
        when(mockDataFile.getProtocol()).thenReturn("file");
        when(mockDataFile.toURI()).thenReturn(new File(lastIndexedFile.getAbsolutePath()).toURI());
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(spySettingsManager).getLastIndexedDate();

        boolean result = spySettingsManager.hasDataFileExpired();

        assertThat(result).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldShowFileSystemDataFileHasNotExpiredOnFileReadError() {
        when(mockDataFile.getProtocol()).thenReturn("file");
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowFileSystemDataFileHasNotExpiredOnFileReadError()"))
            .when(mockDataFile).toURI();

        boolean result = spySettingsManager.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldGetLastIndexedDate() {
        LocalDateTime now = LocalDateTime.now();
        File lastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write(Long.toString(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }

        LocalDateTime result = spySettingsManager.getLastIndexedDate();
        
        assertThat(result).isEqualTo(now);
    }

    @Test
    public void shouldGetDefaultLastIndexedDate() {
        doNothing().when(spySettingsManager).setLastIndexedDate(any());

        LocalDateTime result = spySettingsManager.getLastIndexedDate();

        assertThat(result).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @SneakyThrows
    public void shouldNotGetLastIndexedDateOnFileReadError() {
        File lastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write("Unparseable");
            writer.newLine();
        }

        LocalDateTime result = spySettingsManager.getLastIndexedDate();

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldSetLastIndexedDate() {
        LocalDateTime now = LocalDateTime.now();

        spySettingsManager.setLastIndexedDate(now);

        LocalDateTime lastIndexed = null;
        File lastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());
        try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
            lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
                ZoneId.systemDefault());
        }

        assertThat(lastIndexed).isEqualTo(now);
    }

    @Test
    @SneakyThrows
    public void shouldSetLastIndexedDateIfAlreadyExists() {
        File writeLastIndexedFile = new File(getConfigDirectory(), "last-indexed");
        LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
            writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }

        LocalDateTime now = LocalDateTime.now();

        spySettingsManager.setLastIndexedDate(now);

        LocalDateTime lastIndexed = null;
        File lastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());
        try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
            lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
                ZoneId.systemDefault());
        }

        assertThat(lastIndexed).isEqualTo(now);
    }

    @Test
    public void shouldNotSetLastIndexedDateOnExceptionIfNotAlreadyExists() {
        LocalDateTime mockLocalDateTime = mock(LocalDateTime.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
            .when(mockLocalDateTime).atZone(any());

        spySettingsManager.setLastIndexedDate(mockLocalDateTime);

        File lastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());

        assertThat(lastIndexedFile.exists()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldLeaveLastIndexedDateOnExceptionIfAlreadyExists() {
        File writeLastIndexedFile = new File(getConfigDirectory(), "last-indexed");
        LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
            writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }

        LocalDateTime mockLocalDateTime = mock(LocalDateTime.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
            .when(mockLocalDateTime).atZone(any());

        spySettingsManager.setLastIndexedDate(mockLocalDateTime);

        LocalDateTime lastIndexed = null;
        File readLastIndexedFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getLastIndexedFile());
        try (BufferedReader reader = new BufferedReader(new FileReader(readLastIndexedFile))) {
            lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
                ZoneId.systemDefault());
        }

        assertThat(lastIndexed).isEqualTo(originalLastIndexed);
    }

    @Test
    public void shouldLoadWindowSettingsFromDefault() {
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
    @SneakyThrows
    public void shouldLoadWindowSettingsFromFile() {
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getWindowSettingsFile());
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
    @SneakyThrows
    public void shouldNotLoadWindowSettingsFromFileOnException() {
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getWindowSettingsFile());
        Window window = new Window(100, 200, 300, 400);

        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(new Gson().toJson(window));
        }

        Gson mockGson = mock(Gson.class);
        Stage mockStage = mock(Stage.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotLoadWindowSettingsFromFileOnException()"))
            .when(mockGson).fromJson(any(FileReader.class), (Class<?>)any(Class.class));

        setField(spySettingsManager, "gson", mockGson);

        spySettingsManager.loadWindowSettings(mockStage);

        verify(mockStage, never()).setX(anyDouble());
        verify(mockStage, never()).setY(anyDouble());
        verify(mockStage, never()).setWidth(anyDouble());
        verify(mockStage, never()).setHeight(anyDouble());
    }

    @Test
    @SneakyThrows
    public void shouldSaveWindowSettings() {
        Stage mockStage = mock(Stage.class);
        when(mockStage.getX()).thenReturn(100d);
        when(mockStage.getY()).thenReturn(200d);
        when(mockStage.getWidth()).thenReturn(300d);
        when(mockStage.getHeight()).thenReturn(400d);

        spySettingsManager.saveWindowSettings(mockStage);

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getWindowSettingsFile());
        Window window;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            window = new Gson().fromJson(fileReader, Window.class);
        }

        assertThat(window.getX()).isEqualTo(100d);
        assertThat(window.getY()).isEqualTo(200d);
        assertThat(window.getWidth()).isEqualTo(300d);
        assertThat(window.getHeight()).isEqualTo(400d);
    }

    @Test
    @SneakyThrows
    public void shouldNotSaveWindowSettingsOnException() {
        Stage mockStage = mock(Stage.class);
        when(mockStage.getX()).thenReturn(100d);
        when(mockStage.getY()).thenReturn(200d);
        when(mockStage.getWidth()).thenReturn(300d);
        when(mockStage.getHeight()).thenReturn(400d);

        Gson mockGson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveWindowSettingsOnException()")).when(mockGson)
            .toJson(any(Window.class));

        setField(spySettingsManager, "gson", mockGson);

        spySettingsManager.saveWindowSettings(mockStage);

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getWindowSettingsFile());
        Window window = null;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            window = new Gson().fromJson(fileReader, Window.class);
        }

        assertThat(window).isNull();
    }

    @Test
    public void shouldLoadSystemSettingsFromDefault() {
        doNothing().when(spySettingsManager).saveSystemSettings();

        spySettingsManager.loadSystemSettings();

        SystemSettings systemSettings = spySettingsManager.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(mockAppProperties.getCacheSizeMb());
        assertThat(systemSettings.getProxyHost()).isNull();
        assertThat(systemSettings.getProxyPort()).isNull();
        assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
        assertThat(systemSettings.getProxyUsername()).isNull();
        assertThat(systemSettings.getProxyPassword()).isNull();

        verify(spySettingsManager, times(1)).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadSystemSettingsFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadSystemSettingsFromFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadSystemSettings();

        SystemSettings systemSettings = spySettingsManager.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(123);
        assertThat(systemSettings.getProxyHost()).isEqualTo("localhost");
        assertThat(systemSettings.getProxyPort()).isEqualTo(8080);
        assertThat(systemSettings.getProxyRequiresAuthentication()).isTrue();
        assertThat(systemSettings.getProxyUsername()).isEqualTo("username");
        assertThat(systemSettings.getProxyPassword()).isEqualTo("password");

        verify(spySettingsManager, never()).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotLoadSystemSettingsFromAnInvalidFile() {
        doNothing().when(spySettingsManager).saveSystemSettings();

        File testSettings = getTestResourceFile(
            "json/settingsManager-shouldNotLoadSystemSettingsFromAnInvalidFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadSystemSettings();

        SystemSettings systemSettings = spySettingsManager.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(mockAppProperties.getCacheSizeMb());
        assertThat(systemSettings.getProxyHost()).isNull();
        assertThat(systemSettings.getProxyPort()).isNull();
        assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
        assertThat(systemSettings.getProxyUsername()).isNull();
        assertThat(systemSettings.getProxyPassword()).isNull();

        verify(spySettingsManager, times(1)).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldSaveSystemSettings() {
        setField(spySettingsManager, "systemSettings", new SystemSettings());

        SystemSettings systemSettings = spySettingsManager.getSystemSettings();
        systemSettings.setCacheSizeMb(123);
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);
        systemSettings.setProxyRequiresAuthentication(true);
        systemSettings.setProxyUsername("username");
        systemSettings.setProxyPassword("password");

        spySettingsManager.saveSystemSettings();

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());
        SystemSettings result = null;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            result = new Gson().fromJson(fileReader, SystemSettings.class);
        }

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(123);
        assertThat(systemSettings.getProxyHost()).isEqualTo("localhost");
        assertThat(systemSettings.getProxyPort()).isEqualTo(8080);
        assertThat(systemSettings.getProxyRequiresAuthentication()).isTrue();
        assertThat(systemSettings.getProxyUsername()).isEqualTo("username");
        assertThat(systemSettings.getProxyPassword()).isEqualTo("password");
    }

    @Test
    @SneakyThrows
    public void shouldNotSaveSystemSettingsOnException() {
        Gson mockGson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSystemSettingsOnException()")).when(mockGson)
            .toJson(any(SystemSettings.class));

        setField(spySettingsManager, "gson", mockGson);
        setField(spySettingsManager, "systemSettings", new SystemSettings());

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());
        settingsFile.delete();

        spySettingsManager.saveSystemSettings();

        SystemSettings result = null;

        if (settingsFile.exists()) {
            try (FileReader fileReader = new FileReader(settingsFile)) {
                result = new Gson().fromJson(fileReader, SystemSettings.class);
            }
        }

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldReturnNewVersionWhenSystemVersionIsNull() {
        File testSettings = getTestResourceFile(
            "json/settingsManager-shouldReturnNewVersionWhenSystemVersionIsNull.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadSystemSettings();

        setField(spySettingsManager, "version", new Version("1.1.1"));

        assertThat(spySettingsManager.isNewVersion()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnNewVersionWhenSystemVersionIsLower() {
        File testSettings = getTestResourceFile(
            "json/settingsManager-shouldReturnNewVersionWhenSystemVersionIsLower.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadSystemSettings();

        setField(spySettingsManager, "version", new Version("1.1.1"));

        assertThat(spySettingsManager.isNewVersion()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldNotReturnNewVersionWhenSystemVersionIsEqual() {
        File testSettings = getTestResourceFile(
            "json/settingsManager-shouldNotReturnNewVersionWhenSystemVersionIsEqual.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadSystemSettings();

        setField(spySettingsManager, "version", new Version("1.1.1"));

        assertThat(spySettingsManager.isNewVersion()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsFromFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        Track mockTrack = mock(Track.class);
        when(mockSearchManager.getTrackById("92f9b8ad82601ab97c121239518730108eefa18055ead908e5cdaf369023984b"))
            .thenReturn(mockTrack);

        spySettingsManager.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(spySettingsManager, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(mockPlaylistManager, times(1)).setShuffle(true, true);
        verify(mockPlaylistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mockMediaManager, times(10)).setEqualizerGain(anyInt(), anyDouble());
        verify(mockSearchManager, times(15)).getTrackById(anyString());
        verify(spySettingsManager, never()).saveUserSettings();
    }

    @Test
    public void shouldLoadUserSettingsWithNoExistingFile() {
        doNothing().when(spySettingsManager).saveUserSettings();

        spySettingsManager.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(spySettingsManager, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(spySettingsManager, times(1)).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsWithNoEqFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsWithNoEqFromFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(spySettingsManager, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(mockPlaylistManager, times(1)).setShuffle(true, true);
        verify(mockPlaylistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mockMediaManager, never()).setEqualizerGain(anyInt(), anyDouble());
        verify(mockSearchManager, times(15)).getTrackById(anyString());
        verify(spySettingsManager, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsWithNoPlaylistsFromFile() {
        File testSettings = getTestResourceFile(
            "json/settingsManager-shouldLoadUserSettingsWithNoPlaylistsFromFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(spySettingsManager, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(mockPlaylistManager, times(1)).setShuffle(true, true);
        verify(mockPlaylistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mockMediaManager, times(10)).setEqualizerGain(anyInt(), anyDouble());
        verify(mockSearchManager, never()).getTrackById(anyString());
        verify(spySettingsManager, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotLoadUserSettingsFromAnInvalidFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldNotLoadUserSettingsFromAnInvalidFile.json");
        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        spySettingsManager.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(spySettingsManager, "userSettingsLoaded");

        assertThat(settingsLoaded).isFalse();

        verify(spySettingsManager, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldSaveUserSettings() {
        setField(spySettingsManager, "userSettingsLoaded", true);

        when(mockPlaylistManager.isShuffle()).thenReturn(true);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer mockEqualizer = mock(Equalizer.class);
        when(mockEqualizer.getNumberOfBands()).thenReturn(5);
        when(mockMediaManager.getEqualizer()).thenReturn(mockEqualizer);

        Playlist mockSearchPlaylist = mock(Playlist.class);
        when(mockSearchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist mockFavouritesPlaylist = mock(Playlist.class);
        when(mockFavouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> mockPlaylists = Arrays.asList(new Playlist[] { mockSearchPlaylist, mockFavouritesPlaylist });
        when(mockPlaylistManager.getPlaylists()).thenReturn(mockPlaylists);

        spySettingsManager.saveUserSettings();

        verify(mockPlaylistManager, times(1)).isShuffle();
        verify(mockPlaylistManager, times(1)).getRepeat();
        verify(mockMediaManager, times(1)).getEqualizer();
        verify(mockEqualizer, times(5)).getGain(anyInt());
        verify(mockPlaylistManager, times(1)).getPlaylists();

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        Settings settings;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            settings = new Gson().fromJson(fileReader, Settings.class);
        }

        assertThat(settings.getPlaylists()).hasSize(1);
        assertThat(settings.getPlaylists().get(0).getId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldNotSaveUserSettingsIfNotUserSettingsLoaded() {
        setField(spySettingsManager, "userSettingsLoaded", false);

        spySettingsManager.saveUserSettings();

        verify(mockPlaylistManager, never()).isShuffle();
    }

    @Test
    public void shouldNotSaveUserSettingsOnException() {
        setField(spySettingsManager, "userSettingsLoaded", true);

        when(mockPlaylistManager.isShuffle()).thenReturn(true);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer mockEqualizer = mock(Equalizer.class);
        when(mockEqualizer.getNumberOfBands()).thenReturn(5);
        when(mockMediaManager.getEqualizer()).thenReturn(mockEqualizer);

        Playlist mockSearchPlaylist = mock(Playlist.class);
        when(mockSearchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist mockFavouritesPlaylist = mock(Playlist.class);
        when(mockFavouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> mockPlaylists = Arrays.asList(new Playlist[] { mockSearchPlaylist, mockFavouritesPlaylist });
        when(mockPlaylistManager.getPlaylists()).thenReturn(mockPlaylists);

        Gson mockGson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveUserSettingsOnException()")).when(mockGson)
            .toJson(any(Settings.class));

        setField(spySettingsManager, "gson", mockGson);

        spySettingsManager.saveUserSettings();

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        assertThat(settingsFile.exists()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldNotSaveUserSettingsOnExceptionWhenFileAlreadyExists() {
        File newSettingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());
        newSettingsFile.createNewFile();

        setField(spySettingsManager, "userSettingsLoaded", true);

        when(mockPlaylistManager.isShuffle()).thenReturn(true);
        when(mockPlaylistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer mockEqualizer = mock(Equalizer.class);
        when(mockEqualizer.getNumberOfBands()).thenReturn(5);
        when(mockMediaManager.getEqualizer()).thenReturn(mockEqualizer);

        Playlist mockSearchPlaylist = mock(Playlist.class);
        when(mockSearchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist mockFavouritesPlaylist = mock(Playlist.class);
        when(mockFavouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> mockPlaylists = Arrays.asList(new Playlist[] { mockSearchPlaylist, mockFavouritesPlaylist });
        when(mockPlaylistManager.getPlaylists()).thenReturn(mockPlaylists);

        Gson mockGson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSettingsOnException()")).when(mockGson)
            .toJson(any(Settings.class));

        setField(spySettingsManager, "gson", mockGson);

        spySettingsManager.saveUserSettings();

        File settingsFile = spySettingsManager.getFileFromConfigDirectory(mockAppProperties.getUserSettingsFile());

        assertThat(settingsFile.exists()).isTrue();
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }
}
