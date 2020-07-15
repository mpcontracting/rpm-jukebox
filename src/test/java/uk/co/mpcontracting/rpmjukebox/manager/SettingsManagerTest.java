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

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.*;

public class SettingsManagerTest extends AbstractGUITest implements Constants {

    @Mock
    private AppProperties appProperties;

    @Mock
    private RpmJukebox rpmJukebox;

    @Mock
    private MessageManager messageManager;

    @Mock
    private SearchManager searchManager;

    @Mock
    private PlaylistManager playlistManager;

    @Mock
    private MediaManager mediaManager;

    @Mock
    private InternetManager internetManager;

    @Mock
    private URL dataFile;

    private SettingsManager underTest;

    @Before
    @SneakyThrows
    public void setup() {
        underTest = spy(new SettingsManager(appProperties, rpmJukebox, messageManager));
        underTest.wireSearchManager(searchManager);
        underTest.wirePlaylistManager(playlistManager);
        underTest.wireMediaManager(mediaManager);
        underTest.wireInternetManager(internetManager);

        setField(underTest, "gson", new GsonBuilder().setPrettyPrinting().create());
        setField(underTest, "version", new Version("1.0.0"));
        setField(underTest, "dataFile", dataFile);

        getConfigDirectory().mkdirs();

        when(appProperties.getVersion()).thenReturn("1.0.0");
        when(appProperties.getCacheSizeMb()).thenReturn(250);

        when(appProperties.getLastIndexedFile()).thenReturn("last-indexed");
        when(underTest.getFileFromConfigDirectory("last-indexed")).thenReturn(new File(getConfigDirectory(), "last-indexed"));

        when(appProperties.getWindowSettingsFile()).thenReturn("window.json");
        when(underTest.getFileFromConfigDirectory("window.json")).thenReturn(new File(getConfigDirectory(), "window.json"));

        when(appProperties.getSystemSettingsFile()).thenReturn("system.json");
        when(underTest.getFileFromConfigDirectory("system.json")).thenReturn(new File(getConfigDirectory(), "system.json"));

        when(appProperties.getUserSettingsFile()).thenReturn("rpm-jukebox.json");
        when(underTest.getFileFromConfigDirectory("rpm-jukebox.json")).thenReturn(new File(getConfigDirectory(), "rpm-jukebox.json"));
    }

    @Test
    public void shouldGetFileFromConfigDirectory() {
        File correctValue = new File(RpmJukebox.getConfigDirectory(), "test");
        File result = underTest.getFileFromConfigDirectory("test");

        assertThat(result.getAbsolutePath()).isEqualTo(correctValue.getAbsolutePath());
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasExpired() {
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(dataFile.getProtocol()).thenReturn("http");
        when(internetManager.openConnection(dataFile)).thenReturn(httpURLConnection);
        when(httpURLConnection.getLastModified()).thenReturn(getDateTimeInMillis(1975, 1, 1, 0, 0));
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedNotOneHourOld() {
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(dataFile.getProtocol()).thenReturn("http");
        when(internetManager.openConnection(dataFile)).thenReturn(httpURLConnection);
        when(httpURLConnection.getLastModified())
                .thenReturn(getLocalDateTimeInMillis(LocalDateTime.now().minusMinutes(30)));
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredAsLastModifiedBeforeLastIndexed() {
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(dataFile.getProtocol()).thenReturn("http");
        when(internetManager.openConnection(dataFile)).thenReturn(httpURLConnection);
        when(httpURLConnection.getLastModified()).thenReturn(getDateTimeInMillis(1971, 1, 1, 0, 0));
        doReturn(LocalDateTime.of(1975, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredOnLastModifiedError() {
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(dataFile.getProtocol()).thenReturn("http");
        when(internetManager.openConnection(dataFile)).thenReturn(httpURLConnection);
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnLastModifiedError()"))
                .when(httpURLConnection).getLastModified();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowHttpDataFileHasNotExpiredOnConnectionError() {
        when(dataFile.getProtocol()).thenReturn("http");
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnConnectionError()"))
                .when(internetManager).openConnection(dataFile);

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldShowFileSystemDataFileHasExpired() {
        File lastIndexedFile = new File(getConfigDirectory(), "last-indexed");
        lastIndexedFile.createNewFile();
        lastIndexedFile.setLastModified(getDateTimeInMillis(1975, 1, 1, 0, 0));
        when(dataFile.getProtocol()).thenReturn("file");
        when(dataFile.toURI()).thenReturn(new File(lastIndexedFile.getAbsolutePath()).toURI());
        doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldShowFileSystemDataFileHasNotExpiredOnFileReadError() {
        when(dataFile.getProtocol()).thenReturn("file");
        doThrow(new RuntimeException("SettingsManagerTest.shouldShowFileSystemDataFileHasNotExpiredOnFileReadError()"))
                .when(dataFile).toURI();

        boolean result = underTest.hasDataFileExpired();

        assertThat(result).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldGetLastIndexedDate() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ), ZoneId.systemDefault());
        File lastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write(Long.toString(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        }

        LocalDateTime result = underTest.getLastIndexedDate();

        assertThat(result).isEqualTo(now);
    }

    @Test
    public void shouldGetDefaultLastIndexedDate() {
        doNothing().when(underTest).setLastIndexedDate(any());

        LocalDateTime result = underTest.getLastIndexedDate();

        assertThat(result).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @SneakyThrows
    public void shouldNotGetLastIndexedDateOnFileReadError() {
        File lastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write("Unparseable");
            writer.newLine();
        }

        LocalDateTime result = underTest.getLastIndexedDate();

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldSetLastIndexedDate() {
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ), ZoneId.systemDefault());

        underTest.setLastIndexedDate(now);

        LocalDateTime lastIndexed;
        File lastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());
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

        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ), ZoneId.systemDefault());

        underTest.setLastIndexedDate(now);

        LocalDateTime lastIndexed;
        File lastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());
        try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
            lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
                    ZoneId.systemDefault());
        }

        assertThat(lastIndexed).isEqualTo(now);
    }

    @Test
    public void shouldNotSetLastIndexedDateOnExceptionIfNotAlreadyExists() {
        LocalDateTime localDateTime = mock(LocalDateTime.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
                .when(localDateTime).atZone(any());

        underTest.setLastIndexedDate(localDateTime);

        File lastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());

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

        LocalDateTime localDateTime = mock(LocalDateTime.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
                .when(localDateTime).atZone(any());

        underTest.setLastIndexedDate(localDateTime);

        LocalDateTime lastIndexed ;
        File readLastIndexedFile = underTest.getFileFromConfigDirectory(appProperties.getLastIndexedFile());
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

        Stage stage = mock(Stage.class);
        underTest.loadWindowSettings(stage);

        verify(stage, times(1)).setX(x);
        verify(stage, times(1)).setY(y);
        verify(stage, times(1)).setWidth(width);
        verify(stage, times(1)).setHeight(height);
    }

    @Test
    @SneakyThrows
    public void shouldLoadWindowSettingsFromFile() {
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getWindowSettingsFile());
        Window window = Window.builder()
                .x(100)
                .y(200)
                .width(300)
                .height(400)
                .build();

        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(new Gson().toJson(window));
        }

        Stage stage = mock(Stage.class);
        underTest.loadWindowSettings(stage);

        verify(stage, times(1)).setX(100d);
        verify(stage, times(1)).setY(200d);
        verify(stage, times(1)).setWidth(300d);
        verify(stage, times(1)).setHeight(400d);
    }

    @Test
    @SneakyThrows
    public void shouldNotLoadWindowSettingsFromFileOnException() {
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getWindowSettingsFile());
        Window window = Window.builder()
                .x(100)
                .y(200)
                .width(300)
                .height(400)
                .build();

        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(new Gson().toJson(window));
        }

        Gson gson = mock(Gson.class);
        Stage stage = mock(Stage.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotLoadWindowSettingsFromFileOnException()"))
                .when(gson).fromJson(any(FileReader.class), (Class<?>) any(Class.class));

        setField(underTest, "gson", gson);

        underTest.loadWindowSettings(stage);

        verify(stage, never()).setX(anyDouble());
        verify(stage, never()).setY(anyDouble());
        verify(stage, never()).setWidth(anyDouble());
        verify(stage, never()).setHeight(anyDouble());
    }

    @Test
    @SneakyThrows
    public void shouldSaveWindowSettings() {
        Stage stage = mock(Stage.class);
        when(stage.getX()).thenReturn(100d);
        when(stage.getY()).thenReturn(200d);
        when(stage.getWidth()).thenReturn(300d);
        when(stage.getHeight()).thenReturn(400d);

        underTest.saveWindowSettings(stage);

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getWindowSettingsFile());
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
        Stage stage = mock(Stage.class);
        when(stage.getX()).thenReturn(100d);
        when(stage.getY()).thenReturn(200d);
        when(stage.getWidth()).thenReturn(300d);
        when(stage.getHeight()).thenReturn(400d);

        Gson gson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveWindowSettingsOnException()")).when(gson)
                .toJson(any(Window.class));

        setField(underTest, "gson", gson);

        underTest.saveWindowSettings(stage);

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getWindowSettingsFile());
        Window window;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            window = new Gson().fromJson(fileReader, Window.class);
        }

        assertThat(window).isNull();
    }

    @Test
    public void shouldLoadSystemSettingsFromDefault() {
        doNothing().when(underTest).saveSystemSettings();

        underTest.loadSystemSettings();

        SystemSettings systemSettings = underTest.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(appProperties.getCacheSizeMb());
        assertThat(systemSettings.getProxyHost()).isNull();
        assertThat(systemSettings.getProxyPort()).isNull();
        assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
        assertThat(systemSettings.getProxyUsername()).isNull();
        assertThat(systemSettings.getProxyPassword()).isNull();

        verify(underTest, times(1)).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadSystemSettingsFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadSystemSettingsFromFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadSystemSettings();

        SystemSettings systemSettings = underTest.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(123);
        assertThat(systemSettings.getProxyHost()).isEqualTo("localhost");
        assertThat(systemSettings.getProxyPort()).isEqualTo(8080);
        assertThat(systemSettings.getProxyRequiresAuthentication()).isTrue();
        assertThat(systemSettings.getProxyUsername()).isEqualTo("username");
        assertThat(systemSettings.getProxyPassword()).isEqualTo("password");

        verify(underTest, never()).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotLoadSystemSettingsFromAnInvalidFile() {
        doNothing().when(underTest).saveSystemSettings();

        File testSettings = getTestResourceFile(
                "json/settingsManager-shouldNotLoadSystemSettingsFromAnInvalidFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadSystemSettings();

        SystemSettings systemSettings = underTest.getSystemSettings();

        assertThat(systemSettings.getCacheSizeMb()).isEqualTo(appProperties.getCacheSizeMb());
        assertThat(systemSettings.getProxyHost()).isNull();
        assertThat(systemSettings.getProxyPort()).isNull();
        assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
        assertThat(systemSettings.getProxyUsername()).isNull();
        assertThat(systemSettings.getProxyPassword()).isNull();

        verify(underTest, times(1)).saveSystemSettings();
    }

    @Test
    @SneakyThrows
    public void shouldSaveSystemSettings() {
        setField(underTest, "systemSettings", SystemSettings.builder().build());

        SystemSettings systemSettings = underTest.getSystemSettings();
        systemSettings.setCacheSizeMb(123);
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);
        systemSettings.setProxyRequiresAuthentication(true);
        systemSettings.setProxyUsername("username");
        systemSettings.setProxyPassword("password");

        underTest.saveSystemSettings();

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());
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
        Gson gson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSystemSettingsOnException()")).when(gson)
                .toJson(any(SystemSettings.class));

        setField(underTest, "gson", gson);
        setField(underTest, "systemSettings", SystemSettings.builder().build());

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());
        settingsFile.delete();

        underTest.saveSystemSettings();

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
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadSystemSettings();

        setField(underTest, "version", new Version("1.1.1"));

        assertThat(underTest.isNewVersion()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldReturnNewVersionWhenSystemVersionIsLower() {
        File testSettings = getTestResourceFile(
                "json/settingsManager-shouldReturnNewVersionWhenSystemVersionIsLower.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadSystemSettings();

        setField(underTest, "version", new Version("1.1.1"));

        assertThat(underTest.isNewVersion()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldNotReturnNewVersionWhenSystemVersionIsEqual() {
        File testSettings = getTestResourceFile(
                "json/settingsManager-shouldNotReturnNewVersionWhenSystemVersionIsEqual.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getSystemSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadSystemSettings();

        setField(underTest, "version", new Version("1.1.1"));

        assertThat(underTest.isNewVersion()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsFromFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        Track mockTrack = mock(Track.class);
        when(searchManager.getTrackById("92f9b8ad82601ab97c121239518730108eefa18055ead908e5cdaf369023984b"))
                .thenReturn(of(mockTrack));

        underTest.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(underTest, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(playlistManager, times(1)).setShuffle(true, true);
        verify(playlistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mediaManager, times(10)).setEqualizerGain(anyInt(), anyDouble());
        verify(searchManager, times(15)).getTrackById(anyString());
        verify(underTest, never()).saveUserSettings();
    }

    @Test
    public void shouldLoadUserSettingsWithNoExistingFile() {
        doNothing().when(underTest).saveUserSettings();

        underTest.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(underTest, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(underTest, times(1)).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsWithNoEqFromFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsWithNoEqFromFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(underTest, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(playlistManager, times(1)).setShuffle(true, true);
        verify(playlistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mediaManager, never()).setEqualizerGain(anyInt(), anyDouble());
        verify(searchManager, times(15)).getTrackById(anyString());
        verify(underTest, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldLoadUserSettingsWithNoPlaylistsFromFile() {
        File testSettings = getTestResourceFile(
                "json/settingsManager-shouldLoadUserSettingsWithNoPlaylistsFromFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(underTest, "userSettingsLoaded");

        assertThat(settingsLoaded).isTrue();

        verify(playlistManager, times(1)).setShuffle(true, true);
        verify(playlistManager, times(1)).setRepeat(Repeat.ALL);
        verify(mediaManager, times(10)).setEqualizerGain(anyInt(), anyDouble());
        verify(searchManager, never()).getTrackById(anyString());
        verify(underTest, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldNotLoadUserSettingsFromAnInvalidFile() {
        File testSettings = getTestResourceFile("json/settingsManager-shouldNotLoadUserSettingsFromAnInvalidFile.json");
        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        Files.copy(testSettings, settingsFile);

        underTest.loadUserSettings();
        boolean settingsLoaded = (Boolean) getField(underTest, "userSettingsLoaded");

        assertThat(settingsLoaded).isFalse();

        verify(underTest, never()).saveUserSettings();
    }

    @Test
    @SneakyThrows
    public void shouldSaveUserSettings() {
        setField(underTest, "userSettingsLoaded", true);

        when(playlistManager.isShuffle()).thenReturn(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer equalizer = mock(Equalizer.class);
        when(equalizer.getNumberOfBands()).thenReturn(5);
        when(mediaManager.getEqualizer()).thenReturn(equalizer);

        Playlist searchPlaylist = mock(Playlist.class);
        when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist favouritesPlaylist = mock(Playlist.class);
        when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> playlists = Arrays.asList(searchPlaylist, favouritesPlaylist);
        when(playlistManager.getPlaylists()).thenReturn(playlists);

        underTest.saveUserSettings();

        verify(playlistManager, times(1)).isShuffle();
        verify(playlistManager, times(1)).getRepeat();
        verify(mediaManager, times(1)).getEqualizer();
        verify(equalizer, times(5)).getGain(anyInt());
        verify(playlistManager, times(1)).getPlaylists();

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        Settings settings;

        try (FileReader fileReader = new FileReader(settingsFile)) {
            settings = new Gson().fromJson(fileReader, Settings.class);
        }

        assertThat(settings.getPlaylists()).hasSize(1);
        assertThat(settings.getPlaylists().get(0).getId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
    }

    @Test
    public void shouldNotSaveUserSettingsIfNotUserSettingsLoaded() {
        setField(underTest, "userSettingsLoaded", false);

        underTest.saveUserSettings();

        verify(playlistManager, never()).isShuffle();
    }

    @Test
    public void shouldNotSaveUserSettingsOnException() {
        setField(underTest, "userSettingsLoaded", true);

        when(playlistManager.isShuffle()).thenReturn(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer equalizer = mock(Equalizer.class);
        when(equalizer.getNumberOfBands()).thenReturn(5);
        when(mediaManager.getEqualizer()).thenReturn(equalizer);

        Playlist searchPlaylist = mock(Playlist.class);
        when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist favouritesPlaylist = mock(Playlist.class);
        when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> playlists = Arrays.asList(searchPlaylist, favouritesPlaylist);
        when(playlistManager.getPlaylists()).thenReturn(playlists);

        Gson gson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveUserSettingsOnException()")).when(gson)
                .toJson(any(Settings.class));

        setField(underTest, "gson", gson);

        underTest.saveUserSettings();

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        assertThat(settingsFile.exists()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldNotSaveUserSettingsOnExceptionWhenFileAlreadyExists() {
        File newSettingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());
        newSettingsFile.createNewFile();

        setField(underTest, "userSettingsLoaded", true);

        when(playlistManager.isShuffle()).thenReturn(true);
        when(playlistManager.getRepeat()).thenReturn(Repeat.ALL);

        Equalizer equalizer = mock(Equalizer.class);
        when(equalizer.getNumberOfBands()).thenReturn(5);
        when(mediaManager.getEqualizer()).thenReturn(equalizer);

        Playlist searchPlaylist = mock(Playlist.class);
        when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

        Playlist favouritesPlaylist = mock(Playlist.class);
        when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

        List<Playlist> playlists = Arrays.asList(searchPlaylist, favouritesPlaylist);
        when(playlistManager.getPlaylists()).thenReturn(playlists);

        Gson gson = mock(Gson.class);
        doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSettingsOnException()")).when(gson)
                .toJson(any(Settings.class));

        setField(underTest, "gson", gson);

        underTest.saveUserSettings();

        File settingsFile = underTest.getFileFromConfigDirectory(appProperties.getUserSettingsFile());

        assertThat(settingsFile.exists()).isTrue();
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }
}
